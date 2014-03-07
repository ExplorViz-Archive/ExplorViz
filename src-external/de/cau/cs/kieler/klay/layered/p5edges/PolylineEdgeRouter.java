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
package de.cau.cs.kieler.klay.layered.p5edges;

import java.util.EnumSet;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KielerMath;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraphUtil;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.intermediate.LayoutProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Edge router module that draws edges with non-orthogonal line segments.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>the graph has a proper layering with
 *     assigned node and port positions; the size of each layer is
 *     correctly set; at least one of the nodes connected by an in-layer
 *     edge is a dummy node.</dd>
 *   <dt>Postcondition:</dt><dd>each node is assigned a horizontal coordinate;
 *     the bend points of each edge are set; the width of the whole graph is set</dd>
 * </dl>
 *
 * @author msp
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class PolylineEdgeRouter implements ILayoutPhase {
    
    /**
     * Predicate that checks whether nodes represent external ports.
     */
    public static final Predicate<LNode> PRED_EXTERNAL_PORT = new Predicate<LNode>() {
        public boolean apply(final LNode node) {
            return node.getProperty(Properties.NODE_TYPE) == NodeType.EXTERNAL_PORT;
        }
    };
    
    /* The basic processing strategy for this phase is empty. Depending on the graph features,
     * dependencies on intermediate processors are added dynamically as follows:
     * 
     * Before phase 1:
     *   - None.
     * 
     * Before phase 2:
     *   - For center edge labels:
     *     - LABEL_DUMMY_INSERTER
     * 
     * Before phase 3:
     *   - For non-free ports:
     *     - NORTH_SOUTH_PORT_PREPROCESSOR
     *     - INVERTED_PORT_PROCESSOR
     *     
     *   - For edge labels:
     *     - LABEL_SIDE_SELECTOR
     *   
     *   - For center edge labels:
     *     - LABEL_SIDE_SELECTOR
     *     - LABEL_DUMMY_SWITCHER
     * 
     * Before phase 4:
     *   - None.
     * 
     * Before phase 5:
     *   - None.
     * 
     * After phase 5:
     *   - For non-free ports:
     *     - NORTH_SOUTH_PORT_POSTPROCESSOR
     *     
     *   - For center edge labels:
     *     - LABEL_DUMMY_REMOVER
     *     
     *   - For end edge labels:
     *     - END_LABEL_PROCESSOR
     */
    
    /** additional processor dependencies for graphs with possible inverted ports. */
    private static final IntermediateProcessingConfiguration INVERTED_PORT_PROCESSING_ADDITIONS =
        new IntermediateProcessingConfiguration(IntermediateProcessingConfiguration.BEFORE_PHASE_3,
                LayoutProcessorStrategy.INVERTED_PORT_PROCESSOR);
    
    /** additional processor dependencies for graphs with northern / southern non-free ports. */
    private static final IntermediateProcessingConfiguration NORTH_SOUTH_PORT_PROCESSING_ADDITIONS =
        new IntermediateProcessingConfiguration(
                // Before Phase 1
                null,
                
                // Before Phase 2
                null,
                
                // Before Phase 3
                EnumSet.of(LayoutProcessorStrategy.NORTH_SOUTH_PORT_PREPROCESSOR),
                
                // Before Phase 4
                null,
                
                // Before Phase 5
                null,
                
                // After Phase 5
                EnumSet.of(LayoutProcessorStrategy.NORTH_SOUTH_PORT_POSTPROCESSOR));
    
    /** additional processor dependencies for graphs with center edge labels. */
    private static final IntermediateProcessingConfiguration CENTER_EDGE_LABEL_PROCESSING_ADDITIONS =
        new IntermediateProcessingConfiguration(
                // Before Phase 1
                null,
                
                // Before Phase 2
                EnumSet.of(LayoutProcessorStrategy.LABEL_DUMMY_INSERTER),
                
                // Before Phase 3
                EnumSet.of(LayoutProcessorStrategy.LABEL_SIDE_SELECTOR,
                           LayoutProcessorStrategy.LABEL_DUMMY_SWITCHER),
                
                // Before Phase 4
                null,
                
                // Before Phase 5
                null,
                
                // After Phase 5
                EnumSet.of(LayoutProcessorStrategy.LABEL_DUMMY_REMOVER));
    
    /** additional processor dependencies for graphs with head or tail edge labels. */
    private static final IntermediateProcessingConfiguration END_EDGE_LABEL_PROCESSING_ADDITIONS =
        new IntermediateProcessingConfiguration(
                // Before Phase 1
                null,
                
                // Before Phase 2
                null,
                
                // Before Phase 3
                EnumSet.of(LayoutProcessorStrategy.LABEL_SIDE_SELECTOR),
                
                // Before Phase 4
                null,
                
                // Before Phase 5
                null,
                
                // After Phase 5
                EnumSet.of(LayoutProcessorStrategy.END_LABEL_PROCESSOR));
    
    /** the minimal vertical difference for creating bend points. */
    private static final double MIN_VERT_DIFF = 1.0;
    /** factor for layer spacing. */
    private static final double LAYER_SPACE_FAC = 0.4;
    
    /**
     * {@inheritDoc}
     */
    public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
            final LGraph graph) {
        
        Set<GraphProperties> graphProperties = graph.getProperty(Properties.GRAPH_PROPERTIES);
        
        // Basic configuration
        IntermediateProcessingConfiguration configuration = new IntermediateProcessingConfiguration();
        
        // Additional dependencies
        if (graphProperties.contains(GraphProperties.NON_FREE_PORTS)
                || graph.getProperty(Properties.FEEDBACK_EDGES)) {
            
            configuration.addAll(INVERTED_PORT_PROCESSING_ADDITIONS);

            if (graphProperties.contains(GraphProperties.NORTH_SOUTH_PORTS)) {
                configuration.addAll(NORTH_SOUTH_PORT_PROCESSING_ADDITIONS);
            }
        }
        
        if (graphProperties.contains(GraphProperties.CENTER_LABELS)) {
            configuration.addAll(CENTER_EDGE_LABEL_PROCESSING_ADDITIONS);
        }
        
        if (graphProperties.contains(GraphProperties.END_LABELS)) {
            configuration.addAll(END_EDGE_LABEL_PROCESSING_ADDITIONS);
        }
        
        return configuration;
    }
    
    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Polyline edge routing", 1);
        
        float nodeSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING);
        float edgeSpaceFac = layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);

        double xpos = 0.0;
        double layerSpacing = 0.0;
        
        // Determine the spacing required for west-side in-layer edges of the first layer
        if (!layeredGraph.getLayers().isEmpty()) {
            double firstDiff = determineNextInLayerDiff(layeredGraph.getLayers().get(0));
            xpos = LAYER_SPACE_FAC * edgeSpaceFac * firstDiff;
        }
        
        // Iterate over the layers
        ListIterator<Layer> layerIter = layeredGraph.getLayers().listIterator();
        while (layerIter.hasNext()) {
            Layer layer = layerIter.next();
            boolean externalLayer = Iterables.all(layer, PRED_EXTERNAL_PORT);
            // The rightmost layer is not given any node spacing
            if (externalLayer && xpos > 0) {
                xpos -= nodeSpacing;
            }
            
            // Set horizontal coordinates for all nodes of the layer
            LGraphUtil.placeNodes(layer, xpos);
            
            double maxVertDiff = 0.0;
            
            // Iterate over the layer's nodes
            for (LNode node : layer) {
                // Calculate the maximal vertical span of output edges. In-layer edges are already
                // routed at this point by inserting bend points appropriately
                double maxOutputYDiff = 0.0;
                for (LEdge outgoingEdge : node.getOutgoingEdges()) {
                    double sourcePos = outgoingEdge.getSource().getAbsoluteAnchor().y;
                    double targetPos = outgoingEdge.getTarget().getAbsoluteAnchor().y;
                    if (layer == outgoingEdge.getTarget().getNode().getLayer()) {
                        // We have an in-layer edge -- route it!
                        routeInLayerEdge(outgoingEdge, xpos, layer.getSize().x,
                                LAYER_SPACE_FAC * edgeSpaceFac * Math.abs(sourcePos - targetPos));
                        if (outgoingEdge.getSource().getSide() == PortSide.WEST) {
                            // West-side in-layer edges have been considered in the previous iteration
                            sourcePos = 0;
                            targetPos = 0;
                        }
                    }
                    maxOutputYDiff = KielerMath.maxd(maxOutputYDiff,
                            targetPos - sourcePos, sourcePos - targetPos);
                }
                
                // Different node types have to be handled differently
                NodeType nodeType = node.getProperty(Properties.NODE_TYPE);
                if (nodeType == NodeType.NORMAL) {
                    processNormalNode(node);
                } else if (nodeType == NodeType.LONG_EDGE) {
                    processLongEdgeDummyNode(node, nodeSpacing, edgeSpaceFac, xpos, maxOutputYDiff);
                } else if (nodeType == NodeType.LABEL) {
                    processLabelDummyNode(node, xpos);
                }
                
                maxVertDiff = Math.max(maxVertDiff, maxOutputYDiff);
            }
            
            // Consider west-side in-layer edges of the next layer
            if (layerIter.hasNext()) {
                double nextDiff = determineNextInLayerDiff(layerIter.next());
                maxVertDiff = Math.max(maxVertDiff, nextDiff);
                layerIter.previous();
            }
            
            // Determine placement of next layer based on the maximal vertical difference (as the
            // maximum vertical difference edges span grows, the layer grows wider to allow enough
            // space for such sloped edges to avoid too harsh angles)
            layerSpacing = LAYER_SPACE_FAC * edgeSpaceFac * maxVertDiff;
            if (!externalLayer && layerIter.hasNext()) {
                layerSpacing += nodeSpacing;
            }
            xpos += layer.getSize().x + layerSpacing;
        }
        
        // Set the graph's horizontal size
        layeredGraph.getSize().x = xpos;
        
        monitor.done();
    }
    
    private double determineNextInLayerDiff(final Layer layer) {
        double nextInLayerDiff = 0.0;
        for (LNode node : layer) {
            for (LEdge outgoingEdge : node.getOutgoingEdges()) {
                if (layer == outgoingEdge.getTarget().getNode().getLayer()
                        && outgoingEdge.getSource().getSide() == PortSide.WEST) {
                    double sourcePos = outgoingEdge.getSource().getAbsoluteAnchor().y;
                    double targetPos = outgoingEdge.getTarget().getAbsoluteAnchor().y;
                    nextInLayerDiff = KielerMath.maxd(nextInLayerDiff,
                            targetPos - sourcePos, sourcePos - targetPos);
                }
            }
        }
        return nextInLayerDiff;
    }

    /**
     * Inserts bend points to edges of this node as appropriate according to the node's margins. This
     * is to ensure that edges don't cross port labels or anything. The edges are first routed
     * horizontally through the node's margin before sloping off to wherever they're going.
     * 
     * @param node the node whose edges to insert bend points for.
     */
    private void processNormalNode(final LNode node) {
        // If the node has a left or right margin, we need to ensure that the edges
        // connected to it are horizontal inside the margin (they might cross port
        // or edge labels otherwise)
        LInsets nodeMargin = node.getMargin();
        
        for (LPort port : node.getPorts()) {
            if (port.getSide() == PortSide.EAST && node.getSize().x + nodeMargin.right
                    > port.getPosition().x + port.getSize().x) {
                // Port is on the eastern side and there is a right margin, so add
                // bend points to the connected edges
                KVector bendPoint = new KVector(
                        node.getPosition().x + node.getSize().x + nodeMargin.right,
                        port.getAbsoluteAnchor().y
                );
                for (LEdge edge : port.getOutgoingEdges()) {
                    if (edge.getTarget().getNode().getLayer() != node.getLayer()
                            && Math.abs(edge.getTarget().getAbsoluteAnchor().y - bendPoint.y)
                            > MIN_VERT_DIFF) {
                        edge.getBendPoints().add(0, bendPoint);
                    }
                }
                for (LEdge edge : port.getIncomingEdges()) {
                    if (edge.getSource().getNode().getLayer() != node.getLayer()
                            && Math.abs(edge.getSource().getAbsoluteAnchor().y - bendPoint.y)
                            > MIN_VERT_DIFF) {
                        edge.getBendPoints().add(bendPoint);
                    }
                }
            } else if (port.getSide() == PortSide.WEST && nodeMargin.left > -port.getPosition().x) {
                // Port is on the eastern side and there is a right margin, so add
                // bend points to the connected edges
                KVector bendPoint = new KVector(
                        node.getPosition().x - nodeMargin.left,
                        port.getAbsoluteAnchor().y
                );
                    
                for (LEdge edge : port.getOutgoingEdges()) {
                    if (edge.getTarget().getNode().getLayer() != node.getLayer()
                            && Math.abs(edge.getTarget().getAbsoluteAnchor().y - bendPoint.y)
                            > MIN_VERT_DIFF) {
                        edge.getBendPoints().add(0, bendPoint);
                    }
                }
                for (LEdge edge : port.getIncomingEdges()) {
                    if (edge.getSource().getNode().getLayer() != node.getLayer()
                            && Math.abs(edge.getSource().getAbsoluteAnchor().y - bendPoint.y)
                            > MIN_VERT_DIFF) {
                        edge.getBendPoints().add(bendPoint);
                    }
                }
            }
        }
    }

    /**
     * Routes the edges connected to a {@code LONG_EDGE} dummy node.
     * 
     * @param node the dummy node whose incident edges to route.
     * @param spacing spacing between objects.
     * @param edgeSpaceFac edge spacing factor.
     * @param xpos the layer's x position.
     * @param maxOutputYDiff the maximal vertical span of output edges connected to the node.
     */
    private void processLongEdgeDummyNode(final LNode node, final float spacing,
            final float edgeSpaceFac, final double xpos, final double maxOutputYDiff) {
        
        // Calculate the maximal vertical span of input edges
        double maxInputYDiff = 0.0;
        for (LPort targetPort : node.getPorts(PortType.INPUT)) {
            double targetPos = targetPort.getAbsoluteAnchor().y;

            // Iterate over the connected source ports
            for (LPort sourcePort : targetPort.getPredecessorPorts()) {
                double sourcePos = sourcePort.getAbsoluteAnchor().y;
                maxInputYDiff = KielerMath.maxd(maxInputYDiff,
                        targetPos - sourcePos, sourcePos - targetPos);
            }
        }
        
        Layer currentLayer = node.getLayer();
        if (maxInputYDiff >= MIN_VERT_DIFF && maxOutputYDiff >= MIN_VERT_DIFF) {
            // Both the incoming and the outgoing edges have significant differences. Check
            // how large the vertical span is in relation to the layer's width and thus
            // determine if we need to insert bend points at all
            double layerSize = node.getLayer().getSize().x;
            double diff = Math.max(maxInputYDiff, maxOutputYDiff);
            double deviation = diff / (layerSize / 2.0 + spacing
                    + LAYER_SPACE_FAC * edgeSpaceFac * diff) * layerSize / 2.0;
            
            if (deviation >= edgeSpaceFac * spacing) {
                // Insert for incoming and outgoing edges
                for (LEdge incoming : node.getIncomingEdges()) {
                    if (currentLayer != incoming.getSource().getNode().getLayer()) {
                        incoming.getBendPoints().add(
                                xpos, node.getPosition().y);
                    }
                }

                for (LEdge outgoing : node.getOutgoingEdges()) {
                    if (currentLayer != outgoing.getTarget().getNode().getLayer()) {
                        outgoing.getBendPoints().add(
                                xpos + layerSize, node.getPosition().y);
                    }
                }
            } else {
                // Insert only for incoming edges in the layer's horizontal center
                for (LEdge incoming : node.getIncomingEdges()) {
                    if (currentLayer != incoming.getSource().getNode().getLayer()) {
                        incoming.getBendPoints().add(
                                xpos + layerSize / 2.0, node.getPosition().y);
                    }
                }
            }
        } else if (maxInputYDiff >= MIN_VERT_DIFF) {
            // Only the incoming edges have significant differences
            for (LEdge incoming : node.getIncomingEdges()) {
                if (currentLayer != incoming.getSource().getNode().getLayer()) {
                    incoming.getBendPoints().add(
                            xpos, node.getPosition().y);
                }
            }
        } else if (maxOutputYDiff >= MIN_VERT_DIFF) {
            // Only the outgoing edges have significant differences
            for (LEdge outgoing : node.getOutgoingEdges()) {
                if (currentLayer != outgoing.getTarget().getNode().getLayer()) {
                    outgoing.getBendPoints().add(
                            xpos + node.getLayer().getSize().x, node.getPosition().y);
                }
            }
        }
    }

    /**
     * Routes edges connected to {@code LABEL} dummy nodes. Bend points are inserted left and right
     * of the node to ensure that the edge doesn't cross the label.
     * 
     * @param node the dummy node whose incident edges to route.
     * @param xpos the layer's x position.
     */
    private void processLabelDummyNode(final LNode node, final double xpos) {
        // Insert bend points left and right of the node so that the label does not
        // overlap the edge. We assume that there's only one input and one output edge,
        // which should be true for label dummy nodes.
        node.getIncomingEdges().iterator().next().getBendPoints().add(
                xpos, node.getPosition().y);
        node.getOutgoingEdges().iterator().next().getBendPoints().add(
                xpos + node.getLayer().getSize().x, node.getPosition().y);
    }
    
    /**
     * Computes the bend points for in-layer edges. In-layer edges are assumed to always connect either
     * two western or two eastern ports, but not two ports on different sides. This method makes no
     * restrictions as to the kinds of nodes connected by the in-layer edge. That is, it does not for
     * example assume at least one of the connected nodes to be a regular node.
     * 
     * @param edge the in-layer edge to route.
     * @param layerXPos the layer's x position.
     * @param layerWidth the layer's width.
     * @param edgeSpacing the spacing to respect for the in-layer edge bend points.
     */
    private void routeInLayerEdge(final LEdge edge, final double layerXPos, final double layerWidth,
            final double edgeSpacing) {
        
        /* We will add two bend points to the edge:
         *  1. One will be vertically centered between the connected ports, with the x position
         *     slightly to the left (western ports) or to the right (eastern ports) of the layer.
         *  2. The other will be just where the port of the dummy node is anchored. (all in-layer
         *     edges are assumed to connect to at least one dummy node.)
         */
        
        LPort sourcePort = edge.getSource();
        LPort targetPort = edge.getTarget();
        
        // Calculate the two x coordinates used (one at the layer start / end, and one a bit off)
        double nearX = 0.0;
        double farX = 0.0;
        
        // Since in-layer edges connect two eastern or two western ports, we only need to look at the
        // port side of the source port
        if (sourcePort.getSide() == PortSide.EAST) {
            nearX = layerXPos + layerWidth;
            farX = nearX + edgeSpacing;
        } else if (sourcePort.getSide() == PortSide.WEST) {
            nearX = layerXPos;
            farX = nearX - edgeSpacing;
        }
        
        // FIRST BEND POINT (if the source node is a dummy node)
        if (sourcePort.getNode().getProperty(Properties.NODE_TYPE) != NodeType.NORMAL) {
            edge.getBendPoints().add(new KVector(nearX, sourcePort.getAbsoluteAnchor().y));
        }
        
        // SECOND BEND POINT (halfway between the ports)
        edge.getBendPoints().add(new KVector(
                farX,
                (sourcePort.getAbsoluteAnchor().y + targetPort.getAbsoluteAnchor().y) / 2.0));
        
        // THIRD BEND POINT (if the target node is a dummy node)
        if (targetPort.getNode().getProperty(Properties.NODE_TYPE) != NodeType.NORMAL) {
            edge.getBendPoints().add(new KVector(nearX, targetPort.getAbsoluteAnchor().y));
        }
    }
}
