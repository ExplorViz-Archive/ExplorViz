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
package de.cau.cs.kieler.klay.layered.importexport;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;

import com.google.common.collect.Maps;

import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KGraphElement;
import de.cau.cs.kieler.core.kgraph.KLabel;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.kiml.klayoutdata.KInsets;
import de.cau.cs.kieler.kiml.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.Alignment;
import de.cau.cs.kieler.kiml.options.EdgeRouting;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortLabelPlacement;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.kiml.options.SizeOptions;
import de.cau.cs.kieler.kiml.util.KimlUtil;
import de.cau.cs.kieler.klay.layered.Util;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraphElement;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LGraphElement.HashCodeCounter;
import de.cau.cs.kieler.klay.layered.properties.EdgeType;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Manages the transformation of Compound KGraphs to LayeredGraphs.
 * 
 * @author ima
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class CompoundKGraphImporter extends KGraphImporter {

    /**
     * Maximal depth of the imported graph - to be updated during import.
     */
    private int maximalDepth;
    
    /**
     * Creates a graph importer with the given hash code counter.
     * 
     * @param counter the hash code counter used to determine hash codes of graph elements
     */
    public CompoundKGraphImporter(final HashCodeCounter counter) {
        super(counter);
    }

    /**
     * {@inheritDoc}
     */
    public LGraph importGraph(final KNode kgraph) {

        LGraph layeredGraph = super.importGraph(kgraph);

        layeredGraph.getProperty(Properties.ELEMENT_MAP).put(kgraph, layeredGraph);

        Set<GraphProperties> graphProperties = layeredGraph
                .getProperty(Properties.GRAPH_PROPERTIES);

        graphProperties.add(GraphProperties.FLAT_HIERARCHICAL);

        List<LNode> unlayeredNodes = layeredGraph.getLayerlessNodes();

        Direction direction = layeredGraph.getProperty(LayoutOptions.DIRECTION);

        // Prepare a map to insert Parent Nodes and Child nodes for the documentation of dummy
        // edges.
        Map<LNode, List<LNode>> parentChildMap = Maps.newHashMap();
        recursiveTransformCompoundGraph(kgraph, kgraph, unlayeredNodes, layeredGraph,
                graphProperties, parentChildMap, direction, 0);

        // set the graph properties property
        layeredGraph.setProperty(Properties.GRAPH_PROPERTIES, graphProperties);

        // set the maximal depth property.
        layeredGraph.setProperty(Properties.MAX_DEPTH, maximalDepth);

        // set up the inclusion tree.
        createInclusionTree(layeredGraph, kgraph);

        return layeredGraph;
    }

    /**
     * Transforms a compound graph recursively.
     * 
     * @param kgraph
     *            the KNode representing the graph to be laid out.
     * @param currentNode
     *            current node.
     * @param layeredNodes
     *            the list of nodes to add dummy nodes to.
     * @param layeredGraph
     *            the layered graph.
     * @param graphProperties
     *            graph properties updated during the transformation
     * @param parentChildMap
     *            Map to document set dummy edges
     * @param direction
     * @param depth
     *            Current depth in the inclusion tree (depth of the currentNode);
     */
    void recursiveTransformCompoundGraph(final KNode kgraph, final KNode currentNode,
            final List<LNode> layeredNodes, final LGraph layeredGraph,
            final Set<GraphProperties> graphProperties, final Map<LNode, List<LNode>> parentChildMap,
            final Direction direction, final int depth) {
        
        if (depth > maximalDepth) {
            maximalDepth = depth;
        }
        
        Map<KGraphElement, LGraphElement> elemMap = layeredGraph.getProperty(Properties.ELEMENT_MAP);
        if (currentNode.getChildren().isEmpty()) {
            transformLeaveNode(currentNode, layeredNodes, elemMap, graphProperties,
                    layeredGraph.getProperty(LayoutOptions.DIRECTION), depth, kgraph);
            transformLeaveEdges(currentNode, elemMap, direction, layeredGraph);
        } else {
            for (KNode child : currentNode.getChildren()) {
                recursiveTransformCompoundGraph(kgraph, child, layeredNodes, layeredGraph,
                        graphProperties, parentChildMap, direction, depth + 1);
            }
            if (currentNode != kgraph) {
                transformCompoundNodeWithEdges(currentNode, layeredNodes, layeredGraph, elemMap,
                        direction, depth);
                setCompoundDummyEdges(layeredNodes, parentChildMap, elemMap);
            }
        }
    }

    /**
     * Transform leave node.
     * 
     * @param node
     *            Node to be transformed.
     * @param layeredNodes
     *            List to add representative to.
     * @param elemMap
     *            the element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     * @param graphProperties
     *            graph properties updated during the transformation.
     * @param direction
     *            overall layout direction
     * @param depth
     *            Depth of the leave node in the inclusion tree.
     * @param kgraph
     *            KNode representing the graph to be laid out.
     */
    private void transformLeaveNode(final KNode node, final List<LNode> layeredNodes,
            final Map<KGraphElement, LGraphElement> elemMap,
            final Set<GraphProperties> graphProperties, final Direction direction, final int depth,
            final KNode kgraph) {

        super.transformNode(node, layeredNodes, elemMap,
                (Set<GraphProperties>) graphProperties, direction);

        LNode newNode = (LNode) elemMap.get(node);
        newNode.setProperty(Properties.K_PARENT, node.getParent());
        
        // Set flat position (position in relation to layout node) coordinates to support
        // interactive mode
        KVector flatPosVec = new KVector(0, 0);
        Util.getFlatPosition(node, kgraph, flatPosVec);
        newNode.getPosition().x = flatPosVec.x;
        newNode.getPosition().y = flatPosVec.y;

        // Add ports to connect dummy edges for the layering phase.
        LPort dummyPortWest = createDummyPort(newNode, PortSide.WEST, null, elemMap);
        dummyPortWest.setProperty(Properties.LEAVE_DUMMY_PORT, true);
        LPort dummyPortEast = createDummyPort(newNode, PortSide.EAST, null, elemMap);
        dummyPortEast.setProperty(Properties.LEAVE_DUMMY_PORT, true);

        // Set the depth-Property.
        newNode.setProperty(Properties.DEPTH, depth);
    }

    /**
     * Transforms the Edges of a leave node.
     * 
     * @param knode
     *            Node whose edges are to be transformed.
     * @param elemMap
     *            Map to store pairs of the original elements of the KGraph and their
     *            representatives in the LGraph.
     * @param direction
     *            The layout direction.
     * @param layeredGraph
     *            The layered graph representation of the graph to be laid out.
     */
    private void transformLeaveEdges(final KNode knode,
            final Map<KGraphElement, LGraphElement> elemMap, final Direction direction,
            final LGraph layeredGraph) {
        
        for (KEdge edge : knode.getIncomingEdges()) {
            KEdgeLayout edgeLayout = edge.getData(KEdgeLayout.class);
            LEdge newEdge = null;
            if (elemMap.containsKey(edge)) {
                newEdge = (LEdge) elemMap.get(edge);
            } else {
                newEdge = createLEdgeFromKEdge(edge, elemMap, layeredGraph);
            }
            
            LNode representative = (LNode) elemMap.get(knode);
            if (edge.getTargetPort() == null) {
                LPort newPort = createDummyPort(representative, PortSide.WEST, null, elemMap);
                KPoint targetPoint = edgeLayout.getTargetPoint();
                newPort.getPosition().x = targetPoint.getX() - representative.getPosition().x;
                newPort.getPosition().y = targetPoint.getY() - representative.getPosition().y;
                newEdge.setTarget(newPort);
            } else {
                LPort port = (LPort) elemMap.get(edge.getTargetPort());
                newEdge.setTarget(port);
            }
        }
        
        for (KEdge edge : knode.getOutgoingEdges()) {
            // Check for self-loops
            if (edge.getSource() == edge.getTarget()) {
                Set<GraphProperties> graphProperties = layeredGraph.getProperty(
                        Properties.GRAPH_PROPERTIES);
                graphProperties.add(GraphProperties.SELF_LOOPS);
            }
            
            KEdgeLayout edgeLayout = edge.getData(KEdgeLayout.class);
            LEdge newEdge = null;
            if (elemMap.containsKey(edge)) {
                newEdge = (LEdge) elemMap.get(edge);
            } else {
                newEdge = createLEdgeFromKEdge(edge, elemMap, layeredGraph);
            }
            
            LNode representative = (LNode) elemMap.get(knode);
            if (edge.getSourcePort() == null) {
                LPort newPort = createDummyPort(representative, PortSide.EAST, null, elemMap);
                KPoint sourcePoint = edgeLayout.getSourcePoint();
                newPort.getPosition().x = sourcePoint.getX() - representative.getPosition().x;
                newPort.getPosition().y = sourcePoint.getY() - representative.getPosition().y;
                newEdge.setSource(newPort);
            } else {
                LPort port = (LPort) elemMap.get(edge.getSourcePort());
                newEdge.setSource(port);
            }
        }
    }

    /**
     * Transforms all edges of a compound node and replaces the node and its ports by upper and
     * lower dummy nodes and their ports. Representing a compound node by upper and lower dummy
     * nodes is inspired by Georg Sander, Layout of Compound Directed Graphs, technical report
     * A/03/96, Universität des Saarlandes, 1996. Sander's approach is extended to the use of
     * multiple dummy nodes in the case of the presence of ports.
     * 
     * @param node
     *            node to be replaced by border dummies.
     * @param layeredNodes
     *            list the dummy nodes are to be added to.
     * @param layeredGraph
     *            the layered graph.
     * @param elemMap
     *            the element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     * @param direction
     * @param depth
     *            The depth of the compound node in the inclusion tree.
     * 
     */
    private void transformCompoundNodeWithEdges(final KNode node, final List<LNode> layeredNodes,
            final LGraph layeredGraph, final Map<KGraphElement, LGraphElement> elemMap,
            final Direction direction, final int depth) {
        // While transforming the edges and creating dummy nodes, keep a list of the dummies.
        List<LNode> dummyNodes = new LinkedList<LNode>();
        // Iterate incoming and outgoing edges, transform them and create dummy nodes and ports
        // representing the node.
        transformCompoundEdgeList(node, layeredNodes, elemMap, dummyNodes, node.getIncomingEdges(),
                true, layeredGraph, direction, depth);
        transformCompoundEdgeList(node, layeredNodes, elemMap, dummyNodes, node.getOutgoingEdges(),
                false, layeredGraph, direction, depth);
    }

    /**
     * Iterate the given list of edges of a node. Create an LEdge for each KEdge that has no
     * representative yet. Create dummy nodes with ports for each edge with a target port, a single
     * compound border for all incoming edges without ports or in case there are no incoming edges.
     * Edges from inside the node will be connected to lower dummy nodes, all others to upper dummy
     * nodes. Either to be invoked with a List of incoming or one of outgoing edges as a parameter,
     * indicate whether it is the one or the other by setting the incoming flag in the parameter
     * list.
     * 
     * @param node
     *            The node to be replaced by upper dummy nodes.
     * @param layeredNodes
     *            The List the dummy nodes are to be added to.
     * @param elemMap
     *            The element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     * @param dummyNodes
     *            List to document already created dummy nodes.
     * @param edgesList
     *            List of the edges to transform.
     * @param incoming
     *            True, if the List is a List of incoming edges, false if it is a List of outgoing
     *            edges.
     * @param layeredGraph
     *            The layered graph.
     * @param direction
     *            the overall layout direction.
     * @param depth
     *            the depth of the compound node in the inclusion tree.
     */
    private void transformCompoundEdgeList(final KNode node, final List<LNode> layeredNodes,
            final Map<KGraphElement, LGraphElement> elemMap, final List<LNode> dummyNodes,
            final List<KEdge> edgesList, final boolean incoming, final LGraph layeredGraph,
            final Direction direction, final int depth) {

        // get layout data of the compound node
        KShapeLayout nodeLayout = node.getData(KShapeLayout.class);
        KInsets insets = nodeLayout.getInsets();
        float borderSpacing = nodeLayout.getProperty(Properties.BORDER_SPACING);
        LNode upperBorder = null;

        if (incoming) {
            // Create upper border dummy node to represent the compound node.
            upperBorder = createBorderDummyNode(node, NodeType.UPPER_COMPOUND_BORDER, dummyNodes,
                    elemMap, depth, layeredGraph);
            upperBorder.setProperty(Properties.ORIGINAL_INSETS, insets);
            upperBorder.setProperty(Properties.BORDER_SPACING, borderSpacing);
            upperBorder.getSize().x = insets.getLeft() + borderSpacing;
        } else {
            for (LNode lnode : dummyNodes) {
                if ((lnode.getProperty(Properties.NODE_TYPE) == NodeType.UPPER_COMPOUND_BORDER)
                        && (lnode.getProperty(Properties.ORIGIN) == node)) {
                    upperBorder = lnode;
                    break;
                }
            }
        }

        for (KEdge kEdge : edgesList) {
            KEdgeLayout edgeLayout = kEdge.getData(KEdgeLayout.class);

            // Create a corresponding LEdge for each KEdge in the List that has no
            // representative yet.
            LEdge lEdge = null;
            if (!elemMap.containsKey(kEdge)) {
                lEdge = createLEdgeFromKEdge(kEdge, elemMap, layeredGraph);
            } else {
                lEdge = (LEdge) elemMap.get(kEdge);
            }

            // LNode to represent the node for this edge in the LGraph
            LNode representative = null;

            // Check, whether the edge is from inside the node or not
            boolean fromInside = false;
            if (incoming) {
                fromInside = isDescendantNotSelf(node, kEdge.getSource());
            } else {
                // The handling of outgoing edges is inverse to the handling of incoming edges.
                // So the reverse boolean value is needed.
                fromInside = !(isDescendantNotSelf(node, kEdge.getTarget()));
            }

            // Get the adequate port of current edge, null, if there is none
            KPort port = null;
            if (incoming) {
                port = kEdge.getTargetPort();
            } else {
                port = kEdge.getSourcePort();
            }
            KShapeLayout portLayout = null;
            if (port != null) {
                portLayout = port.getData(KShapeLayout.class);
            }

            // If edge has no target port, create a border dummy node resp. reuse the one
            // created before
            if (port == null) {
                if (fromInside) {
                    representative = createBorderDummyNode(node, NodeType.LOWER_COMPOUND_BORDER,
                            dummyNodes, elemMap, depth, layeredGraph);
                    // representative.setProperty(Properties.COMPOUND_NODE, upperBorder);
                    representative.getSize().x = insets.getRight() + borderSpacing;
                } else {
                    representative = upperBorder;
                }

                // If edge has a target port, create adequate compound port dummy node, if not done
                // before
            } else {
                LGraphElement portRepresentative = elemMap.get(port);
                if (portRepresentative == null) {
                    if (fromInside) {
                        representative = createBorderDummyNode(node, NodeType.LOWER_COMPOUND_PORT,
                                dummyNodes, elemMap, depth, layeredGraph);
                        // representative.setProperty(Properties.COMPOUND_NODE, upperBorder);
                        representative.getSize().x = insets.getRight() + borderSpacing;
                    } else {
                        representative = createBorderDummyNode(node, NodeType.UPPER_COMPOUND_PORT,
                                dummyNodes, elemMap, depth, layeredGraph);
                        // representative.setProperty(Properties.COMPOUND_NODE, upperBorder);
                        representative.getSize().x = insets.getLeft() + borderSpacing;
                    }
                } else {
                    representative = ((LPort) portRepresentative).getNode();
                }
            }
            if (!layeredNodes.contains(representative)) {
                layeredNodes.add(representative);
            }

            // Connect the edge to the dummy node, to western port for incoming edges, to
            // eastern for outgoing ones
            PortSide portSide = null;
            if (incoming) {
                portSide = PortSide.WEST;
            } else {
                portSide = PortSide.EAST;
            }
            // Make an entry to the elemMap, if the dummyport is to represent a port in the original
            // graph
            LPort dummyPort = createDummyPort(representative, portSide, port, elemMap);
            // make sure, edges do not overlap
            float edgeSpacing = layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR)
                    * layeredGraph.getProperty(Properties.OBJ_SPACING);
            representative.getSize().y += edgeSpacing;

            if (incoming) {
                KPoint targetPoint = edgeLayout.getTargetPoint();
                dummyPort.getPosition().x = targetPoint.getX() - representative.getPosition().x;
                lEdge.setTarget(dummyPort);
            } else {
                KPoint sourcePoint = edgeLayout.getSourcePoint();
                dummyPort.getPosition().x = sourcePoint.getX() - representative.getPosition().x;
                lEdge.setSource(dummyPort);
            }

            if (port != null) {
                // copy properties and attributes of original port to dummy port.
                elemMap.put(port, dummyPort);
                dummyPort.setProperty(Properties.ORIGIN, port);
                dummyPort.copyProperties(portLayout);
                dummyPort.getSize().x = portLayout.getWidth();
                dummyPort.getSize().y = portLayout.getHeight();
            }
        }

        // If not done before (if the edge list is empty or containing only edges to/from
        // descendants), a single border dummy node is added.

        NodeType nodeType = null;
        if (incoming) {
            if (!(layeredNodes.contains(upperBorder))) {
                layeredNodes.add(upperBorder);
            }
        } else {
            nodeType = NodeType.LOWER_COMPOUND_BORDER;
            LNode dummyNode = createBorderDummyNode(node, nodeType, dummyNodes, elemMap, depth,
                    layeredGraph);
            dummyNode.getSize().x = insets.getRight() + borderSpacing;
            if (!(layeredNodes.contains(dummyNode))) {
                layeredNodes.add(dummyNode);
            }
        }
    }

    /**
     * Creates dummy edges between a compound node border dummy node and its children.
     * 
     * @param layeredNodes
     * @param layeredNodes
     *            the list of LNodes with the created dummy nodes and the imported nodes.
     * @param elemMap
     * @param source
     *            the source node.
     */
    private void setCompoundDummyEdges(final List<LNode> layeredNodes,
            final Map<LNode, List<LNode>> parentChildMap,
            final Map<KGraphElement, LGraphElement> elemMap) {
        for (LNode lNode : layeredNodes) {
            NodeType nodeType = lNode.getProperty(Properties.NODE_TYPE);
            switch (nodeType) {
            // If the node is a compound dummy node at the upper line of the compound node, add
            // edge to every child node
            case UPPER_COMPOUND_BORDER:
            case UPPER_COMPOUND_PORT:
                for (LNode childCandidate : layeredNodes) {
                    if ((childCandidate.getProperty(Properties.K_PARENT) != null)
                            && (childCandidate.getProperty(Properties.K_PARENT) == (lNode
                                    .getProperty(Properties.ORIGIN)))) {
                        if (!(parentChildMap.containsKey(lNode))
                                || !(parentChildMap.get(lNode).contains(childCandidate))) {
                            LEdge dummyEdge = new LEdge(layeredGraph);
                            dummyEdge.setProperty(Properties.EDGE_TYPE, EdgeType.COMPOUND_DUMMY);

                            LPort sourcePort = lNode.getPorts(PortSide.EAST).iterator().next();
                            dummyEdge.setSource(sourcePort);

                            LPort targetPort = findDummyEdgePort(childCandidate, PortSide.WEST,
                                    elemMap);
                            dummyEdge.setTarget(targetPort);
                            if (parentChildMap.containsKey(lNode)) {
                                parentChildMap.get(lNode).add(childCandidate);
                            } else {
                                List<LNode> list = new LinkedList<LNode>();
                                list.add(childCandidate);
                                parentChildMap.put(lNode, list);
                            }
                        }
                    }
                }
                break;
            // If the node is a compound dummy node at the lower line of the compound node,
            // add
            // edge from every child node to this node
            case LOWER_COMPOUND_BORDER:
            case LOWER_COMPOUND_PORT:
                for (LNode childCandidate : layeredNodes) {
                    if ((childCandidate.getProperty(Properties.K_PARENT) != null)
                            && (childCandidate.getProperty(Properties.K_PARENT) == (lNode
                                    .getProperty(Properties.ORIGIN)))) {
                        if (!(parentChildMap.containsKey(lNode))
                                || !(parentChildMap.get(lNode).contains(childCandidate))) {
                            LEdge dummyEdge = new LEdge(layeredGraph);
                            dummyEdge.setProperty(Properties.EDGE_TYPE, EdgeType.COMPOUND_DUMMY);

                            LPort sourcePort = findDummyEdgePort(childCandidate, PortSide.EAST,
                                    elemMap);
                            dummyEdge.setSource(sourcePort);

                            LPort targetPort = lNode.getPorts(PortSide.WEST).iterator().next();
                            dummyEdge.setTarget(targetPort);

                            if (parentChildMap.containsKey(lNode)) {
                                parentChildMap.get(lNode).add(childCandidate);
                            } else {
                                List<LNode> list = new LinkedList<LNode>();
                                list.add(childCandidate);
                                parentChildMap.put(lNode, list);
                            }
                        }
                    }
                }
                break;
            // If the node is no compound dummy node, nothing is to be done.
            default:
                break;

            }
        }
    }

    /**
     * Returns an LEdge as a representative for the given KEdge. Nodes and Ports for source and
     * target are not set. They are to be handled by calling methods.
     * 
     * @param kedge
     *            KEdge for which the LEdge is to be created.
     * @param elemMap
     *            the element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     * @param layeredGraph
     *            The layered graph.
     * @return returns created LEdge that represents the given KEdge.
     */
    private LEdge createLEdgeFromKEdge(final KEdge kedge,
            final Map<KGraphElement, LGraphElement> elemMap, final LGraph layeredGraph) {
        super.transformEdge(kedge, (KNode) layeredGraph.getProperty(Properties.ORIGIN), elemMap);

        LEdge newEdge = (LEdge) elemMap.get(kedge);
        elemMap.put(kedge, newEdge);

        return newEdge;
    }

    /**
     * Creates a border dummy node in context of the representation of a compound node. Adds a
     * western and an eastern port. In case a lower or upper compound dummy node is asked for,
     * checks whether there is one already in the given list. If yes, that one will be returned, if
     * not, a new dummy node is created. As to initial positioning for interactive mode, the
     * algorithm sets the positions of all left side dummy nodes alike as well as for all right
     * dummy nodes. This might be enhanced by assigning positions in the original order of ports.
     * 
     * @param node
     *            The node to be represented.
     * @param elemMap
     * @param depth
     *            The depth of the compound node in the inclusion tree.
     * @param layeredGraph
     * @param upperBorder
     *            Denotes, if an upper border node is to be created, if not, a lower border node
     *            will be created.
     */
    private LNode createBorderDummyNode(final KNode node, final NodeType nodeType,
            final List<LNode> dummyList, final Map<KGraphElement, LGraphElement> elemMap,
            final int depth, final LGraph layeredGraph) {

        // Prepare initial positioning of dummy node for interactive mode
        KNode refNode = (KNode) elemMap.get(layeredGraph);
        KVector flatPosVec = new KVector(0, 0);
        Util.getFlatPosition(node, refNode, flatPosVec);

        LNode dummyNode = null;
        if ((nodeType == NodeType.LOWER_COMPOUND_BORDER)) {
            for (LNode dummy : dummyList) {
                if (dummy.getProperty(Properties.NODE_TYPE) == nodeType) {
                    dummyNode = dummy;
                }
            }
        }
        if (dummyNode == null) {
            KShapeLayout nodeLayout = node.getData(KShapeLayout.class);
            dummyNode = new LNode(layeredGraph);
            dummyNode.setProperty(Properties.ORIGIN, node);
            dummyNode.setProperty(Properties.K_PARENT, node.getParent());
            dummyNode.getPosition().x = nodeLayout.getXpos();
            dummyNode.setProperty(Properties.NODE_TYPE, nodeType);
            if (nodeType == NodeType.UPPER_COMPOUND_BORDER
                    || nodeType == NodeType.UPPER_COMPOUND_PORT) {
                dummyNode.setProperty(LayoutOptions.ALIGNMENT, Alignment.RIGHT);
                // set initial positioning for interactive mode
                dummyNode.getPosition().x = flatPosVec.x;
                dummyNode.getPosition().y = flatPosVec.y + (nodeLayout.getHeight() / 2.0);
            }
            if (nodeType == NodeType.LOWER_COMPOUND_BORDER
                    || nodeType == NodeType.LOWER_COMPOUND_PORT) {
                dummyNode.setProperty(LayoutOptions.ALIGNMENT, Alignment.LEFT);
                // set initial positioning for interactive mode
                dummyNode.getPosition().x = flatPosVec.x + nodeLayout.getWidth();
                dummyNode.getPosition().y = flatPosVec.y + (nodeLayout.getHeight() / 2.0);
            }
            createDummyPort(dummyNode, PortSide.EAST, null, elemMap);
            createDummyPort(dummyNode, PortSide.WEST, null, elemMap);
            if (nodeType == NodeType.UPPER_COMPOUND_BORDER) {
                transferNodePropertiesAndAttributes(node, dummyNode);
            }
            dummyList.add(dummyNode);
        }
        if (nodeType == NodeType.UPPER_COMPOUND_BORDER) {
            elemMap.put(node, dummyNode);
        }

        // Set the depth-Property.
        dummyNode.setProperty(Properties.DEPTH, depth);

        // Set the compound-Node-Property
        dummyNode.setProperty(Properties.COMPOUND_NODE, elemMap.get(node));

        return dummyNode;
    }

    /**
     * Transfers a compound nodes attributes and properties to the representative upper border dummy
     * node. As this is only the setup of a left border dummy node, the label of the original node
     * is not added here.
     * 
     * @param node
     *            The compound KNode to be represented.
     * @param dummyNode
     *            The upper border dummy node representing the compound node.
     */
    private void transferNodePropertiesAndAttributes(final KNode node, final LNode dummyNode) {

        KShapeLayout nodeLayout = node.getData(KShapeLayout.class);

        // port constraints are not handled for compound nodes at the moment, so set them to free
        nodeLayout.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FREE);

        // set properties of the new node
        dummyNode.copyProperties(nodeLayout);
    }

    /**
     * Creates a port for an LNode with the given PortSide. If the port is to represent a port from
     * the original graph, who already has a representative, the latter will be returned.
     * 
     * @param dummyNode
     *            the node the port is to be created for.
     * @param side
     *            the side of the node, on which the port is to be located.
     * @param port
     *            the port to be represented, null, if original edge had no port.
     * @param elemMap
     *            the element map, mapping the KGraph-Elements to their LGraph-Representatives.
     */
    private LPort createDummyPort(final LNode node, final PortSide side, final KPort port,
            final Map<KGraphElement, LGraphElement> elemMap) {

        LPort portRepresentative = null;
        if (port != null) {
            portRepresentative = (LPort) elemMap.get(port);
        }
        if ((portRepresentative == null) || (portRepresentative.getSide() != side)) {
            LPort dummyPort = new LPort(layeredGraph);
            dummyPort.setSide(side);
            dummyPort.setNode(node);
            if (port != null) {
                elemMap.put(port, dummyPort);
            }
            return dummyPort;
        } else {
            return portRepresentative;
        }
    }

    /**
     * Checks, if a KNode is descendant of another in the inclusion tree. attention: returns false,
     * if nodes are equal.
     * 
     * @param node
     *            node that is the possible ancestor.
     * @param candidate
     *            node that is the possible descendant.
     * @return returns a boolean value indicating, if candidate is descendant of node (false if
     *         nodes are equal).
     */
    private boolean isDescendantNotSelf(final KNode node, final KNode candidate) {
        List<KNode> descendantsList = new LinkedList<KNode>();
        listDescendants(node, descendantsList);
        if (node.equals(candidate)) {
            return false;
        } else {
            return descendantsList.contains(candidate);
        }
    }

    /**
     * Checks, if a KNode is descendant of another in the inclusion tree. attention: returns true,
     * if nodes are equal.
     * 
     * @param node
     *            node that is the possible ancestor.
     * @param candidate
     *            node that is the possible descendant.
     * @return returns a boolean value indicating, if candidate is descendant of node (false if
     *         nodes are equal).
     */
    public static boolean isDescendant(final KNode node, final KNode candidate) {
        List<KNode> descendantsList = new LinkedList<KNode>();
        listDescendants(node, descendantsList);
        return descendantsList.contains(candidate);
    }

    /**
     * Recursively adds descendants of given node to a given list. Adds the startNode as well.
     * 
     * @param currentNode
     *            actual root node of the inclusion tree, whose nodes are to be added to the list.
     * @param descendantsList
     */
    private static void listDescendants(final KNode currentNode, final List<KNode> descendantsList) {
        if (currentNode.getChildren().isEmpty()) {
            descendantsList.add(currentNode);
        } else {
            for (KNode child : currentNode.getChildren()) {
                listDescendants(child, descendantsList);
            }
            descendantsList.add(currentNode);
        }
    }

    /**
     * Finds among the ports of one side of the node the one prepared to connect dummy edges for
     * layering constraints.
     * 
     * @param node
     *            The node who's ports are to be probed.
     * @param side
     *            The side for which the ports are to be probed.
     * @param elemMap
     */
    private LPort findDummyEdgePort(final LNode node, final PortSide side,
            final Map<KGraphElement, LGraphElement> elemMap) {
        LPort port = null;
        Iterator<LPort> portIterator = node.getPorts(side).iterator();
        // Find the correct port to connect the edge to: do not use ports that are
        // transformed ports from the original graph or created to connect edges from the original
        // graph.
        while (portIterator.hasNext()) {
            LPort portCandidate = portIterator.next();
            NodeType nodeType = portCandidate.getNode().getProperty(Properties.NODE_TYPE);
            if ((nodeType == (NodeType.UPPER_COMPOUND_BORDER) || portCandidate
                    .getProperty(Properties.LEAVE_DUMMY_PORT))) {
                port = portCandidate;
                break;
            }
        }
        // Do not return null, if there is no applicable port, create one.
        if (port == null) {
            port = createDummyPort(node, side, null, elemMap);
        }
        return port;
    }

    // /////////////////////////////////////////////////////////////////////////////
    // Apply Layout Results

    /**
     * {@inheritDoc}
     */
    public void applyLayout(final LGraph layeredGraph) {

        KNode target = (KNode) layeredGraph.getProperty(Properties.ORIGIN);

        // determine the border spacing, which influences the offset
        KShapeLayout parentLayout = target.getData(KShapeLayout.class);
        float graphBorderSpacing = layeredGraph.getProperty(Properties.BORDER_SPACING);

        KVector graphOffset = layeredGraph.getOffset();

        // process nodes, collect edges while at it
        List<LEdge> edgeList = new LinkedList<LEdge>();

        for (LNode lnode : layeredGraph.getLayerlessNodes()) {
            Object origin = lnode.getProperty(Properties.ORIGIN);
            if (origin instanceof KNode) {
                KNode kNode = (KNode) origin;
                // apply the layout to the KNode
                applyNodeLayout(layeredGraph, lnode);
                // apply the layout to the KNode's ports
                boolean isCompound = (lnode.getProperty(Properties.NODE_TYPE) 
                        == NodeType.UPPER_COMPOUND_BORDER);
                if (isCompound) {
                    compoundApplyPortLayout(kNode, layeredGraph, lnode);
                } else {
                    applyPortLayout((KNode) origin, layeredGraph, lnode);
                }
            } else if (origin instanceof KPort) {
                // It's an external port. Set its position
                KShapeLayout portLayout = ((KPort) origin).getData(KShapeLayout.class);
                KVector portPosition = getExternalPortPosition(layeredGraph, lnode,
                        portLayout.getWidth(), portLayout.getHeight());

                portLayout.setXpos((float) portPosition.x);
                portLayout.setYpos((float) portPosition.y);
            }
            // collect edges
            for (LPort port : lnode.getPorts()) {
                edgeList.addAll(port.getOutgoingEdges());
            }
        }

        // check if the edge routing uses splines
        EdgeRouting routing = parentLayout.getProperty(LayoutOptions.EDGE_ROUTING);
        boolean splinesActive = routing == EdgeRouting.SPLINES;

        // process edges
        for (LEdge ledge : edgeList) {
            applyEdgeLayout(ledge, graphBorderSpacing, layeredGraph, splinesActive);
        }

        // set up the layout node
        KInsets insets = parentLayout.getInsets();
        float width = (float) layeredGraph.getSize().x + 2 * graphBorderSpacing + insets.getLeft()
                + insets.getRight() + (float) graphOffset.x;
        float height = (float) layeredGraph.getSize().y + 2 * graphBorderSpacing + insets.getTop()
                + insets.getBottom() + (float) graphOffset.y;

        if (layeredGraph.getProperty(Properties.GRAPH_PROPERTIES).contains(
                GraphProperties.EXTERNAL_PORTS)) {

            // ports have been positioned using dummy nodes
            parentLayout.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
            KimlUtil.resizeNode(target, width, height, false, true);
        } else {
            // ports have not been positioned yet - leave this for next layouter
            KimlUtil.resizeNode(target, width, height, true, true);
        }
    }

    /**
     * Applies the layout of a single edge.
     * 
     * @param ledge
     *            edge whose layout is to be applied.
     * @param graphBorderSpacing
     *            borderSpacing of the whole graph.
     * @param layeredGraph
     *            the complete layered graph.
     * @param splinesActive
     *            signifies if the edge routing uses splines.
     */
    private void applyEdgeLayout(final LEdge ledge, final double graphBorderSpacing,
            final LGraph layeredGraph, final boolean splinesActive) {
        // get layout of corresponding KEdge
        KEdge kedge = (KEdge) ledge.getProperty(Properties.ORIGIN);
        KEdgeLayout edgeLayout = kedge.getData(KEdgeLayout.class);
        KVectorChain bendPoints = ledge.getBendPoints();

        // get position of the node corresponding to the edge's source node (KNode) and layout
        // of its parent
        KNode kSourceNode = kedge.getSource();
        KShapeLayout kSourceNodeLayout = kSourceNode.getData(KShapeLayout.class);
        KVector kSourceNodePosition = new KVector(kSourceNodeLayout.getXpos(),
                kSourceNodeLayout.getYpos());

        // get position of the node corresponding to the edge's target node (KNode)
        KNode kTargetNode = kedge.getTarget();
        KShapeLayout kTargetNodeLayout = kTargetNode.getData(KShapeLayout.class);
        KVector kTargetNodePosition = new KVector(kTargetNodeLayout.getXpos(),
                kTargetNodeLayout.getYpos());

        // determine, if edge establishes adjacency between a node and one of its descendants
        boolean descendantEdge = KimlUtil.isDescendant(kTargetNode, kSourceNode);

        LPort sourcePort = ledge.getSource();
        LPort targetPort = ledge.getTarget();

        // adjust bendpoint-positions
        // respect graph's border spacing
        KVector offsetBorderSpacingVec = new KVector(graphBorderSpacing, graphBorderSpacing);

        // and respect the graph's offset also
        KVector graphOffset = layeredGraph.getOffset();
        offsetBorderSpacingVec.add(graphOffset);
        
        // consider junction points
        KVectorChain junctionPoints = ledge.getProperty(LayoutOptions.JUNCTION_POINTS);
        if (junctionPoints != null) {
            junctionPoints.translate(offsetBorderSpacingVec);
        }

        bendPoints.translate(offsetBorderSpacingVec);
        if (!(kSourceNode.getParent() == (KNode) layeredGraph.getProperty(Properties.ORIGIN))
                || descendantEdge) {
            // calculate relative positioning
            KVector bendpointOffset = new KVector();
            if (descendantEdge) {
                bendpointOffset = getAbsolute(kSourceNode);
            } else {
                bendpointOffset = getAbsolute(kSourceNode.getParent());
            }
            bendpointOffset.negate();
            bendPoints.translate(bendpointOffset);
            if (junctionPoints != null) {
                junctionPoints.translate(bendpointOffset);
            }
        }

        // calculate starting point of edge
        KVector edgeStart = new KVector(0, 0);
        if ((sourcePort.getNode().getProperty(Properties.NODE_TYPE) == NodeType.UPPER_COMPOUND_BORDER)
                && (sourcePort.getSide() == PortSide.WEST)
                && !(sourcePort.getProperty(Properties.ORIGIN) instanceof KPort)) {
            // edges starting at an UPPER_COMPOUND_BORDER node (in the original import) need special
            // treatment, because their source node has been repositioned after edge routing.
            edgeStart.add(sourcePort.getPosition()).add(sourcePort.getAnchor());
            edgeStart.y += sourcePort.getNode().getProperty(Properties.POSITION_DIFFERENCE).y;
        } else {
            edgeStart.add(sourcePort.getPosition()).add(sourcePort.getAnchor());
        }
        if (descendantEdge) {
            edgeStart.x -= kSourceNodeLayout.getInsets().getLeft();
            edgeStart.y -= kSourceNodeLayout.getInsets().getTop();
        } else {
            edgeStart.add(kSourceNodePosition);
        }

        KVector difference = getAbsolute(kTargetNode).sub(getAbsolute(kSourceNode));
        if (!descendantEdge) {
            // Mind the fact that getAbsolute calculates absolute coordinates plus insets. We need
            // the difference from the absolute sourceNode position without insets.
            KVector sourceInsets = new KVector(kSourceNodeLayout.getInsets().getLeft(),
                    kSourceNodeLayout.getInsets().getTop());
            difference.add(sourceInsets);
        }

        // calculate end point of edge
        KVector edgeEnd = new KVector(0, 0);

        if ((targetPort.getNode().getProperty(Properties.NODE_TYPE) == NodeType.UPPER_COMPOUND_BORDER)
                && (targetPort.getSide() == PortSide.WEST)
                && !(targetPort.getProperty(Properties.ORIGIN) instanceof KPort)) {
            // edges ending at an UPPER_COMPOUND_BORDER (in the original import) node need special
            // treatment, because their target node has been repositioned after edge routing.
            edgeEnd.add(targetPort.getPosition()).add(targetPort.getAnchor());
            edgeEnd.y += targetPort.getNode().getProperty(Properties.POSITION_DIFFERENCE).y;
        } else {
            edgeEnd.add(targetPort.getPosition()).add(targetPort.getAnchor());
        }
        if (kSourceNode.getParent() == kTargetNode.getParent()) {
            edgeEnd.add(kTargetNodePosition);
        } else {
            if (!descendantEdge) {
                edgeEnd.add(kSourceNodePosition);
            }
            edgeEnd.add(difference);
            // mind the fact that getAbsolute calculates absolute coordinates plus insets.
            edgeEnd.x -= kTargetNodeLayout.getInsets().getLeft();
            edgeEnd.y -= kTargetNodeLayout.getInsets().getTop();
        }

        // add starting- and endpoint of edge to bendpoints
        bendPoints.addFirst(edgeStart);
        bendPoints.addLast(edgeEnd);

        // transfer the bend points and end points to the edge layout
        edgeLayout.applyVectorChain(bendPoints);
        
        // transfer the junction points to the edge layout
        edgeLayout.setProperty(LayoutOptions.JUNCTION_POINTS, junctionPoints);

        // set spline option
        if (splinesActive) {
            edgeLayout.setProperty(LayoutOptions.EDGE_ROUTING, EdgeRouting.SPLINES);
        }
    }

    /**
     * Applies layout to a single node.
     * 
     * @param layeredGraph
     *            the complete layered graph.
     * @param node
     *            LNode representative of the KNode whose layout will be updated.
     */
    private void applyNodeLayout(final LGraph layeredGraph, final LNode node) {

        // Get borderSpacing of the graph
        float graphBorderSpacing = layeredGraph.getProperty(Properties.BORDER_SPACING);

        // get original of node
        KNode original = (KNode) node.getProperty(Properties.ORIGIN);

        // get parent of currentNode
        KNode parent = original.getParent();

        // determine the border spacing of the parent node
        KShapeLayout parentLayout = parent.getData(KShapeLayout.class);

        // get insets of parent node
        KInsets insetsParent = parentLayout.getInsets();

        // get current node's layout
        KShapeLayout nodeLayout = original.getData(KShapeLayout.class);

        // find the representative of the original's parent in the layered graph.
        LNode parentRepresentative = null;
        for (LNode lnode : layeredGraph.getLayerlessNodes()) {
            if (lnode.getProperty(Properties.ORIGIN) == parent) {
                parentRepresentative = lnode;
                break;
            }
        }

        // determine, if the current node is a compound node
        boolean isCompound = (node.getProperty(Properties.NODE_TYPE) == NodeType.UPPER_COMPOUND_BORDER);

        // get the size and margin of the node's representative
        KVector size = node.getSize();
        nodeLayout.setSize((float) size.x, (float) size.y);
        
        // set label positions
        if (!node.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT).isEmpty()) {
            for (LLabel llabel : node.getLabels()) {
                KLabel klabel = (KLabel) llabel.getProperty(Properties.ORIGIN);
                KShapeLayout klabelLayout = klabel.getData(KShapeLayout.class);
                klabelLayout.applyVector(llabel.getPosition());
            }
        }
        
        // set node insets, if requested
        if (!isCompound && nodeLayout.getProperty(LayoutOptions.SIZE_OPTIONS)
                .contains(SizeOptions.COMPUTE_INSETS)) {
            
            // Apply insets
            LInsets lInsets = node.getInsets();
            KInsets kInsets = nodeLayout.getInsets();
            kInsets.setBottom((float) lInsets.bottom);
            kInsets.setTop((float) lInsets.top);
            kInsets.setLeft((float) lInsets.left);
            kInsets.setRight((float) lInsets.right);
        }

        // get position of currentNodes representative in the layered graph
        KVector position = node.getPosition();

        // position of representative in the layered graph is not relative
        // to the parent node for nodes whose originals are not direct children of the layout node -
        // calculate relative position
        if (!(original.getParent() == layeredGraph.getProperty(Properties.ORIGIN))) {
            KVector parentRepPos = parentRepresentative.getPosition();
            KVector pointOfOrigin = new KVector(parentRepPos.x, parentRepPos.y);
            pointOfOrigin.x += insetsParent.getLeft();
            pointOfOrigin.y += insetsParent.getTop();

            // compute and set the node's relative positioning
            float relativeX = (float) (position.x - pointOfOrigin.x);
            float relativeY = (float) (position.y - pointOfOrigin.y);
            nodeLayout.setPos(relativeX, relativeY);
        } else {
            // for nodes that are direct children of the layout node, only the border spacing of the
            // drawing and the graph's offset have to be respected
            KVector graphOffset = layeredGraph.getOffset();
            float newX = (float) (position.x + graphBorderSpacing + graphOffset.x);
            float newY = (float) (position.y + graphBorderSpacing + graphOffset.y);
            nodeLayout.setPos(newX, newY);
        }

    }

    /**
     * Applies layout to the ports of the given leave node.
     * 
     * @param kNode
     *            KNode whose layout is to be updated.
     * @param layeredGraph
     *            The complete layered graph.
     * @param representative
     *            LNode which represents the kNode in the layered graph.
     */
    private void applyPortLayout(final KNode kNode, final LGraph layeredGraph,
            final LNode representative) {
        
        KShapeLayout nodeLayout = kNode.getData(KShapeLayout.class);

        // set port positions
        if (!nodeLayout.getProperty(LayoutOptions.PORT_CONSTRAINTS).isPosFixed()
                || !nodeLayout.getProperty(LayoutOptions.SIZE_CONSTRAINT).isEmpty()) {
            
            for (LPort lport : representative.getPorts()) {
                Object origin = lport.getProperty(Properties.ORIGIN);
                
                if (origin instanceof KPort) {
                    KShapeLayout portLayout = ((KPort) origin).getData(KShapeLayout.class);
                    portLayout.applyVector(lport.getPosition());
                    portLayout.setProperty(LayoutOptions.PORT_SIDE, lport.getSide());
                }
            }
        }
        
        if (representative.getProperty(LayoutOptions.PORT_LABEL_PLACEMENT) != PortLabelPlacement.FIXED) {
            for (LPort lport : representative.getPorts()) {
                for (LLabel label : lport.getLabels()) {
                    KLabel klabel = (KLabel) label.getProperty(Properties.ORIGIN);
                    KShapeLayout klabelLayout = klabel.getData(KShapeLayout.class);
                    klabelLayout.applyVector(label.getPosition());
                }
            }
        }
    }

    /**
     * Applies layout to the ports of a given compound node.
     * 
     * @param kNode
     *            compound kNode, whose layout is to be updated.
     * @param layeredGraph
     *            complete layered graph.
     * @param representative
     *            upper compound border node representing the compound node.
     */
    private void compoundApplyPortLayout(final KNode kNode, final LGraph layeredGraph,
            final LNode representative) {
        
        boolean placePortLabels = representative.getProperty(LayoutOptions.PORT_LABEL_PLACEMENT)
                != PortLabelPlacement.FIXED;
        
        for (LPort lport : representative.getPorts()) {
            Object origin = lport.getProperty(Properties.ORIGIN);
            if (origin instanceof KPort) {
                KShapeLayout portLayout = ((KPort) origin).getData(KShapeLayout.class);
                portLayout.setPos((float) (lport.getPosition().x - (lport.getSize().x / 2)),
                        (float) (lport.getPosition().y - (lport.getSize().y / 2)));
                
                if (placePortLabels) {
                    for (LLabel label : lport.getLabels()) {
                        KLabel klabel = (KLabel) label.getProperty(Properties.ORIGIN);
                        KShapeLayout klabelLayout = klabel.getData(KShapeLayout.class);
                        klabelLayout.applyVector(label.getPosition());
                    }
                }
            }
        }
    }

    /**
     * Calculates the absolute coordinates of a KNode towards nullpoint plus its insets.
     * 
     * @param kNode
     *            kNode, whose distance to nullpoint is to be calculated.
     * @return absolute coordinate of KNode.
     */
    private KVector getAbsolute(final KNode kNode) {
        KShapeLayout nodeLayout = kNode.getData(KShapeLayout.class);
        KVector purePosition = new KVector(nodeLayout.getXpos(), nodeLayout.getYpos());
        KVector insets = new KVector(nodeLayout.getInsets().getLeft(), nodeLayout.getInsets()
                .getTop());
        KVector position = new KVector(0, 0);
        position.add(purePosition);
        position.add(insets);
        if (kNode.getParent().getParent() == null) {
            return position;
        } else {
            return getAbsolute(kNode.getParent()).add(position);
        }
    }

    /**
     * Rebuilds the inclusion tree of the KGraph with the help of Properties of the LGraphElements.
     * Each node sets its parent-property and inserts itself into the children-list of its parent.
     * Recursive method.
     * 
     * @param layeredGraph
     *            The LayeredGraph representation of the graph to be laid out.
     * @param knode
     *            The current node.
     */
    private void createInclusionTree(final LGraph layeredGraph, final KNode knode) {
        // get the layeredGraph's element map
        Map<KGraphElement, LGraphElement> elemMap = layeredGraph.getProperty(Properties.ELEMENT_MAP);
        // get the knode's representative in the layeredGraph
        LGraphElement representative = elemMap.get(knode);
        // set the children-property for the representative
        LinkedList<LNode> children = new LinkedList<LNode>();
        representative.setProperty(Properties.CHILDREN, children);
        // get the knode's parent node
        KNode parent = knode.getParent();
        // if knode is not the layoutNode, insert the nodes representative in the children list of
        // parent. Set own parent property.
        if (parent != null) {
            LGraphElement parentRep = elemMap.get(parent);
            representative.setProperty(Properties.PARENT, parentRep);
            parentRep.getProperty(Properties.CHILDREN).add((LNode) representative);
        }
        EList<KNode> ownChildren = knode.getChildren();
        for (KNode child : ownChildren) {
            createInclusionTree(layeredGraph, child);
        }
    }

}
