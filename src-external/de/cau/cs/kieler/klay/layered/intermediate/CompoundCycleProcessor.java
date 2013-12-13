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
import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.Util;
import de.cau.cs.kieler.klay.layered.graph.LGraphElement;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.p1cycles.GreedyCycleBreaker;
import de.cau.cs.kieler.klay.layered.properties.EdgeType;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Detects cyclic dependencies between compound nodes and reverts edges to remove them before simple
 * cycle removal and layering.
 * 
 * <dl>
 * <dt>Precondition:</dt>
 * <dd>A layered graph. All nodes have the ORIGIN-Property set to the KNode in the original graph
 * that is represented by them.</dd>
 * <dt>Postcondition:</dt>
 * <dd>The layered graph contains no more cyclic dependencies.</dd>
 * <dt>Slots:</dt>
 * <dd>Before phase 1.</dd>
 * <dt>Same-slot dependencies:</dt>
 * <dd>none.</dd>
 * </dl>
 * 
 * @author ima
 * @kieler.design 2012-08-10 chsch grh
 */
public final class CompoundCycleProcessor implements ILayoutProcessor {

    /**
     * Store information about inserted dummy edges.
     */
    private final HashMap<LEdge, LEdge> dummyEdgeMap = new HashMap<LEdge, LEdge>();

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Revert edges to remove cyclic dependencies between compound nodes", 1);

        // Represent the cyclic dependencies of compound nodes in a cycle-removal-graph
        LGraph cycleRemovalGraph = new LGraph(layeredGraph);
        cycleRemovalGraph.setProperty(Properties.RANDOM,
                layeredGraph.getProperty(Properties.RANDOM));
        List<LNode> cycleRemovalNodes = cycleRemovalGraph.getLayerlessNodes();
        HashMap<LNode, LNode> insertedNodes = new HashMap<LNode, LNode>();

        LinkedList<LEdge> toDescendantEdges = new LinkedList<LEdge>();

        // For each edge, walk up the nesting tree of the compound graph until a pair of ancestors
        // of source and target node is found that shares the same parent. Propagating dependencies
        // keeps the cycleRemovalGraph simple. Insert representatives of the resulting nodes into
        // the cycleRemovalGraph as well as a representative for the edge - connecting them.
        for (LNode lnode : layeredGraph.getLayerlessNodes()) {
            for (LEdge edge : lnode.getOutgoingEdges()) {

                // only normal edges need to be regarded
                EdgeType edgeType = edge.getProperty(Properties.EDGE_TYPE);
                if (edgeType == EdgeType.NORMAL) {
                    boolean isDescendantEdge = false;

                    LNode sourceNode = edge.getSource().getNode();
                    LNode targetNode = edge.getTarget().getNode();

                    if ((Util.isDescendant(sourceNode, targetNode))
                            || (Util.isDescendant(targetNode, sourceNode))) {
                        isDescendantEdge = true;
                    }

                    LGraphElement sourceParent = Util.getParent(sourceNode);
                    LGraphElement targetParent = Util.getParent(targetNode);
                    LNode currentSource = sourceNode;
                    LNode currentTarget = targetNode;
                    LGraphElement currentSourceAncestor = sourceParent;
                    LGraphElement currentTargetAncestor = targetParent;

                    // Edges leading from ports of compound nodes to their descendants are reverted
                    // in case the port has incoming edges. Otherwise the layering for ports with
                    // incoming and outgoing descendant edges could lead to faulty layer
                    // assignments.
                    NodeType sourceNType = sourceNode.getProperty(Properties.NODE_TYPE);
                    if ((sourceNType == NodeType.LOWER_COMPOUND_PORT)
                            || sourceNType == NodeType.UPPER_COMPOUND_PORT) {
                        if (Util.isDescendant(targetNode,
                                sourceNode.getProperty(Properties.COMPOUND_NODE))) {
                            List<LEdge> portIncomingEdges = edge.getSource().getIncomingEdges();
                            if (!portIncomingEdges.isEmpty()) {
                                boolean descendantIncoming = false;
                                for (LEdge ledge : portIncomingEdges) {
                                    if (Util.isDescendant(
                                            ledge.getSource().getNode(),
                                            ledge.getTarget().getNode()
                                                    .getProperty(Properties.COMPOUND_NODE))) {
                                        descendantIncoming = true;
                                    }
                                    if (descendantIncoming) {
                                        toDescendantEdges.add(edge);
                                    }
                                }

                            }
                        }
                    }

                    // Establishes the edge an adjacency of two compound nodes (source and target
                    // are one of the compound nodes or any of it's descendants)?
                    LinkedList<LNode> sourceChildren = Util.getChildren(sourceNode);
                    LinkedList<LNode> targetChildren = Util.getChildren(targetNode);
                    if ((currentSourceAncestor != currentTargetAncestor)
                            || ((!Util.getChildren(sourceNode).isEmpty()) && (!Util.getChildren(
                                    targetNode).isEmpty()))
                            || (sourceChildren.isEmpty() && !targetChildren.isEmpty())
                            || (!sourceChildren.isEmpty() && targetChildren.isEmpty())) {

                        int depthSource = sourceNode.getProperty(Properties.DEPTH);
                        int depthTarget = targetNode.getProperty(Properties.DEPTH);

                        // If source and target differ in depth in the nesting tree, crawl up the
                        // nesting tree on the deep side to reach even depth level
                        if (depthSource != depthTarget) {
                            for (int i = depthSource; i > depthTarget; i--) {
                                LGraphElement sourceNextParent = Util.getParent(currentSource);
                                // This should stop at the latest, when a node of depth 1 is
                                // reached;
                                assert (sourceNextParent instanceof LNode);
                                currentSource = (LNode) sourceNextParent;
                            }
                            for (int j = depthTarget; j > depthSource; j--) {
                                LGraphElement targetNextParent = Util.getParent(currentTarget);
                                // This should stop at the latest, when a node of depth 1 is
                                // reached;
                                assert (targetNextParent instanceof LNode);
                                currentTarget = (LNode) targetNextParent;
                            }
                        }

                        if (currentSource != currentTarget) {
                            // Walk up the nesting tree from both sides, until nodes have the same
                            // parent.
                            currentSourceAncestor = Util.getParent(currentSource);
                            currentTargetAncestor = Util.getParent(currentTarget);
                            while (currentSourceAncestor != currentTargetAncestor) {
                                // The loop should stop at the latest, when Nodes of depth 1 are
                                // reached, whose parent is the layeredGraph
                                assert (currentSourceAncestor instanceof LNode);
                                assert (currentTargetAncestor instanceof LNode);
                                currentSource = (LNode) currentSourceAncestor;
                                currentTarget = (LNode) currentTargetAncestor;
                                currentSourceAncestor = Util.getParent(currentSource);
                                currentTargetAncestor = Util.getParent(currentTarget);
                            }

                            NodeType sourceNodeType = currentSource
                                    .getProperty(Properties.NODE_TYPE);
                            NodeType targetNodeType = currentTarget
                                    .getProperty(Properties.NODE_TYPE);

                            if (!((sourceNodeType == NodeType.NORMAL) 
                                    || (sourceNodeType == NodeType.UPPER_COMPOUND_BORDER))) {
                                currentSource = currentSource.getProperty(Properties.COMPOUND_NODE);
                            }

                            if (!((targetNodeType == NodeType.NORMAL) 
                                    || (targetNodeType == NodeType.UPPER_COMPOUND_BORDER))) {
                                currentTarget = currentTarget.getProperty(Properties.COMPOUND_NODE);
                            }

                            insertCycleNode(layeredGraph, currentSource, insertedNodes,
                                    cycleRemovalNodes);
                            insertCycleNode(layeredGraph, currentTarget, insertedNodes,
                                    cycleRemovalNodes);

                            // While at it, add dummy edges to enhance the layering of the dependent
                            // nodes. Remember, which edge lead to the insertion of which dummy
                            // edge,
                            // because dummy edges inserted for an edge that is reverted later on
                            // have
                            // to be removed again
                            if (!isDescendantEdge) {
                                NodeType nodeTypeDummySource = currentSource
                                        .getProperty(Properties.NODE_TYPE);
                                if (nodeTypeDummySource == NodeType.NORMAL) {
                                    // leave node
                                    insertDummyEdge(layeredGraph, currentTarget, currentSource, edge);
                                } else {
                                    // compound node, lower border and port dummy nodes have to be
                                    // found
                                    for (LNode node : layeredGraph.getLayerlessNodes()) {
                                        if (((node.getProperty(Properties.NODE_TYPE) 
                                                == NodeType.LOWER_COMPOUND_BORDER) 
                                                || (node.getProperty(Properties.NODE_TYPE) 
                                                        == NodeType.LOWER_COMPOUND_PORT))
                                                && (node.getProperty(Properties.COMPOUND_NODE) 
                                                        == currentSource)) {
                                            insertDummyEdge(layeredGraph, currentTarget, node, edge);
                                        }
                                    }
                                }
                            }

                            LEdge cycleGraphEdge = new LEdge(layeredGraph);
                            cycleGraphEdge.setProperty(Properties.ORIGIN, edge);
                            LPort cycleSourcePort = new LPort(layeredGraph);
                            LPort cycleTargetPort = new LPort(layeredGraph);
                            cycleGraphEdge.setSource(cycleSourcePort);
                            cycleGraphEdge.setTarget(cycleTargetPort);
                            cycleSourcePort.setNode(insertedNodes.get(currentSource));
                            cycleTargetPort.setNode(insertedNodes.get(currentTarget));
                        }

                    }
                }
            }
        }

        reverseCyclicEdges(layeredGraph, cycleRemovalGraph, monitor);

        int toDescendantSize = toDescendantEdges.size();
        for (int i = 0; i < toDescendantSize; i++) {
            LEdge edge = toDescendantEdges.get(i);
            if (!edge.getProperty(Properties.REVERSED)) {
                // LPort source = edge.getSource();
                // LPort target = edge.getTarget();
                // edge.setSource(target);
                // edge.setTarget(source);
                // edge.setProperty(Properties.REVERSED, true);
                edge.reverse(layeredGraph, true);
            }
        }

        dummyEdgeMap.clear();
        monitor.done();
    }

    /**
     * Inserts dummy edge for the layering phase.
     * 
     * @param layeredGraph
     *            the layered graph
     * @param target
     *            the LNode that is to be the edge's target.
     * @param source
     *            the LNode that is to be the edge's source.
     * @param edge
     *            the edge, for which this dummy edge is inserted (edge leading to the requirement,
     *            one node should be placed before the other in layering)
     */
    private void insertDummyEdge(final LGraph layeredGraph, final LNode target, final LNode source,
            final LEdge edge) {
        LEdge dummyEdge = new LEdge(layeredGraph);
        dummyEdgeMap.put(edge, dummyEdge);
        dummyEdge.setProperty(Properties.EDGE_TYPE, EdgeType.COMPOUND_DUMMY);
        LPort dummyPortSource = new LPort(layeredGraph);
        LPort dummyPortTarget = new LPort(layeredGraph);
        dummyEdge.setSource(dummyPortSource);
        dummyEdge.setTarget(dummyPortTarget);
        dummyPortTarget.setNode(target);
        dummyPortSource.setNode(source);
    }

    /**
     * Inserts a representative for a node into the cycleRemovalGraph, if it has none already.
     * 
     * @param layeredGraph
     *            the layered graph
     * @param node
     *            LNode to be represented.
     * @param insertedNodes
     *            Nodes inserted before.
     * @param cycleRemovalNodes
     *            The layer-less nodes of the cycleRemoval graph.
     */
    private void insertCycleNode(final LGraph layeredGraph, final LNode node,
            final HashMap<LNode, LNode> insertedNodes, final List<LNode> cycleRemovalNodes) {
        if (!insertedNodes.containsKey(node)) {
            LNode cycleGraphNode = new LNode(layeredGraph);
            cycleGraphNode.setProperty(Properties.ORIGIN, node);
            insertedNodes.put(node, cycleGraphNode);
            cycleRemovalNodes.add(cycleGraphNode);
        }
    }

    /**
     * Removes cyclic dependencies between compound nodes by reverting edges.
     * 
     * @param layeredGraph
     *            the layered graph
     * @param cycleRemovalGraph
     *            A layered graph representing the cyclic dependencies of the layeredGraph.
     * @param monitor
     *            the current progress monitor
     */
    private void reverseCyclicEdges(final LGraph layeredGraph,
            final LGraph cycleRemovalGraph, final IKielerProgressMonitor monitor) {

        LinkedList<LEdge> edgesToReverse = new LinkedList<LEdge>();

        // At this point, a cycle breaking algorithm is needed. At the moment, the greedy cycle
        // breaker is used.
        GreedyCycleBreaker cycleBreaker = new GreedyCycleBreaker();
        cycleBreaker.process(cycleRemovalGraph, monitor.subTask(1.0f / 2));

        for (LNode lnode : cycleRemovalGraph.getLayerlessNodes()) {
            for (LEdge ledge : lnode.getOutgoingEdges()) {
                if (ledge.getProperty(Properties.REVERSED)) {
                    edgesToReverse.add((LEdge) ledge.getProperty(Properties.ORIGIN));
                }
            }
        }

        reverseEdges(edgesToReverse, layeredGraph);
    }

    /**
     * Reverts edges of a given list of edges.
     * 
     * @param edgeList
     *            The list of edges to be reversed.
     * @param layeredGraph
     */
    private void reverseEdges(final LinkedList<LEdge> edgeList, final LGraph layeredGraph) {
        for (int i = 0; i < edgeList.size(); i++) {
            LEdge edge = edgeList.get(i);
            LPort source = edge.getSource();
            LPort target = edge.getTarget();
            LNode sourceNode = source.getNode();
            LNode targetNode = target.getNode();
            NodeType sourceNodeType = sourceNode.getProperty(Properties.NODE_TYPE);
            NodeType targetNodeType = targetNode.getProperty(Properties.NODE_TYPE);

            LPort newSource = edge.getTarget();
            LPort newTarget = edge.getSource();

            // Änderung
            if (targetNodeType != NodeType.NORMAL) {
                newSource = getOppositePort(target, layeredGraph);
            }

            if (sourceNodeType != NodeType.NORMAL) {
                newTarget = getOppositePort(source, layeredGraph);
            }

            edge.setSource(newSource);
            edge.setTarget(newTarget);
            edge.setProperty(Properties.REVERSED, true);

            // Original port dummy nodes are not needed any more. Remove them. Prepare removing them
            // by removing all connected edges (which will be only dummy edges). removableEdges-List
            // is used to avoid concurrent modification exception.
            LinkedList<LEdge> removableEdges = new LinkedList<LEdge>();
            if (sourceNodeType == NodeType.LOWER_COMPOUND_PORT) {
                for (LEdge ledge : sourceNode.getConnectedEdges()) {
                    removableEdges.add(ledge);
                }
                for (LEdge ledge : removableEdges) {
                    ledge.getTarget().getIncomingEdges().remove(ledge);
                    ledge.getSource().getOutgoingEdges().remove(ledge);
                }
                layeredGraph.getLayerlessNodes().remove(sourceNode);
            }
            if (targetNodeType == NodeType.UPPER_COMPOUND_PORT) {
                for (LEdge ledge : targetNode.getConnectedEdges()) {
                    removableEdges.add(ledge);
                }
                for (LEdge ledge : removableEdges) {
                    ledge.getTarget().getIncomingEdges().remove(ledge);
                    ledge.getSource().getOutgoingEdges().remove(ledge);
                }
                layeredGraph.getLayerlessNodes().remove(targetNode);
            }
            LEdge dummyEdge = dummyEdgeMap.get(edge);
            if (dummyEdge != null) {
                dummyEdge.getSource().getOutgoingEdges().remove(dummyEdge);
                dummyEdge.getTarget().getIncomingEdges().remove(dummyEdge);
            }
        }
    }

    /**
     * Returns the port an edge ending or starting at a compound dummy node is to be connected to if
     * to be reversed.
     * 
     * @param port
     *            The original edge port.
     * @param layeredGraph
     *            The layered graph.
     * @return Returns the port to replace port in edge reversion.
     */
    private LPort getOppositePort(final LPort port, final LGraph layeredGraph) {
        float edgeSpacing = layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR)
                * layeredGraph.getProperty(Properties.OBJ_SPACING);

        // Determine the new portside as the opposite of the current one.
        PortSide portSide = port.getSide();
        PortSide newSide;
        if (portSide == PortSide.EAST) {
            newSide = PortSide.WEST;
        } else {
            newSide = PortSide.EAST;
        }

        // In any case, the new portside will be the opposite of the old one.
        LPort newPort = new LPort(layeredGraph);
        newPort.getSize().x = port.getSize().x;
        newPort.getSize().y = port.getSize().y;
        newPort.copyProperties(port);
        newPort.setSide(newSide);

        // To which node the edge is to be connected differs according to the NodeType of
        // source/target.
        LNode node = port.getNode();
        NodeType nodeType = node.getProperty(Properties.NODE_TYPE);
        switch (nodeType) {
        case UPPER_COMPOUND_BORDER:
            LNode lowerBorder = null;
            for (LNode lnode : layeredGraph.getLayerlessNodes()) {
                if (lnode.getProperty(Properties.NODE_TYPE) == NodeType.LOWER_COMPOUND_BORDER) {
                    if (lnode.getProperty(Properties.COMPOUND_NODE) == node) {
                        lowerBorder = lnode;
                        break;
                    }
                }
            }
            newPort.setNode(lowerBorder);
            // There is no upper compound border dummy node without corresponding lower
            // compound border node, so no null pointer dereference possible (ignore find bug 
            // warning)
            lowerBorder.getSize().y += edgeSpacing;
            break;
        case UPPER_COMPOUND_PORT:
            // Create new LOWER_COMPOUND_PORT
            LNode newLowerCompoundPort = new LNode(layeredGraph);
            newLowerCompoundPort.copyProperties(node);
            newLowerCompoundPort.setProperty(Properties.NODE_TYPE, NodeType.LOWER_COMPOUND_PORT);
            newLowerCompoundPort.setProperty(Properties.COMPOUND_NODE,
                    node.getProperty(Properties.COMPOUND_NODE));
            LPort dummyConnectionPort = new LPort(layeredGraph);
            dummyConnectionPort.setSide(PortSide.WEST);
            dummyConnectionPort.setNode(newLowerCompoundPort);
            // Connect it with compound dummy edges to the direct children of the compound node
            for (LNode child : Util.getChildren(node)) {
                LEdge dummyEdge = new LEdge(layeredGraph);
                dummyEdge.setProperty(Properties.EDGE_TYPE, EdgeType.COMPOUND_DUMMY);
                LPort startPort = child.getPorts(PortSide.WEST).iterator().next();
                dummyEdge.setSource(startPort);
                dummyEdge.setTarget(dummyConnectionPort);
            }
            newPort.setNode(newLowerCompoundPort);
            layeredGraph.getLayerlessNodes().add(newLowerCompoundPort);
            break;
        case LOWER_COMPOUND_BORDER:
            LNode upperBorder = node.getProperty(Properties.COMPOUND_NODE);
            newPort.setNode(upperBorder);
            upperBorder.getSize().y += edgeSpacing;
            break;
        case LOWER_COMPOUND_PORT:
            // Create new UPPER_COMPOUND_PORT
            LNode newUpperCompoundPort = new LNode(layeredGraph);
            newUpperCompoundPort.copyProperties(node);
            newUpperCompoundPort.setProperty(Properties.NODE_TYPE, NodeType.UPPER_COMPOUND_PORT);
            newUpperCompoundPort.setProperty(Properties.COMPOUND_NODE,
                    node.getProperty(Properties.COMPOUND_NODE));
            LPort dummyConnector = new LPort(layeredGraph);
            dummyConnector.setSide(PortSide.EAST);
            dummyConnector.setNode(newUpperCompoundPort);
            // Connect it with compound dummy edges to the direct children of the compound node
            for (LNode child : Util.getChildren(node)) {
                LEdge dummyEdge = new LEdge(layeredGraph);
                dummyEdge.setProperty(Properties.EDGE_TYPE, EdgeType.COMPOUND_DUMMY);
                LPort endPort = child.getPorts(PortSide.EAST).iterator().next();
                dummyEdge.setSource(dummyConnector);
                dummyEdge.setTarget(endPort);
            }
            newPort.setNode(newUpperCompoundPort);
            layeredGraph.getLayerlessNodes().add(newUpperCompoundPort);
            layeredGraph.getLayerlessNodes().remove(node);
            break;
        default:
            break;
        }

        return newPort;
    }
}
