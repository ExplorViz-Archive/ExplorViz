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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KGraphElement;
import de.cau.cs.kieler.core.kgraph.KLabel;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.kiml.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.klayoutdata.KInsets;
import de.cau.cs.kieler.kiml.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.EdgeLabelPlacement;
import de.cau.cs.kieler.kiml.options.EdgeRouting;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortLabelPlacement;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.kiml.options.SizeOptions;
import de.cau.cs.kieler.kiml.util.KimlUtil;
import de.cau.cs.kieler.klay.layered.Util;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LGraphElement;
import de.cau.cs.kieler.klay.layered.graph.LGraphElement.HashCodeCounter;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.p3order.CrossingMinimizationStrategy;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Manages the transformation of KGraphs to LayeredGraphs. Sets the
 * {@link Properties#GRAPH_PROPERTIES} property on imported graphs.
 * 
 * @author msp
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public class KGraphImporter extends AbstractGraphImporter<KNode> {
    
    /**
     * Creates a graph importer with the given hash code counter.
     * 
     * @param counter the hash code counter used to determine hash codes of graph elements
     */
    public KGraphImporter(final HashCodeCounter counter) {
        super(counter);
    }

    // //////////////////////////////////////////////////////////////////////////////
    // Transformation KGraph -> LGraph
    
    /** the minimal spacing between edges, so edges won't overlap. */
    private static final float MIN_EDGE_SPACING = 2.0f;

    /**
     * {@inheritDoc}
     */
    public LGraph importGraph(final KNode kgraph) {
        layeredGraph = new LGraph(hashCodeCounter);
        layeredGraph.setProperty(Properties.ORIGIN, kgraph);

        KShapeLayout sourceShapeLayout = kgraph.getData(KShapeLayout.class);

        // copy the properties of the KGraph to the layered graph and check values
        layeredGraph.copyProperties(sourceShapeLayout);
        layeredGraph.checkProperties(Properties.OBJ_SPACING, Properties.BORDER_SPACING,
                Properties.THOROUGHNESS, Properties.ASPECT_RATIO);
        float spacing = layeredGraph.getProperty(Properties.OBJ_SPACING);
        if (spacing < 1) {
            // Spacing values below 0 are transformed to the default value by checkProperties(..)
            // Values between 0 and 1 are normalized to 1 here.
            spacing = 1;
            layeredGraph.setProperty(Properties.OBJ_SPACING, spacing);
        }
        if (layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR) * spacing < MIN_EDGE_SPACING) {
            // Edge spacing is determined by the product of object spacing and edge spacing factor.
            // Make sure the resulting edge spacing is at least 2 in order to avoid overlapping edges.
            layeredGraph.setProperty(Properties.EDGE_SPACING_FACTOR, MIN_EDGE_SPACING / spacing);
        }
        Direction direction = layeredGraph.getProperty(LayoutOptions.DIRECTION);
        if (direction == Direction.UNDEFINED) {
            // The default layout direction is right.
            direction = Direction.RIGHT;
            layeredGraph.setProperty(LayoutOptions.DIRECTION, direction);
        }

        // copy the insets to the layered graph
        KInsets kinsets = sourceShapeLayout.getInsets();
        LInsets linsets = layeredGraph.getInsets();

        linsets.left = kinsets.getLeft();
        linsets.right = kinsets.getRight();
        linsets.top = kinsets.getTop();
        linsets.bottom = kinsets.getBottom();

        // keep a list of created nodes in the layered graph, as well as a map
        // between KGraph
        // nodes / ports and LGraph nodes / ports
        Map<KGraphElement, LGraphElement> elemMap = new HashMap<KGraphElement, LGraphElement>();

        // the graph properties discovered during the transformations
        EnumSet<GraphProperties> graphProperties = EnumSet.noneOf(GraphProperties.class);
        layeredGraph.setProperty(Properties.GRAPH_PROPERTIES, graphProperties);

        // method will be called from the subclass CompoundKGraphImporter. The following part is not
        // to be executed in this case.
        boolean isCompound = sourceShapeLayout.getProperty(LayoutOptions.LAYOUT_HIERARCHY);
        if (!isCompound) {
            // transform everything
            transformNodesAndPorts(kgraph, elemMap);
            transformEdges(kgraph, elemMap);
        }

        layeredGraph.setProperty(Properties.ELEMENT_MAP, elemMap);
        return layeredGraph;
    }

    /**
     * Transforms the nodes and ports defined by the given layout node.
     * 
     * @param graph
     *            the original graph.
     * @param elemMap
     *            the element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     */
    private void transformNodesAndPorts(final KNode graph,
            final Map<KGraphElement, LGraphElement> elemMap) {
        Set<GraphProperties> graphProperties = layeredGraph.getProperty(Properties.GRAPH_PROPERTIES);
        List<LNode> layeredNodes = layeredGraph.getLayerlessNodes();

        KShapeLayout layoutNodeLayout = graph.getData(KShapeLayout.class);
        KVector layoutNodeSize = new KVector(layoutNodeLayout.getWidth(),
                layoutNodeLayout.getHeight());

        // Find out whether there are external ports or ports with multiple incident
        // edges that need to be considered
        List<KPort> ports = graph.getPorts();
        for (KPort kport : ports) {
            int hierarchicalEdges = 0;

            for (KEdge kedge : kport.getEdges()) {
                if (graph.equals(kedge.getSource().getParent())
                        || graph.equals(kedge.getTarget().getParent())) {
                    hierarchicalEdges++;
                }
            }

            if (hierarchicalEdges > 0) {
                graphProperties.add(GraphProperties.EXTERNAL_PORTS);
            }

            if (hierarchicalEdges > 1) {
                graphProperties.add(GraphProperties.HYPEREDGES);
            }
        }

        // Transform the external ports
        Direction direction = layeredGraph.getProperty(LayoutOptions.DIRECTION);
        if (graphProperties.contains(GraphProperties.EXTERNAL_PORTS)) {
            for (KPort kport : ports) {
                transformExternalPort(kport, layeredNodes, graph, layoutNodeSize, elemMap,
                        direction);
            }
        }

        // Now transform the node's children
        for (KNode child : graph.getChildren()) {
            transformNode(child, layeredNodes, elemMap, graphProperties, direction);
        }

    }

    /**
     * Transforms the given external port into a dummy node.
     * 
     * @param kport
     *            the port.
     * @param layeredNodes
     *            the list of nodes to add the dummy node to.
     * @param graph
     *            the original graph.
     * @param layoutNodeSize
     *            the layout node's size.
     * @param elemMap
     *            the element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     * @param direction
     *            the overall layout direction
     */
    private void transformExternalPort(final KPort kport, final List<LNode> layeredNodes,
            final KNode graph, final KVector layoutNodeSize,
            final Map<KGraphElement, LGraphElement> elemMap, final Direction direction) {

        KShapeLayout graphLayout = graph.getData(KShapeLayout.class);
        KShapeLayout kportLayout = kport.getData(KShapeLayout.class);

        // Calculate the position of the port's center
        KVector kportPosition = new KVector(kportLayout.getXpos() + kportLayout.getWidth() / 2.0,
                kportLayout.getYpos() + kportLayout.getHeight() / 2.0);

        // Count the number of incoming and outgoing edges
        int inEdges = 0, outEdges = 0;
        for (KEdge edge : kport.getEdges()) {
            if (edge.getSourcePort() == kport && edge.getTarget().getParent() == graph) {
                outEdges++;
            }
            if (edge.getTargetPort() == kport && edge.getSource().getParent() == graph) {
                inEdges++;
            }
        }

        // Create dummy
        KShapeLayout portLayout = kport.getData(KShapeLayout.class);
        PortSide portSide = portLayout.getProperty(LayoutOptions.PORT_SIDE);
        Float offset = portLayout.getProperty(LayoutOptions.OFFSET);
        PortConstraints portConstraints = graphLayout.getProperty(LayoutOptions.PORT_CONSTRAINTS);
        
        if (portSide == PortSide.UNDEFINED) {
            portSide = KimlUtil.calcPortSide(kport, direction);
            portLayout.setProperty(LayoutOptions.PORT_SIDE, portSide);
        }
        
        if (offset == null) {
            // if port coordinates are (0,0), we default to port offset 0 to make the common case
            // frustration-free
            if (portLayout.getXpos() == 0.0f && portLayout.getYpos() == 0.0f) {
                offset = 0.0f;
            } else {
                offset = KimlUtil.calcPortOffset(kport, portSide);
            }
            portLayout.setProperty(LayoutOptions.OFFSET, offset);
        }
        
        LNode dummy = createExternalPortDummy(
                kport,
                portConstraints,
                portSide,
                inEdges - outEdges,
                layoutNodeSize,
                kportPosition,
                new KVector(kportLayout.getWidth(), kportLayout.getHeight()),
                direction);
        layeredNodes.add(dummy);
        elemMap.put(kport, dummy);
    }

    /**
     * Transforms the given node.
     * 
     * @param node
     *            the node to transform.
     * @param layeredNodes
     *            the list of nodes to add the transformed node to.
     * @param elemMap
     *            the element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     * @param graphProperties
     *            graph properties updated during the transformation.
     * @param direction
     *            the overall layout direction
     */
    protected void transformNode(final KNode node, final List<LNode> layeredNodes,
            final Map<KGraphElement, LGraphElement> elemMap,
            final Set<GraphProperties> graphProperties, final Direction direction) {
        KShapeLayout nodeLayout = node.getData(KShapeLayout.class);

        // add a new node to the layered graph, copying its position
        LNode newNode = new LNode(layeredGraph);
        newNode.setProperty(Properties.ORIGIN, node);
        newNode.getPosition().x = nodeLayout.getXpos();
        newNode.getPosition().y = nodeLayout.getYpos();

        layeredNodes.add(newNode);
        elemMap.put(node, newNode);

        // port constraints and sides cannot be undefined
        PortConstraints portConstraints = nodeLayout.getProperty(LayoutOptions.PORT_CONSTRAINTS);
        if (portConstraints == PortConstraints.UNDEFINED) {
            portConstraints = PortConstraints.FREE;
        } else if (portConstraints != PortConstraints.FREE) {
            // if the port constraints are not free, set the appropriate graph property
            graphProperties.add(GraphProperties.NON_FREE_PORTS);
        }

        // transform the ports
        for (KPort kport : node.getPorts()) {
            KShapeLayout portLayout = kport.getData(KShapeLayout.class);

            // find out if there are hyperedges, that is a set of edges connected to the same port
            if (kport.getEdges().size() > 1) {
                graphProperties.add(GraphProperties.HYPEREDGES);
            }

            // create layered port, copying its position
            LPort newPort = new LPort(layeredGraph);
            newPort.setProperty(Properties.ORIGIN, kport);
            KVector portSize = newPort.getSize();
            portSize.x = portLayout.getWidth();
            portSize.y = portLayout.getHeight();
            KVector portPos = newPort.getPosition();
            portPos.x = portLayout.getXpos();
            portPos.y = portLayout.getYpos();
            newPort.setNode(newNode);

            PortSide portSide = portLayout.getProperty(LayoutOptions.PORT_SIDE);
            Float offset = portLayout.getProperty(LayoutOptions.OFFSET);
            if (portSide == PortSide.UNDEFINED) {
                // calculate the port side and offset from the port's current position
                portSide = KimlUtil.calcPortSide(kport, direction);
                portLayout.setProperty(LayoutOptions.PORT_SIDE, portSide);
                
                if (offset == null && portSide != PortSide.UNDEFINED) {
                    // if port coordinates are (0,0), we default to port offset 0 to make the common case
                    // frustration-free
                    if (portLayout.getXpos() != 0.0f || portLayout.getYpos() != 0.0f) {
                        offset = KimlUtil.calcPortOffset(kport, portSide);
                    }
                }
            }

            newPort.setSide(portSide);
            // the offset can be null when the port side has been constrained, so probably we
            // cannot derive the offset from the port's current position --> keep default value 0
            if (offset != null) {
                newPort.setProperty(Properties.OFFSET, offset);
            }
            newPort.setProperty(LayoutOptions.PORT_INDEX,
                    portLayout.getProperty(LayoutOptions.PORT_INDEX));
            
            // if the port anchor property is set, use it as anchor point
            KVector anchorPos = portLayout.getProperty(Properties.PORT_ANCHOR);
            if (anchorPos != null) {
                newPort.getAnchor().x = anchorPos.x;
                newPort.getAnchor().y = anchorPos.y;
            } else if (portConstraints.isSideFixed() && portSide != PortSide.UNDEFINED) {
                // set the anchor point according to the port side
                switch (portSide) {
                case NORTH:
                    newPort.getAnchor().x = portSize.x / 2;
                    break;
                case EAST:
                    newPort.getAnchor().x = portSize.x;
                    newPort.getAnchor().y = portSize.y / 2;
                    break;
                case SOUTH:
                    newPort.getAnchor().x = portSize.x / 2;
                    newPort.getAnchor().y = portSize.y;
                    break;
                case WEST:
                    newPort.getAnchor().y = portSize.y / 2;
                    break;
                }
            } else {
                // the port side will be decided later, so set the anchor to the center point
                newPort.getAnchor().x = portSize.x / 2;
                newPort.getAnchor().y = portSize.y / 2;
            }
            
            // if the port constraints are set to fixed ratio, remember the current ratio
            if (portConstraints.isRatioFixed()) {
                double ratio = 0.0;
                
                switch (portSide) {
                case NORTH:
                case SOUTH:
                    ratio = portPos.x / nodeLayout.getWidth();
                    break;
                case EAST:
                case WEST:
                    ratio = portPos.y / nodeLayout.getHeight();
                    break;
                }
                
                newPort.setProperty(Properties.PORT_RATIO_OR_POSITION, ratio);
            }

            elemMap.put(kport, newPort);

            // create the port's labels
            for (KLabel klabel : kport.getLabels()) {
                KShapeLayout labelLayout = klabel.getData(KShapeLayout.class);

                LLabel newLabel = new LLabel(layeredGraph, klabel.getText());
                newLabel.setProperty(Properties.ORIGIN, klabel);
                newLabel.getSize().x = labelLayout.getWidth();
                newLabel.getSize().y = labelLayout.getHeight();
                newLabel.getPosition().x = labelLayout.getXpos();
                newLabel.getPosition().y = labelLayout.getYpos();
                newPort.getLabels().add(newLabel);
            }

            switch (direction) {
            case LEFT:
            case RIGHT:
                if (portSide == PortSide.NORTH || portSide == PortSide.SOUTH) {
                    graphProperties.add(GraphProperties.NORTH_SOUTH_PORTS);
                }
                break;
            case UP:
            case DOWN:
                if (portSide == PortSide.EAST || portSide == PortSide.WEST) {
                    graphProperties.add(GraphProperties.NORTH_SOUTH_PORTS);
                }
                break;
            }
        }

        // set the size of the new node AFTER ports have been created, since the original size
        // is required for port side and offset calculation
//        KVector ratio = KimlUtil.resizeNode(node);
//        if (ratio != null && (ratio.x != 1 || ratio.y != 1)) {
//            newNode.setProperty(Properties.RESIZE_RATIO, ratio);
//        }
        newNode.getSize().x = nodeLayout.getWidth();
        newNode.getSize().y = nodeLayout.getHeight();

        // add the node's labels
        for (KLabel klabel : node.getLabels()) {
            KShapeLayout labelLayout = klabel.getData(KShapeLayout.class);
            LLabel newLabel = new LLabel(layeredGraph, klabel.getText());
            newLabel.setProperty(Properties.ORIGIN, klabel);
            newLabel.getSize().x = labelLayout.getWidth();
            newLabel.getSize().y = labelLayout.getHeight();
            newLabel.getPosition().x = labelLayout.getXpos();
            newLabel.getPosition().y = labelLayout.getYpos();
            newNode.getLabels().add(newLabel);
        }

        // set properties of the new node
        newNode.copyProperties(nodeLayout);

        if (newNode.getProperty(LayoutOptions.COMMENT_BOX)) {
            graphProperties.add(GraphProperties.COMMENTS);
        }

        // if we have a hypernode without ports, create a default input and output port
        if (newNode.getProperty(LayoutOptions.HYPERNODE)) {
            graphProperties.add(GraphProperties.HYPERNODES);
            graphProperties.add(GraphProperties.HYPEREDGES);
            newNode.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FREE);
        }
    }

    /**
     * Transforms the edges defined by the given layout node.
     * 
     * @param graph
     *            the original graph.
     * @param elemMap
     *            the element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     */
    private void transformEdges(final KNode graph, final Map<KGraphElement, LGraphElement> elemMap) {

        // Transform external port edges
        transformExternalPortEdges(graph, graph.getIncomingEdges(), elemMap);
        transformExternalPortEdges(graph, graph.getOutgoingEdges(), elemMap);

        // Transform edges originating in the layout node's children
        for (KNode child : graph.getChildren()) {
            for (KEdge kedge : child.getOutgoingEdges()) {
                // exclude edges that pass hierarchy bounds (except for those
                // going into an external port)
                if (kedge.getTarget().getParent() == child.getParent()) {
                    transformEdge(kedge, graph, elemMap);
                }
            }
        }
    }

    /**
     * Transforms the given list of edges connected to the layout node's external ports.
     * 
     * @param graph
     *            the original graph.
     * @param edges
     *            the list of edges, each connected to an external port of the layout node.
     * @param elemMap
     *            the element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     */
    private void transformExternalPortEdges(final KNode graph, final List<KEdge> edges,
            final Map<KGraphElement, LGraphElement> elemMap) {

        for (KEdge kedge : edges) {
            // Only transform edges going into the layout node's direct children
            // (self-loops of the layout node will be processed on level higher)
            if (kedge.getSource().getParent() == graph || kedge.getTarget().getParent() == graph) {

                transformEdge(kedge, graph, elemMap);
            }
        }
    }

    /**
     * Transforms the given edge.
     * 
     * @param kedge
     *            the edge to transform.
     * @param graph
     *            the original graph.
     * @param elemMap
     *            the element map that maps the original {@code KGraph} elements to the transformed
     *            {@code LGraph} elements.
     */
    protected void transformEdge(final KEdge kedge, final KNode graph,
            final Map<KGraphElement, LGraphElement> elemMap) {
        KEdgeLayout edgeLayout = kedge.getData(KEdgeLayout.class);
        boolean isCompound = graph.getData(KShapeLayout.class).getProperty(
                LayoutOptions.LAYOUT_HIERARCHY);

        // create a layered edge
        LEdge newEdge = new LEdge(layeredGraph);
        newEdge.setProperty(Properties.ORIGIN, kedge);

        // the following is not needed in case of compound graph handling, as source and target will
        // be set by calling function.

        if (!isCompound) {
            // retrieve source and target
            LNode sourceNode = null;
            LPort sourcePort = null;
            LNode targetNode = null;
            LPort targetPort = null;

            // check if the edge source is an external port
            if (kedge.getSource() == graph && kedge.getSourcePort() != null) {
                sourceNode = (LNode) elemMap.get(kedge.getSourcePort());
                // the port could be missing in the element map if kedge is not in the port's edge list
                if (sourceNode != null) {
                    sourcePort = sourceNode.getPorts().get(0);
                }
            } else {
                sourceNode = (LNode) elemMap.get(kedge.getSource());
                sourcePort = (LPort) elemMap.get(kedge.getSourcePort());
            }

            // check if the edge target is an external port
            if (kedge.getTarget() == graph && kedge.getTargetPort() != null) {
                targetNode = (LNode) elemMap.get(kedge.getTargetPort());
                // the port could be missing in the element map if kedge is not in the port's edge list
                if (targetNode != null) {
                    targetPort = targetNode.getPorts().get(0);
                }
            } else {
                targetNode = (LNode) elemMap.get(kedge.getTarget());
                targetPort = (LPort) elemMap.get(kedge.getTargetPort());
            }
            
            if (sourceNode != null && targetNode != null) {
                // if we have a self-loop, set the appropriate graph property
                if (sourceNode == targetNode) {
                    Set<GraphProperties> graphProperties = layeredGraph.getProperty(
                            Properties.GRAPH_PROPERTIES);
                    graphProperties.add(GraphProperties.SELF_LOOPS);
                }
    
                // create source and target ports if they do not exist yet
                if (sourcePort == null) {
                    sourcePort = createPort(sourceNode, edgeLayout.getSourcePoint(), PortType.OUTPUT);
                }
                
                if (targetPort == null) {
                    targetPort = createPort(targetNode, edgeLayout.getTargetPoint(), PortType.INPUT);
                }
                newEdge.setSource(sourcePort);
                newEdge.setTarget(targetPort);
            }
        }

        // transform the edge's labels
        for (KLabel klabel : kedge.getLabels()) {
            KShapeLayout labelLayout = klabel.getData(KShapeLayout.class);
            LLabel newLabel = new LLabel(layeredGraph, klabel.getText());
            newLabel.getPosition().x = labelLayout.getXpos();
            newLabel.getPosition().y = labelLayout.getYpos();
            newLabel.getSize().x = labelLayout.getWidth();
            newLabel.getSize().y = labelLayout.getHeight();
            newLabel.setProperty(Properties.ORIGIN, klabel);
            newLabel.setProperty(LayoutOptions.EDGE_LABEL_PLACEMENT,
                    labelLayout.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT));
            newEdge.getLabels().add(newLabel);
            if (labelLayout.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                    == EdgeLabelPlacement.CENTER) {
                // Add annotation if graph contains labels which shall be placed
                // in the middle of an edge
                Set<GraphProperties> graphProperties = layeredGraph.getProperty(
                        Properties.GRAPH_PROPERTIES);
                graphProperties.add(GraphProperties.CENTER_LABELS);
            }
            if (labelLayout.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                    == EdgeLabelPlacement.HEAD
                || labelLayout.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                    == EdgeLabelPlacement.TAIL) {
                // Add annotation if graph contains labels which shall be placed
                // in the middle of an edge
                Set<GraphProperties> graphProperties = layeredGraph.getProperty(
                        Properties.GRAPH_PROPERTIES);
                graphProperties.add(GraphProperties.END_LABELS);
            }
        }
        
        // copy the bend points of the edge if they are needed by anyone
        if (layeredGraph.getProperty(Properties.CROSS_MIN)
                == CrossingMinimizationStrategy.INTERACTIVE
                && !edgeLayout.getBendPoints().isEmpty()) {
            KVectorChain bendpoints = new KVectorChain();
            for (KPoint point : edgeLayout.getBendPoints()) {
                bendpoints.add(point.createVector());
            }
            newEdge.setProperty(Properties.ORIGINAL_BENDPOINTS, bendpoints);
        }

        // set properties of the new edge
        newEdge.copyProperties(edgeLayout);
        // clear junction points, since they are recalculated from scratch
        newEdge.setProperty(LayoutOptions.JUNCTION_POINTS, null);
        
        // method will be called from the subclass CompoundKGraphImporter. The following part is
        // to be executed in this case.
        if (isCompound) {
            // put edge to elementMap
            elemMap.put(kedge, newEdge);
        }
    }

    /**
     * Create a port for an edge that is not connected to a port. This is necessary because KLay
     * Layered wants all edges to have a source port and a target port.
     * 
     * @param node
     *            the node at which the edge is incident
     * @param endPoint
     *            the absolute point where the edge ends
     * @param type
     *            the port type
     * @return a new port
     */
    private LPort createPort(final LNode node, final KPoint endPoint, final PortType type) {
        LPort port;
        Direction direction = layeredGraph.getProperty(LayoutOptions.DIRECTION);
        boolean mergePorts = layeredGraph.getProperty(Properties.MERGE_PORTS);
        
        if ((mergePorts || node.getProperty(LayoutOptions.HYPERNODE))
                && !node.getProperty(LayoutOptions.PORT_CONSTRAINTS).isSideFixed()) {
            
            // Hypernodes have one output port and one input port
            final PortSide defaultSide = PortSide.fromDirection(direction);
            port = Util.provideCollectorPort(layeredGraph, node, type,
                    type == PortType.OUTPUT ? defaultSide : defaultSide.opposed());
        } else {
            port = new LPort(layeredGraph);
            port.setNode(node);
            
            KVector pos = port.getPosition();
            pos.x = endPoint.getX() - node.getPosition().x;
            pos.y = endPoint.getY() - node.getPosition().y;
            
            KVector resizeRatio = node.getProperty(Properties.RESIZE_RATIO);
            if (resizeRatio != null) {
                pos.scale(resizeRatio.x, resizeRatio.y);
            }
            pos.applyBounds(0, 0, node.getSize().x, node.getSize().y);
            
            PortSide portSide = calcPortSide(node, port);
            port.setSide(portSide);
            Set<GraphProperties> graphProperties = layeredGraph.getProperty(
                    Properties.GRAPH_PROPERTIES);
            switch (direction) {
            case LEFT:
            case RIGHT:
                if (portSide == PortSide.NORTH || portSide == PortSide.SOUTH) {
                    graphProperties.add(GraphProperties.NORTH_SOUTH_PORTS);
                }
                break;
            case UP:
            case DOWN:
                if (portSide == PortSide.EAST || portSide == PortSide.WEST) {
                    graphProperties.add(GraphProperties.NORTH_SOUTH_PORTS);
                }
                break;
            }
        }
        
        return port;
    }

    /**
     * Calculate the port side from the relative position.
     * 
     * @param node
     *            a node
     * @param port
     *            a port of that node
     * @return the side of the node on which the port is situated
     */
    private static PortSide calcPortSide(final LNode node, final LPort port) {
        double widthPercent = port.getPosition().x / node.getSize().x;
        double heightPercent = port.getPosition().y / node.getSize().y;
        if (widthPercent + heightPercent <= 1 && widthPercent - heightPercent <= 0) {
            // port is on the left
            return PortSide.WEST;
        } else if (widthPercent + heightPercent >= 1 && widthPercent - heightPercent >= 0) {
            // port is on the right
            return PortSide.EAST;
        } else if (heightPercent < 1.0f / 2) {
            // port is on the top
            return PortSide.NORTH;
        } else {
            // port is on the bottom
            return PortSide.SOUTH;
        }
    }

    // //////////////////////////////////////////////////////////////////////////////
    // Apply Layout Results

    /**
     * {@inheritDoc}
     */
    public void applyLayout(final LGraph layeredGraph) {
        Object target = layeredGraph.getProperty(Properties.ORIGIN);
        if (!(target instanceof KNode)) {
            return;
        }
        KNode parentNode = (KNode) target;
        
        // determine the border spacing, which influences the offset
        KShapeLayout parentLayout = parentNode.getData(KShapeLayout.class);
        float borderSpacing = layeredGraph.getProperty(Properties.BORDER_SPACING);

        // calculate the offset
        KVector offset = new KVector(borderSpacing + layeredGraph.getOffset().x, borderSpacing
                + layeredGraph.getOffset().y);

        // along the way, we collect the list of edges to be processed later
        List<LEdge> edgeList = new LinkedList<LEdge>();

        // process the nodes
        for (LNode lnode : layeredGraph.getLayerlessNodes()) {
            Object origin = lnode.getProperty(Properties.ORIGIN);

            if (origin instanceof KNode) {
                // set the node position
                KNode knode = (KNode) origin;
                KShapeLayout nodeLayout = knode.getData(KShapeLayout.class);

                nodeLayout.setXpos((float) (lnode.getPosition().x + offset.x));
                nodeLayout.setYpos((float) (lnode.getPosition().y + offset.y));
                
                // set the node size, if necessary
                if (!nodeLayout.getProperty(LayoutOptions.SIZE_CONSTRAINT).isEmpty()) {
                    nodeLayout.setWidth((float) lnode.getSize().x);
                    nodeLayout.setHeight((float) lnode.getSize().y);
                }

                // set port positions
                if (!nodeLayout.getProperty(LayoutOptions.PORT_CONSTRAINTS).isPosFixed()
                        || !nodeLayout.getProperty(LayoutOptions.SIZE_CONSTRAINT).isEmpty()) {
                    
                    for (LPort lport : lnode.getPorts()) {
                        origin = lport.getProperty(Properties.ORIGIN);
                        if (origin instanceof KPort) {
                            KPort kport = (KPort) origin;
                            KShapeLayout portLayout = kport.getData(KShapeLayout.class);
                            portLayout.applyVector(lport.getPosition());
                            portLayout.setProperty(LayoutOptions.PORT_SIDE, lport.getSide());
                        }
                    }
                }
                
                // set label positions, if they were not fixed
                if (!lnode.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT).isEmpty()) {
                    for (LLabel llabel : lnode.getLabels()) {
                        KLabel klabel = (KLabel) llabel.getProperty(Properties.ORIGIN);
                        KShapeLayout klabelLayout = klabel.getData(KShapeLayout.class);
                        klabelLayout.applyVector(llabel.getPosition());
                    }
                }
                
                // set port labels, if they were not fixed
                if (lnode.getProperty(LayoutOptions.PORT_LABEL_PLACEMENT) != PortLabelPlacement.FIXED) {
                    for (LPort lport : lnode.getPorts()) {
                        for (LLabel label : lport.getLabels()) {
                            KLabel klabel = (KLabel) label.getProperty(Properties.ORIGIN);
                            KShapeLayout klabelLayout = klabel.getData(KShapeLayout.class);
                            klabelLayout.applyVector(label.getPosition());
                        }
                    }
                }
                
                // set node insets, if requested
                if (nodeLayout.getProperty(LayoutOptions.SIZE_OPTIONS)
                        .contains(SizeOptions.COMPUTE_INSETS)) {
                    
                    // Apply insets
                    LInsets lInsets = lnode.getInsets();
                    KInsets kInsets = nodeLayout.getInsets();
                    kInsets.setBottom((float) lInsets.bottom);
                    kInsets.setTop((float) lInsets.top);
                    kInsets.setLeft((float) lInsets.left);
                    kInsets.setRight((float) lInsets.right);
                }
            } else if (origin instanceof KPort) {
                // It's an external port. Set its position
                KPort kport = (KPort) origin;
                KShapeLayout portLayout = kport.getData(KShapeLayout.class);
                KVector portPosition = getExternalPortPosition(layeredGraph, lnode,
                        portLayout.getWidth(), portLayout.getHeight());
                portLayout.applyVector(portPosition);
            }

            // collect edges
            for (LPort port : lnode.getPorts()) {
                edgeList.addAll(port.getOutgoingEdges());
            }
        }

        // check if the edge routing uses splines
        EdgeRouting routing = parentLayout.getProperty(LayoutOptions.EDGE_ROUTING);
        boolean splinesActive = routing == EdgeRouting.SPLINES;
        
        // check if the orthogonal edge router was used
        boolean orthogonalRouting =
                layeredGraph.getProperty(LayoutOptions.EDGE_ROUTING).equals(EdgeRouting.ORTHOGONAL);

        // iterate through all edges
        for (LEdge ledge : edgeList) {
            // Self-loops are currently left untouched unless the edge router is set to
            // the orthogonal router
            if (ledge.isSelfLoop() && !orthogonalRouting) {
                continue;
            }
            
            KEdge kedge = (KEdge) ledge.getProperty(Properties.ORIGIN);
            KEdgeLayout edgeLayout = kedge.getData(KEdgeLayout.class);
            KVectorChain bendPoints = ledge.getBendPoints();

            // add the source port and target port positions to the vector chain
            bendPoints.addFirst(ledge.getSource().getAbsoluteAnchor());
            bendPoints.addLast(ledge.getTarget().getAbsoluteAnchor());

            // translate the bend points by the offset and apply the bend points
            bendPoints.translate(offset);
            edgeLayout.applyVectorChain(bendPoints);

            // apply layout to labels
            for (LLabel label : ledge.getLabels()) {
                KLabel klabel = (KLabel) label.getProperty(Properties.ORIGIN);
                KShapeLayout klabelLayout = klabel.getData(KShapeLayout.class);
                klabelLayout.applyVector(label.getPosition().add(offset));
            }
            
            // copy junction points
            KVectorChain junctionPoints = ledge.getProperty(LayoutOptions.JUNCTION_POINTS);
            if (junctionPoints != null) {
                junctionPoints.translate(offset);
                edgeLayout.setProperty(LayoutOptions.JUNCTION_POINTS, junctionPoints);
            }

            // set spline option
            if (splinesActive) {
                edgeLayout.setProperty(LayoutOptions.EDGE_ROUTING, EdgeRouting.SPLINES);
            }
        }

        // setup the parent node
        KVector actualGraphSize = layeredGraph.getActualSize();
        
        if (layeredGraph.getProperty(Properties.GRAPH_PROPERTIES).contains(
                GraphProperties.EXTERNAL_PORTS)) {
            
            // ports have positions assigned, the graph's size is final
            parentLayout.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
            KimlUtil.resizeNode(
                    parentNode,
                    (float) actualGraphSize.x,
                    (float) actualGraphSize.y,
                    false,
                    true);
        } else {
            // ports have not been positioned yet - leave this for next layouter
            KimlUtil.resizeNode(
                    parentNode,
                    (float) actualGraphSize.x,
                    (float) actualGraphSize.y,
                    true,
                    true);
        }
    }

}
