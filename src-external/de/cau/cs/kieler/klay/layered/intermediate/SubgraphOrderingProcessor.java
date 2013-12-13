/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2010 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KGraphElement;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.Util;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraphElement;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.p1cycles.GreedyCycleBreaker;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Postprocesses the node ordering phase to ensure that subgraphs are not intertwined across the
 * layers. The approach is inspired by Georg Sander, "Layout of Compound Graphs", Technical Report
 * A/03/96, Universität des Saarlandes, 1996.
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>A layered graph. The node ordering has taken place. The nodes on a layer that belong to the
 *     same compound node are placed in an unbroken sequence in the layer.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>The nodes are ordered such that the subgraphs have the same relative position on all layers.
 *     The nodes of one subgraph on one layer are still placed next to each other without other nodes
 *     between them.</dd>
 *   <dt>Slots:</dt>
 *     <dd>After phase 3.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>none.</dd>
 * </dl>
 * 
 * @author ima
 * @kieler.design 2012-08-10 chsch grh
 */
public final class SubgraphOrderingProcessor implements ILayoutProcessor {

    /**
     * Document the layers that are resorted.
     */
    private HashMap<Layer, HashMap<LNode, LinkedList<LNode>>> reorderedLayers;

    /**
     * Store the node orderings for compound nodes.
     */
    private HashMap<LNode, LinkedList<LNode>> orderedLists;

    /**
     * Keep a childrenlist for every compound node relevant for this layer.
     * This is done to preserve the ordering of compound nodes as far as possible.
     */
    private HashMap<Layer, HashMap<LNode, LinkedList<LNode>>> compoundChildrenLists;

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin(
                "Order subgraphs so that the relative position " + "is the same on all layers", 1);

        reorderedLayers = new HashMap<Layer, HashMap<LNode, LinkedList<LNode>>>();

        // A subgraph ordering graph is used to find the correct subgraph ordering. The subgraph 
        // ordering graph is disconnected - it consists of connected components for each compound
        // node. Represent it as a HashMap of layered Graphs. The compound nodes serve as keys.
        HashMap<LNode, LGraph> subgraphOrderingGraph = new HashMap<LNode, LGraph>();

        // Make up an LNode that is to represent the layeredGraph as a key in
        // the subgraphOrderingGraph
        LNode graphKey = new LNode(layeredGraph);
        graphKey.copyProperties(layeredGraph);

        // Document the insertion of nodes into the subgraphOrderingGraph.
        HashMap<LNode, LNode> insertedNodes = new HashMap<LNode, LNode>();

        // Get the layeredGraph's element map.
        HashMap<KGraphElement, LGraphElement> elemMap = layeredGraph
                .getProperty(Properties.ELEMENT_MAP);

        compoundChildrenLists = new HashMap<Layer, HashMap<LNode, LinkedList<LNode>>>();

        // Build the subgraphOrderingGraph:
        // Insert nodes and edges representing the relationship "is left of"
        // into the subgraph ordering graph parts.
        for (Layer layer : layeredGraph) {

            Random random = layeredGraph.getProperty(Properties.RANDOM);

            // Initialize the compoundChildrenLists-HashMap for this layer
            HashMap<LNode, LinkedList<LNode>> layerCompoundChildrenLists 
                = new HashMap<LNode, LinkedList<LNode>>();

            // Keep a list of associated Nodes for every compound node relevant
            // for this layer for later order application.
            HashMap<LNode, LinkedList<LNode>> layerCompoundContents 
                = new HashMap<LNode, LinkedList<LNode>>();

            List<LNode> layerNodes = layer.getNodes();
            boolean reordered = false;

            for (int i = 0; i < (layerNodes.size() - 1); i++) {

                LNode currentNode = layerNodes.get(i);
                LNode relatedCompoundCurrent = Util.getRelatedCompoundNode(currentNode,
                        layeredGraph);
                // Store the currentNode in layerCompoundContents under the key
                // of relatedCompoundCurrent.
                insertRelatedCompound(layerCompoundContents, currentNode, relatedCompoundCurrent,
                        graphKey);
                // fill in the insertions in layerCompoundContents up the inclusion tree
                recursiveInsert(layerCompoundChildrenLists, currentNode, relatedCompoundCurrent,
                        graphKey);
                LNode nextNode = layerNodes.get(i + 1);
                // Store the currentNode in layerCompoundContents under the key
                // of relatedCompoundCurrent.
                LNode relatedCompoundNext = Util.getRelatedCompoundNode(nextNode, layeredGraph);
                insertRelatedCompound(layerCompoundContents, nextNode, relatedCompoundNext,
                        graphKey);
                // fill in the insertions in the layerCompoundContents up the
                // inclusion tree
                recursiveInsert(layerCompoundChildrenLists, nextNode, relatedCompoundNext, graphKey);

                // The subgraphOrderingGraph has to be updated, if nodes that are neighbors in a
                // layer are of different compound nodes and no leave nodes of highest level.
                if ((relatedCompoundCurrent != null) && (relatedCompoundNext != null)
                        && (relatedCompoundCurrent != relatedCompoundNext)) {
                    reordered = true;
                    // Find the correct partial graph to insert the "is left of"-relationship by
                    // propagating the dependency up the inclusion tree till two compound nodes with
                    // the same parent are reached.
                    LinkedList<LNode> leftRightList = new LinkedList<LNode>();
                    leftRightList.add(currentNode);
                    leftRightList.add(nextNode);
                    Util.propagatePair(leftRightList, elemMap);
                    LNode propCompoundCurrent = leftRightList.getFirst();
                    LNode propCompoundNext = leftRightList.getLast();
                    // Do not insert self-loops into the
                    // subgraph-ordering-graph.
                    if (propCompoundCurrent != propCompoundNext) {
                        LNode key;
                        LGraphElement parentRep = elemMap.get(propCompoundCurrent
                                .getProperty(Properties.K_PARENT));
                        if (parentRep instanceof LGraph) {
                            key = graphKey;
                        } else {
                            key = (LNode) parentRep;
                        }
                        LGraph partGraph;
                        // Get the corresponding component of the subgraphOrderingGraph or create it.
                        if (subgraphOrderingGraph.containsKey(key)) {
                            partGraph = subgraphOrderingGraph.get(key);
                        } else {
                            partGraph = new LGraph(layeredGraph);
                            partGraph.setProperty(Properties.RANDOM, random);
                            subgraphOrderingGraph.put(key, partGraph);
                        }
                        // Add representatives for the compound nodes to the ordering graph's component
                        // if not already present. Add an "is-left-of" edge between them.
                        List<LNode> nodeList = partGraph.getLayerlessNodes();
                        LNode currentRep = getNodeCopy(propCompoundCurrent, nodeList, insertedNodes);
                        LNode nextRep = getNodeCopy(propCompoundNext, nodeList, insertedNodes);
                        LEdge leftOfEdge = new LEdge(layeredGraph);
                        LPort sourcePort = new LPort(layeredGraph);
                        LPort targetPort = new LPort(layeredGraph);
                        leftOfEdge.setSource(sourcePort);
                        leftOfEdge.setTarget(targetPort);
                        sourcePort.setNode(currentRep);
                        targetPort.setNode(nextRep);
                    }
                }
            }
            if (reordered) {
                reorderedLayers.put(layer, layerCompoundContents);
                compoundChildrenLists.put(layer, layerCompoundChildrenLists);
            }
        }
        // Break the cycles in the subgraphOrderingGraph. Any cycle-breaking heuristic can be used
        // here - but with different results with respect to the number of introduced edge crossings.
        // Sander's approach for example is to break a cycle at the node with the smallest complete 
        // average position. At the time, we use the GreedyCycleBreaker here. This may be changed
        // for a heuristic using Sander's approach in the future. Convert the subgraphOrderingGraph to 
        // Lists expressing topological sortings of the subgraphOrderingGraph's components.
        Set<LNode> keys = subgraphOrderingGraph.keySet();
        orderedLists = new HashMap<LNode, LinkedList<LNode>>();
        GreedyCycleBreaker cycleBreaker = new GreedyCycleBreaker();

        // Check, if there are intertwined subgraphs at all.
        boolean noProblems = true;

        for (LNode key : keys) {
            LGraph graphComponent = subgraphOrderingGraph.get(key);
            // Remove cycles from the graph component.
            cycleBreaker.process(graphComponent, monitor.subTask(1.0f / keys.size()));
            // Extract a topological sorting from the graph component and store
            // it.
            if (graphComponent.getProperty(Properties.CYCLIC)) {
                noProblems = false;
                LinkedList<LNode> topologicalSorting = graphToList(graphComponent);
                orderedLists.put(key, topologicalSorting);
            }
        }

        // New ordering is only necessary, if there are intertwined subgraphs.
        if (!noProblems) {
            applyOrder(layeredGraph, subgraphOrderingGraph, graphKey, elemMap);
        }

        compoundChildrenLists = null;
        reorderedLayers = null;
        orderedLists = null;
        monitor.done();
    }

    /**
     * Makes insertions in the layerCompoundContents not only for nodes under the key of their
     * parent, but also for the parent under the key of its parent and so on - recursively up the
     * inclusion tree.
     * 
     * @param layerCompoundContents
     *            The map, in which each compound node relevant to the layer is stored together with
     *            a list of its children.
     * @param currentChild
     *            The currentNode to be stored.
     * @param currentParent
     *            The currentKey for which the node is to be stored.
     * @param graphKey
     *            The LNode-key representing the layeredGraph.
     */
    private void recursiveInsert(final HashMap<LNode, LinkedList<LNode>> layerCompoundContents,
            final LNode currentChild, final LGraphElement currentParent, final LNode graphKey) {
        LinkedList<LNode> currentAncestorNodesList;
        // root of inclusion tree reached
        if (currentParent instanceof LGraph) {
            currentAncestorNodesList = layerCompoundContents.get(graphKey);
            if (currentAncestorNodesList == null) {
                currentAncestorNodesList = new LinkedList<LNode>();
                layerCompoundContents.put(graphKey, currentAncestorNodesList);
            }
            // register nodes when they first turn up
            if (!currentAncestorNodesList.contains(currentChild)) {
                currentAncestorNodesList.add(currentChild);
            }
        } else {
            // root of inclusion tree not reached
            if (currentParent instanceof LNode) {
                LNode nodeKey = (LNode) currentParent;
                currentAncestorNodesList = layerCompoundContents.get(nodeKey);
                if (currentAncestorNodesList == null) {
                    currentAncestorNodesList = new LinkedList<LNode>();
                    layerCompoundContents.put(nodeKey, currentAncestorNodesList);
                }
                if (!currentAncestorNodesList.contains(currentChild)) {
                    currentAncestorNodesList.add(currentChild);
                }
                recursiveInsert(layerCompoundContents, nodeKey,
                        nodeKey.getProperty(Properties.PARENT), graphKey);
            }
        }
    }

    /**
     * This method stores the given node in the layerCompoundContents in the list given by the
     * relatedCompound as a key. If the relatedCompound is null, the key is the graphKey
     * representing the layeredGraph itself. Nodes will appear in the list in the order preserving
     * the result of the crossing-minimization phase.
     * 
     * @param layerCompoundContents
     *            HashMap mapping compound nodes to their direct children leave nodes and any dummy
     *            nodes assigned to them in the sense of the method getRelatedCompoundNode().
     * @param node
     *            The node to be stored.
     * @param relatedCompound
     *            The relatedCompound serving as key to the layerCompoundContents.
     * @param graphKey
     *            The key representing the layeredGraph itself.
     */
    private void insertRelatedCompound(
            final HashMap<LNode, LinkedList<LNode>> layerCompoundContents, final LNode node,
            final LNode relatedCompound, final LNode graphKey) {
        LNode key = relatedCompound;
        if (key == null) {
            key = graphKey;
        }
        LinkedList<LNode> nodeList;
        if (layerCompoundContents.containsKey(key)) {
            nodeList = layerCompoundContents.get(key);
        } else {
            nodeList = new LinkedList<LNode>();
            layerCompoundContents.put(key, nodeList);
        }
        if (!(nodeList.contains(node))) {
            nodeList.addLast(node);
        }
    }

    /**
     * Applies the order given by the acyclic subgraphOrderingGraph to the nodes of the
     * layeredGraph.
     * 
     * @param layeredGraph
     *            Graph, to which the node ordering is to be applied.
     * @param subgraphOrderingGraph
     *            The subgraphOrderingGraph. Every LayeredGraph in the HashMap must be acyclic.
     * @param graphKey
     *            The LNode serving as key representing the layeredGraph in the
     *            subgraphOrderingGraph.
     * @param elemMap
     *            The element-map mapping the original KGraph elements to LGraph elements.
     */
    private void applyOrder(final LGraph layeredGraph,
            final HashMap<LNode, LGraph> subgraphOrderingGraph, final LNode graphKey,
            final HashMap<KGraphElement, LGraphElement> elemMap) {

        HashMap<Layer, LinkedList<LNode>> layerOrders = new HashMap<Layer, LinkedList<LNode>>();
        for (Layer layer : reorderedLayers.keySet()) {
            LinkedList<LNode> layerOrder = new LinkedList<LNode>();
            recursiveApplyLayerOrder(layer, graphKey, layeredGraph, subgraphOrderingGraph,
                    layerOrder, elemMap);
            layerOrders.put(layer, layerOrder);
        }
        // Resort the layers.
        for (Map.Entry<Layer, LinkedList<LNode>> entry : layerOrders.entrySet()) {
            List<LNode> nodes = entry.getKey().getNodes();
            int sizeNodes = nodes.size();
            LinkedList<LNode> orderNodes = entry.getValue();
            int sizeOrderNodes = orderNodes.size();
            assert (sizeNodes == sizeOrderNodes);
            for (int i = 0; i < sizeNodes; i++) {
                nodes.remove(0);
            }
            assert (nodes.isEmpty());
            for (LNode node : orderNodes) {
                nodes.add(node);
            }
            assert (nodes.size() == sizeOrderNodes);
        }
    }

    /**
     * Applies the node order given by the subgraphOrderingGraph to one given layer. The
     * subgraphOrderingGraph passed to this method has to be acyclic and non-layered.
     * 
     * @param layer
     *            The layer to be ordered.
     * @param key
     *            The key-LNode designating the actual orderingGraph-component processed.
     * @param layeredGraph
     *            The layeredGraph to be laid out.
     * @param subgraphOrderingGraph
     *            A subgraph ordering graph without cycles.
     * @param layerOrder
     *            The list, in which the node order for the layer is stored.
     * @param elemMap
     *            The element map mapping original KGraph elements to LGraph elements.
     */
    private void recursiveApplyLayerOrder(final Layer layer, final LNode key,
            final LGraph layeredGraph,
            final HashMap<LNode, LGraph> subgraphOrderingGraph,
            final LinkedList<LNode> layerOrder, final HashMap<KGraphElement, LGraphElement> elemMap) {

        LinkedList<LNode> componentOrder = orderedLists.get(key);

        // There may be no component for the key in the subgraphOrderingGraph. A child compound node
        // of this node has to be handled nevertheless.
        if (componentOrder == null) {
            LinkedList<LNode> childrenList = compoundChildrenLists.get(layer).get(key);
            // childrenlist may be null, if the node is either a leave node or a compound node
            // without relevance for this layer (not spanning this layer)
            if ((childrenList != null)) {
                for (LNode child : childrenList) {
                    // leave out the self-referencing entries
                    if (child != key) {
                        if (compoundChildrenLists.get(layer).containsKey(child)) {
                            recursiveApplyLayerOrder(layer, child, layeredGraph,
                                    subgraphOrderingGraph, layerOrder, elemMap);
                        }
                    }
                }
            }
        } else {
            // If there is a component in the subgraphOrderingGraph for this key, stick to the
            // topological order of the children in the component in handling them.
            componentOrder = merge(componentOrder, compoundChildrenLists.get(layer).get(key));

            Iterator<LNode> orderIterator = componentOrder.iterator();
            while (orderIterator.hasNext()) {
                LNode currentNode = orderIterator.next();
                if (currentNode != key) {
                    recursiveApplyLayerOrder(layer, currentNode, layeredGraph,
                            subgraphOrderingGraph, layerOrder, elemMap);
                }
            }
        }

        // Add assigned nodes for the current key to the order.
        LinkedList<LNode> assignedNodes = reorderedLayers.get(layer).get(key);
        if (assignedNodes != null) {
            for (LNode assignedNode : assignedNodes) {
                if (!layerOrder.contains(assignedNode)) {
                    layerOrder.add(assignedNode);
                }
            }
        }
    }

    /**
     * For a given layer and a given compound node, this method takes two orders of the compound
     * node's children. The order of appearance in the layer(layerOrder) and the order given by the
     * subgraph ordering graph(componentOrder). The list returned must respect the order given by
     * componentOrder, but nodes that were never not inserted into the subgraph ordering graph are
     * added.
     * 
     * 
     * @param componentOrder
     *            compound children representatives in the order given by the subgraph ordering
     *            graph.
     * @param layerOrder
     *            compound children in the order of appearance in the layer. Order that was
     *            determined by the node ordering phase - uncorrected.
     * @return Returns a list of all children of one compound node that are present in the layer.
     *         The order respects the order given by the subgraph ordering graph.
     */
    private LinkedList<LNode> merge(final LinkedList<LNode> componentOrder,
            final LinkedList<LNode> layerOrder) {
        LinkedList<LNode> retList = new LinkedList<LNode>();
        if (layerOrder.isEmpty()) {
            // retList is to contain original nodes, not their representatives
            // in the subgraph ordering graph.
            for (LNode lnode : componentOrder) {
                LNode origin = (LNode) lnode.getProperty(Properties.ORIGIN);
                retList.add(origin);
            }
            return retList;
        }
        // Get componentOrder without nodes that are not represented in the layer. Nodes as originals, 
        // not representatives from subgraph ordering graph.
        LinkedList<LNode> trimmedComponentOrder = new LinkedList<LNode>();
        for (LNode repNode : componentOrder) {
            LNode origin = (LNode) repNode.getProperty(Properties.ORIGIN);
            if (layerOrder.contains(origin)) {
                trimmedComponentOrder.add(origin);
            }
        }
        int i = 0;
        // resort the layer order according to trimmedComponentOrder
        for (LNode node : layerOrder) {
            if (!trimmedComponentOrder.contains(node)) {
                retList.add(node);
            } else {
                assert (i < trimmedComponentOrder.size());
                retList.add(trimmedComponentOrder.get(i));
                // This element of trimmedComponentOrder is used up. Go to the next. 
                i++;
            }
        }
        assert (retList.size() == layerOrder.size());
        return retList;
    }

    /**
     * Creates a node list representing an topological sorting of the nodes for a layered graph. The
     * graph passed to this method has to be acyclic and non-layered.
     * 
     * @param graph
     *            Acyclic layeredGraph - all nodes in the layerlessNodes-list.
     * @return List containing all nodes of the layered graph in an topological order.
     */

    private LinkedList<LNode> graphToList(final LGraph graph) {
        boolean acyclic = true;
        LinkedList<LNode> retList = new LinkedList<LNode>();
        List<LNode> nodes = graph.getLayerlessNodes();
        LinkedList<LNode> sources = new LinkedList<LNode>();
        // Collect the graph's sources.
        for (LNode node : nodes) {
            // if node has no incoming edges
            if (!node.getIncomingEdges().iterator().hasNext()) {
                sources.add(node);
            }
        }
        // Iterate over the sources.
        while (!sources.isEmpty()) {
            LNode currentSource = sources.getFirst();
            sources.removeFirst();
            // Add source to sorting.
            retList.add(currentSource);
            Iterator<LEdge> outEdgesIterator = currentSource.getOutgoingEdges().iterator();
            // Collect the source's targets.
            HashSet<LNode> targets = new HashSet<LNode>();
            while (outEdgesIterator.hasNext()) {
                LEdge edge = outEdgesIterator.next();
                LNode edgeTarget = edge.getTarget().getNode();
                if (!(targets.contains(edgeTarget))) {
                    targets.add(edgeTarget);
                }
            }
            // Iterate the targets.
            for (LNode target : targets) {
                boolean isNewSource = true;
                Iterator<LEdge> inEdgesIterator = target.getIncomingEdges().iterator();
                LinkedList<LEdge> removableEdges = new LinkedList<LEdge>();
                // If they have no further incoming edges, add them to the source list.
                while (inEdgesIterator.hasNext()) {
                    LEdge edge = inEdgesIterator.next();
                    LNode source = edge.getSource().getNode();
                    if (source == currentSource) {
                        removableEdges.add(edge);
                    } else {
                        isNewSource = false;
                    }
                }
                // Remove edges already visited.
                int edgesSize = removableEdges.size();
                for (int i = 0; i < edgesSize; i++) {
                    LEdge edge = removableEdges.removeFirst();
                    edge.getSource().getNode().getPorts().remove(edge.getSource());
                    edge.getTarget().getNode().getPorts().remove(edge.getTarget());
                }
                if (isNewSource) {
                    sources.add(target);
                }
            }
        }
        // There should be no more edges left. If otherwise, there is at least one cycle in the graph 
        // passed as parameter.
        for (LNode node : nodes) {
            for (LPort port : node.getPorts()) {
                if (port.getConnectedEdges().iterator().hasNext()) {
                    acyclic = false;
                }
            }
        }
        // This method will not work, if the graph is cyclic.
        assert acyclic;
        return retList;
    }

    /**
     * Checks, if a representative for the LNode currentRep is already inserted into the
     * corresponding component graph of the subgraph-ordering-graph. Makes the insertion, if not.
     * 
     * @param node
     *            The LNode to be represented.
     * @param layerlessNodes
     *            The list of layerless nodes of the corresponding component graph.
     * @param insertedNodes
     *            The Hashmap, in which the insertion of Nodes into the subGraphOrderingGraph is
     *            documented.
     * @return Returns the representative of the currentNode in the component graph. It may be
     *         freshly created by this method.
     */
    private LNode getNodeCopy(final LNode node, final List<LNode> layerlessNodes,
            final HashMap<LNode, LNode> insertedNodes) {
        LNode retNode;
        if (insertedNodes.containsKey(node)) {
            // Node already has a representative.
            retNode = insertedNodes.get(node);
        } else {
            // A representative has to be created.
            retNode = new LNode(node.getLayer().getGraph());
            retNode.setProperty(Properties.ORIGIN, node);
            // retNode.copyProperties(node);
            insertedNodes.put(node, retNode);
            layerlessNodes.add(retNode);
        }
        return retNode;
    }
}
