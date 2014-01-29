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

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.EdgeConstraint;
import de.cau.cs.kieler.klay.layered.properties.LayerConstraint;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Makes sure nodes with edge or layer constraints have only incoming or only outgoing edges,
 * as appropriate. This is done even before cycle breaking because the result may
 * already break some cycles. This processor is required for
 * {@link LayerConstraintProcessor} to work correctly. If edge constraints are in conflict
 * with layer constraints, the latter take precedence. Furthermore, this processor handles
 * nodes with fixed port sides for which all ports are reversed, i.e. input ports are on the
 * right and output ports are on the left. All incident edges are reversed in such cases.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>an unlayered graph.</dd>
 *   <dt>Postcondition:</dt><dd>nodes with layer constraints have only incoming or
 *     only outgoing edges, as appropriate.</dd>
 *   <dt>Slots:</dt><dd>Before phase 1.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>None.</dd>
 * </dl>
 * 
 * @see LayerConstraintProcessor
 * @author cds
 * @author msp
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class EdgeAndLayerConstraintEdgeReverser implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Edge and layer constraint edge reversal", 1);
        
        // Iterate through the list of nodes
        for (LNode node : layeredGraph.getLayerlessNodes()) {
            // Check if there is a layer constraint
            LayerConstraint layerConstraint = node.getProperty(Properties.LAYER_CONSTRAINT);
            EdgeConstraint edgeConstraint = null;
            
            switch (layerConstraint) {
            case FIRST:
            case FIRST_SEPARATE:
                edgeConstraint = EdgeConstraint.OUTGOING_ONLY;
                break;
            
            case LAST:
            case LAST_SEPARATE:
                edgeConstraint = EdgeConstraint.INCOMING_ONLY;
                break;
            }
            
            if (edgeConstraint != null) {
                // Set the edge constraint on the node
                node.setProperty(Properties.EDGE_CONSTRAINT, EdgeConstraint.OUTGOING_ONLY);
                
                if (edgeConstraint == EdgeConstraint.INCOMING_ONLY) {
                    reverseEdges(layeredGraph, node, layerConstraint, PortType.OUTPUT);
                } else if (edgeConstraint == EdgeConstraint.OUTGOING_ONLY) {
                    reverseEdges(layeredGraph, node, layerConstraint, PortType.INPUT);
                }
            } else {
                // If the port sides are fixed, but all ports are reversed, that probably means that we
                // have a feedback node. Normally, the connected edges would be routed around the node,
                // but that hides the feedback node character. We thus simply reverse all connected
                // edges and thus make KLay Layered think we have a regular node
                if (node.getProperty(LayoutOptions.PORT_CONSTRAINTS).isSideFixed()
                        && !node.getPorts().isEmpty()) {
                    
                    boolean allPortsReversed = true;
                    for (LPort port : node.getPorts()) {
                        if (!(port.getSide() == PortSide.EAST && port.getNetFlow() > 0
                                || port.getSide() == PortSide.WEST && port.getNetFlow() < 0)) {
                            
                            allPortsReversed = false;
                            break;
                        }
                    }
                    if (allPortsReversed) {
                        reverseEdges(layeredGraph, node, layerConstraint, PortType.UNDEFINED);
                    }
                }
            }
        }
        
        monitor.done();
    }
    
    /**
     * Reverses edges as appropriate.
     * 
     * @param layeredGraph the layered graph.
     * @param node the node to place in the layer.
     * @param nodeLayerConstraint the layer constraint put on the node.
     * @param type type of edges that are reversed.
     */
    private void reverseEdges(final LGraph layeredGraph, final LNode node,
            final LayerConstraint nodeLayerConstraint, final PortType type) {
        
        // Iterate through the node's edges and reverse them, if necessary
        LPort[] ports = node.getPorts().toArray(new LPort[node.getPorts().size()]);
        for (LPort port : ports) {
            // Only incoming edges
            if (type != PortType.INPUT) {
                LEdge[] outgoing = port.getOutgoingEdges().toArray(
                        new LEdge[port.getOutgoingEdges().size()]);
                
                for (LEdge edge : outgoing) {
                    LayerConstraint targetLayerConstraint = edge.getTarget().getNode().getProperty(
                            Properties.LAYER_CONSTRAINT);
                    
                    // We leave an edge untouched if it has already been reversed or if it runs from a
                    // LAST to a LAST_SEPARATE node (such outgoing edges are allowed for LAST nodes)
                    if (!edge.getProperty(Properties.REVERSED)
                            && !(nodeLayerConstraint == LayerConstraint.LAST
                                && targetLayerConstraint == LayerConstraint.LAST_SEPARATE)) {
                        
                        edge.reverse(layeredGraph, true);
                    }
                }
            }
            
            // Only outgoing edges
            if (type != PortType.OUTPUT) {
                LEdge[] incoming = port.getIncomingEdges().toArray(
                        new LEdge[port.getIncomingEdges().size()]);
                
                for (LEdge edge : incoming) {
                    LayerConstraint sourceLayerConstraint = edge.getSource().getNode().getProperty(
                            Properties.LAYER_CONSTRAINT);
                    
                    // We leave an edge untouched if it has already been reversed or if it runs from a
                    // FIRST_SEPARATE to a FIRST node (such incoming edges are allowed for FIRST nodes)
                    if (!edge.getProperty(Properties.REVERSED)
                            && !(nodeLayerConstraint == LayerConstraint.FIRST
                                && sourceLayerConstraint == LayerConstraint.FIRST_SEPARATE)) {
                        
                        edge.reverse(layeredGraph, true);
                    }
                }
            }
        }
    }

}
