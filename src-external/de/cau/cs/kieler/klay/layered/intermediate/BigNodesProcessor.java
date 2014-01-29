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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * The main class of the big node handler component. It offers a method to
 * split wide nodes of the given graph into smaller, equal-sized ones to allow the former wide node
 * to be assigned to multiple consecutive layers. After layerer execution, the thereby determined
 * layer assignment may be esthetically optimized by invoking {@code segmentateLayering()}, which
 * segmentates the layering, i.e. two nodes will be be placed into layers, so that they are not
 * disjunct regarding the layers they are assigned to, and the narrower of the two nodes is not
 * assigned to all layers, the wider nodes is also placed in unless there exists a node, that covers
 * all layers, the two nodes are assigned to. For more information, see Philipp Doehring:
 * <em>Algorithmen zur Layerzuweisung</em>, Bachelor Thesis, 2010.
 * 
 * FIXME this doesn't work yet, since a postprocessor is needed that reverts the changes made here
 * @author pdo
 * @kieler.design 2012-08-10 chsch grh
 * 
 * @deprecated replaced by BigNodes*Processor
 */
public final class BigNodesProcessor implements ILayoutProcessor {

    // ================================== Attributes ==============================================

    /**
     * The layered graph, all methods in this class operate on.
     * 
     * @see de.cau.cs.kieler.klay.layered.graph.LGraph LayeredGraph
     */
    private LGraph layeredGraph;

    /**
     * A {@code Collection} containing all nodes in the graph to layer.
     * 
     * @see de.cau.cs.kieler.klay.layered.graph.LNode LNode
     */
    private Collection<LNode> nodes;

    /**
     * The width of each node in layers before wide nodes have been split. Note that all wide nodes
     * in the graph can be easily identified by this attribute, since their {@code width} is always
     * greater than {@code 1}, whereas the {@code width} of all other nodes remains {@code 1}.
     */
    private int[] width;

    /**
     * The index of the layer, each node is currently assigned to. The lesser the index, the more to
     * the left will a node be located in the final drawing of the graph.
     */
    private int[] layer;

    /**
     * The length of the longest path in the graph from a source to sink node, which is defined as
     * the number of nodes in the path minus one.
     */
    private int longestPath;

    /**
     * A map, in which for every node in the graph its ID will be saved.
     */
    private LinkedHashMap<LNode, Integer> nodeIDs;


    // ============================== Big-Node-Handling Algorithm ================================

    /**
     * Main method for the Big-Node-Handler. It splits all wide nodes in the graph into smaller,
     * equal sized nodes by insertion of dummy nodes. All incoming edges incident to the original
     * wide node will remain on the node, but outgoing back edges will be moved to the rightmost
     * inserted dummy node. Note that after a wide node has been split, its width in layers stored
     * in {@code width} will not be updated, i.e. it keeps its value of 1 plus the number of
     * inserted dummy nodes to split the specific wide node.
     * 
     * @param theLayeredGraph
     *            the layered graph to put all nodes into
     * @param monitor
     *            the progress monitor
     * 
     * @see de.cau.cs.kieler.klay.layered.intermediate.BigNodesProcessor#width width
     */
    public void process(final LGraph theLayeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Big nodes processing", 1);

        // initialize attributes
        nodes = theLayeredGraph.getLayerlessNodes();
        layeredGraph = theLayeredGraph;

        // re-index nodes
        int counter = 0;
        for (LNode node : nodes) {
            node.id = counter++;
        }
        // the list of dummy nodes to be inserted into the graph
        LinkedList<LNode> dummyNodes = new LinkedList<LNode>();
        // the object spacing in the drawn graph
        double minSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING);
        // the ID for the most recently created dummy node
        int dummyID = nodes.size();
        // the list of ports to reassign to the wide node's rightmost dummy node
        LinkedList<LPort> ports = null;

        // Compute width of narrowest node
        double minWidth = Float.MAX_VALUE;
        for (LNode node : nodes) {
            if (node.getSize().x < minWidth) {
                minWidth = node.getSize().x;
            }
        }
        // initialize width array
        if (width == null || width.length < nodes.size()) {
            width = new int[nodes.size()];
        }
        Arrays.fill(width, 1);
        minWidth /= 2;
        // determine width in layers of each node
        double threshold;
        for (LNode node : nodes) {
            threshold = ((2 * minWidth) + minSpacing);
            while (threshold <= node.getSize().x) {
                width[node.id]++;
                threshold += (minWidth + minSpacing);
            }

            // all nodes in the segment get the same width (temporarily)
            node.getSize().x = minWidth;
            if (width[node.id] > 1) {
                // save outgoing ports of wide node to reassign them later
                ports = new LinkedList<LPort>();
                for (LPort port : node.getPorts()) {
                    // TODO This originally iterated over output ports; check if this replacement works.
                    if (!port.getOutgoingEdges().isEmpty()) {
                        ports.add(port);
                    }
                }
                // expand node by one dummy node per iteration
                for (int d = 1; d < width[node.id]; d++) {
                    // create new dummy node
                    LNode dummy = new LNode(layeredGraph);
                    dummy.id = dummyID++;
                    // set size
                    dummy.getSize().y = node.getSize().y;
                    dummy.getSize().x = minWidth;
                    // add ports to connect it with the previous node
                    LPort outPort = new LPort(layeredGraph);
                    LPort inPort = new LPort(layeredGraph);
                    outPort.setNode((d == 1) ? node : dummyNodes.getLast());
                    inPort.setNode(dummy);
                    // add edge to connect it with the previous node
                    LEdge edge = new LEdge(layeredGraph);
                    edge.setSource(outPort);
                    edge.setTarget(inPort);
                    // fixation edges should be handled as a straight line
                    edge.setProperty(Properties.PRIORITY, 1);
                    // add new dummy node to list of all dummy nodes
                    dummyNodes.add(dummy);
                }
                // reassign saved outgoing ports to rightmost dummy node
                for (LPort port : ports) {
                    port.setNode(dummyNodes.getLast());
                }
            }
        }
        // add all dummy nodes to the nodes of the graph
        nodes.addAll(dummyNodes);
        // save current node IDs to restore them in 'correctLayering()'
        nodeIDs = new LinkedHashMap<LNode, Integer>(nodes.size());
        for (LNode node : nodes) {
            nodeIDs.put(node, node.id);
        }
        
        monitor.done();
    }

    /**
     * Main method of the Big-Node-Splitter. It segmentates the layering after layerer execution by
     * shifting single nodes to the right (i.e. placing them into a layer with a higher ID). Two
     * wide nodes (i.e. nodes with a {@code width} > 1) will be be placed into layers, so that they
     * are not disjunct regarding the layers, they are assigned to, and the narrower of the two
     * nodes is not assigned to all layers, the wider nodes is also placed in unless there exists a
     * node, that covers all layers, the two nodes are assigned to. Note that before invoking this
     * method, the {@code splitWideNodes()} -procedure has to be called first. This will be checked
     * if not or the set of nodes has been changed since that invocation.
     * 
     * TODO if this is to be supported again, it must be moved into an own processor
     * @see splitWideNodes(LayeredGraph)
     */
    public void segmentateLayering() {
        assert nodeIDs != null;
        
        // restore previous node indices
        Integer id = null;
        assert nodes.size() == nodeIDs.size();
        for (LNode node : nodes) {
            id = nodeIDs.get(node);
            assert id != null;
            node.id = id;
        }
        // get current layer assignment
        convertLayering();
        // create buckets for wide nodes
        ArrayList<LinkedList<LNode>> buckets = new ArrayList<LinkedList<LNode>>(longestPath << 1);
        for (int i = 0; i < (longestPath << 1); i++) {
            buckets.add(new LinkedList<LNode>());
        }
        // put all wide nodes into their bucket
        LinkedList<LNode> bucket = null;
        for (LNode node : nodes) {
            if (node.id < width.length && width[node.id] > 1) {
                // node is a wide node
                bucket = buckets.get(layer[node.id]);
                // put widest node at the top
                if (bucket.isEmpty() || width[bucket.getFirst().id] < width[node.id]) {
                    bucket.addFirst(node);
                } else {
                    bucket.addLast(node);
                }
            }
        }
        // correct layering by shifting violating nodes to the right
        int curIndex = 0, targetIndex = -1, restrIndex = -1;
        LinkedList<LNode> curBucket, targetBucket;
        LNode curNode = null;
        Iterator<LNode> iterator = null;
        while (curIndex < buckets.size()) {
            curBucket = buckets.get(curIndex);
            // check all wide nodes for layering violations
            if (!curBucket.isEmpty()) {
                // update restriction index
                if (restrIndex < curIndex) {
                    restrIndex = curIndex + width[curBucket.getFirst().id] - 1;
                }
                iterator = curBucket.iterator();
                while (iterator.hasNext()) {
                    curNode = iterator.next();
                    targetIndex = -1;
                    if (curIndex + width[curNode.id] - 1 > restrIndex) {
                        // node violates the layering, put it into a fitting layer
                        targetIndex = restrIndex + 1;
                    } else if (layer[curNode.id] > curIndex) {
                        // node is placed in wrong bucket
                        targetIndex = layer[curNode.id];
                    }
                    if (targetIndex != -1) {
                        // add more buckets, if necessary
                        while (targetIndex >= buckets.size()) {
                            buckets.add(new LinkedList<LNode>());
                        }
                        // put node into the new bucket
                        targetBucket = buckets.get(targetIndex);
                        if (targetBucket.isEmpty()
                                || width[targetBucket.getFirst().id] < width[curNode.id]) {
                            targetBucket.addFirst(curNode);
                        } else {
                            targetBucket.addLast(curNode);
                        }
                        // remove node from current bucket
                        iterator.remove();
                        // update leftmost assignable layer of each connected node
                        minimalLayer(curNode, targetIndex);
                    }
                }
            }
            curIndex++;
        }
        // put all nodes into their new layers
        putNodes();
    }

    /**
     * Helper method for the Big-Node-Handler. It puts all nodes reachable by a path beginning with
     * the input node traversing outgoing edges only into the their floor layers, i.e. the layers
     * with a lowest possible index, where the layer assignment remains feasible.
     * 
     * @param node
     *            the root of the DFS-subtree
     * @param start
     *            The index of the layer to put the given input node into
     */
    private void minimalLayer(final LNode node, final int start) {
        // traverse edges from source to target
        layer[node.id] = Math.max(layer[node.id], start);
        for (LPort port : node.getPorts()) {
            for (LEdge edge : port.getOutgoingEdges()) {
                minimalLayer(edge.getTarget().getNode(), start + 1);
            }
        }
        // update longest path
        longestPath = Math.max(longestPath, layer[node.id]);
    }

    /**
     * Helper method for the Big-Node-Handler. It re-indexes all layers contained in
     * {@code layeredGraph}, determines the current longest path in the graph and retrieves the
     * index of the layer, each node of the graph is assigned to and stores this value in
     * {@code layer}. If this array does not exist or its size to small to store the value of each
     * node, a (new) instance of this attribute will be created. Otherwise, the old instance will be
     * reused. After this step is done, all layers will be deleted from {@code layeredGraph}.
     * 
     * @see de.cau.cs.kieler.klay.layered.intermediate.BigNodesProcessor#layer layer
     * @see de.cau.cs.kieler.klay.layered.intermediate.BigNodesProcessor#longestPath longestPath
     */
    private void convertLayering() {
        // initialize layer attribute
        if (layer == null || layer.length < nodes.size()) {
            layer = new int[nodes.size()];
        }
        // re-index layers
        int counter = 0;
        List<Layer> layers = layeredGraph.getLayers();
        for (Layer theLayer : layers) {
            theLayer.id = counter++;
        }
        // get layer assignment of each node
        for (LNode node : nodes) {
            layer[node.id] = node.getLayer().id;
        }
        // save length of longest path
        longestPath = layers.size() - 1;
        // clear layers
        layers.clear();
    }

    /**
     * Helper method for the Big-Node-Handler. It puts all nodes of the graph into their assigned
     * layers in the layered graph ({@code layeredGraph}) as stated in {@code layer}. Since at this
     * stage, the graph does not contain any layers, not even empty ones, all necessary layers will
     * be added to it by this method. After execution, each layer is assigned at least one node.
     * 
     * @param node
     *            the node to put into the layered graph
     * 
     * @see de.cau.cs.kieler.klay.layered.intermediate.BigNodesProcessor#layer layer
     * @see de.cau.cs.kieler.klay.layered.intermediate.BigNodesProcessor#layeredGraph layeredGraph
     */
    private void putNodes() {
        List<Layer> layers = layeredGraph.getLayers();
        // add additional layers to match required amount
        while (longestPath-- >= 0) {
            layers.add(new Layer(layeredGraph));
        }
        // put nodes into their assigned layers
        for (LNode node : nodes) {
            node.setLayer(layers.get(layer[node.id]));
        }
        // remove empty layers
        Iterator<Layer> iterator = layers.iterator();
        Layer curLayer = null;
        while (iterator.hasNext()) {
            curLayer = iterator.next();
            if (curLayer.getNodes().isEmpty()) {
                iterator.remove();
            }
        }
    }

}
