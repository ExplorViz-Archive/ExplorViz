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
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;

/**
 * Calculates the size of each layer, the height of the graph, and the graph's vertical offset. The
 * graph's width cannot be set at this point since that would require the edge routing phase to have
 * already finished.
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>A layered graph with finished node placement.</dd>
 *     <dd>Node margins are calculated.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>Layer sizes are set</dd>
 *     <dd>Graph's height is set</dd>
 *     <dd>Graph's offset includes node positions</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 5.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>{@link HierarchicalPortDummySizeProcessor}</dd>
 *     <dd>{@link HierarchicalPortPositionProcessor}</dd>
 * </dl>
 * 
 * @author cds
 */
public final class LayerSizeAndGraphHeightCalculator implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Layer size calculation", 1);
        
        // Remember the lowest and the highest y coordinates
        double minY = 0.0;
        double maxY = 0.0;

        for (Layer layer : layeredGraph) {
            // Retrieve and reset layer size (just to be sure...)
            KVector layerSize = layer.getSize();
            layerSize.x = 0.0;
            layerSize.y = 0.0;
            
            // If the layer is empty, skip the rest (should not usually happen)
            if (layer.getNodes().isEmpty()) {
                continue;
            }
            
            // Calculate the layer's width
            for (LNode node : layer) {
                KVector nodeSize = node.getSize();
                LInsets nodeMargin = node.getMargin();
                
                layerSize.x = Math.max(
                        layerSize.x,
                        nodeSize.x + nodeMargin.left + nodeMargin.right);
            }
            
            // Calculate the layer's height
            LNode firstNode = layer.getNodes().get(0);
            double top = firstNode.getPosition().y - firstNode.getMargin().top;
            LNode lastNode = layer.getNodes().get(layer.getNodes().size() - 1);
            double bottom = lastNode.getPosition().y + lastNode.getSize().y
                    + lastNode.getMargin().bottom;
            layerSize.y = bottom - top;
            
            // Update the lowest and highest encountered Y coordinate
            minY = Math.min(minY, top);
            maxY = Math.max(maxY, bottom);
        }
        
        // Set the graph's height
        layeredGraph.getSize().y = maxY - minY;
        layeredGraph.getOffset().y -= minY;
        
        monitor.done();
    }

}
