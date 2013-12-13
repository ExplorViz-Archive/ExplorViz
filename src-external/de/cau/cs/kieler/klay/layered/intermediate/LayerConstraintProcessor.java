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

import java.util.List;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.UnsupportedConfigurationException;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.LayerConstraint;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Moves nodes with layer constraints to the appropriate layers. To meet the preconditions of
 * this processor, the {@link EdgeAndLayerConstraintEdgeReverser} can be used.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>a layered graph; nodes to be placed in the first layer have only
 *     outgoing edges; nodes to be placed in the last layer have only incoming edges.</dd>
 *   <dt>Postcondition:</dt><dd>nodes with layer constraints have been placed in the
 *     appropriate layers.</dd>
 *   <dt>Slots:</dt><dd>Before phase 3.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>{@link HierarchicalPortConstraintProcessor}</dd>
 * </dl>
 * 
 * @see EdgeAndLayerConstraintEdgeReverser
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class LayerConstraintProcessor implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Layer constraint application", 1);
        
        List<Layer> layers = layeredGraph.getLayers();
        
        // Retrieve the current first and last layers
        Layer firstLayer = layers.get(0);
        Layer lastLayer = layers.get(layers.size() - 1);
        
        // Create the new first and last layers, in case they will be needed
        Layer veryFirstLayer = new Layer(layeredGraph);
        Layer veryLastLayer = new Layer(layeredGraph);
        
        // Iterate through the current list of layers
        for (Layer layer : layers) {
            // Iterate through a node array to avoid ConcurrentModificationExceptions
            LNode [] nodes = layer.getNodes().toArray(new LNode[layer.getNodes().size()]);
            
            for (LNode node : nodes) {
                LayerConstraint constraint = node.getProperty(Properties.LAYER_CONSTRAINT);
                
                // Check if there is a layer constraint
                switch (constraint) {
                case FIRST:
                    node.setLayer(firstLayer);
                    assertNoIncoming(node);
                    break;
                
                case FIRST_SEPARATE:
                    node.setLayer(veryFirstLayer);
                    assertNoIncoming(node);
                    break;
                
                case LAST:
                    node.setLayer(lastLayer);
                    assertNoOutgoing(node);
                    break;
                
                case LAST_SEPARATE:
                    node.setLayer(veryLastLayer);
                    assertNoOutgoing(node);
                    break;
                }
            }
        }
        
        // Remove empty first and last layers
        if (firstLayer.getNodes().isEmpty()) {
            layers.remove(0);
        }
        
        if (firstLayer != lastLayer && lastLayer.getNodes().isEmpty()) {
            layers.remove(layers.size() - 1);
        }
        
        // Add non-empty new first and last layers
        if (!veryFirstLayer.getNodes().isEmpty()) {
            layers.add(0, veryFirstLayer);
        }

        if (!veryLastLayer.getNodes().isEmpty()) {
            layers.add(veryLastLayer);
        }
        
        monitor.done();
    }
    
    /**
     * Check that the node has no incoming edges, and fail if it has any.
     * 
     * @param node a node
     */
    private void assertNoIncoming(final LNode node) {
        for (LPort port : node.getPorts()) {
            if (!port.getIncomingEdges().isEmpty()) {
                throw new UnsupportedConfigurationException("Node '" + node.getDesignation()
                        + "' has its layer constraint set to FIRST or FIRST_SEPARATE, but has at least "
                        + "one incoming edge. Connections between nodes with these layer constraints "
                        + "are not supported.");
            }
        }
    }
    
    /**
     * Check that the node has no outgoing edges, and fail if it has any.
     * 
     * @param node a node
     */
    private void assertNoOutgoing(final LNode node) {
        for (LPort port : node.getPorts()) {
            if (!port.getOutgoingEdges().isEmpty()) {
                throw new UnsupportedConfigurationException("Node '" + node.getDesignation()
                        + "' has its layer constraint set to LAST or LAST_SEPARATE, but has at least "
                        + "one outgoing edge. Connections between nodes with these layer constraints "
                        + "are not supported.");
            }
        }
    }

}
