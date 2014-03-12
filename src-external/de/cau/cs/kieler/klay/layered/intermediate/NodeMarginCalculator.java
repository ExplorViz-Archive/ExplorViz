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
import de.cau.cs.kieler.kiml.util.nodespacing.KimlNodeDimensionCalculation;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Sets the node margins. Node margins are influenced by both port positions and sizes
 * and label positions and sizes. Furthermore, comment boxes that are put directly
 * above or below a node also increase the margin.
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>A layered graph.</dd>
 *     <dd>Ports have fixed port positions.</dd>
 *     <dd>Labels have fixed positions.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>The node margins are properly set to form a bounding box around the node and its ports and
 *         labels.</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>{@link LabelAndNodeSizeProcessor}</dd>
 * </dl>
 *
 * @see LabelAndNodeSizeProcessor
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 */
public final class NodeMarginCalculator implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Node margin calculation", 1);
        
        // calculate the margins using KIML's utility methods
        KimlNodeDimensionCalculation.calculateNodeMargins(LGraphAdapters.adapt(layeredGraph));
        
        // Iterate through the layers to additionally handle comments
        double spacing = layeredGraph.getProperty(Properties.OBJ_SPACING);
        for (Layer layer : layeredGraph) {
            // Iterate through the layer's nodes
            for (LNode node : layer) {
                processComments(node, spacing);
            }
        }
        
        monitor.done();
    }

    /**
     * Make some extra space for comment boxes that are placed near a node.
     * 
     * @param node a node
     * @param spacing the overall spacing value
     */
    private void processComments(final LNode node, final double spacing) {
        LInsets margin = node.getMargin();

        // Consider comment boxes that are put on top of the node
        List<LNode> topBoxes = node.getProperty(Properties.TOP_COMMENTS);
        double topWidth = 0;
        if (topBoxes != null) {
            double maxHeight = 0;
            for (LNode commentBox : topBoxes) {
                maxHeight = Math.max(maxHeight, commentBox.getSize().y);
                topWidth += commentBox.getSize().x;
            }
            topWidth += spacing / 2 * (topBoxes.size() - 1);
            margin.top += maxHeight + spacing;
        }
        
        // Consider comment boxes that are put in the bottom of the node
        List<LNode> bottomBoxes = node.getProperty(Properties.BOTTOM_COMMENTS);
        double bottomWidth = 0;
        if (bottomBoxes != null) {
            double maxHeight = 0;
            for (LNode commentBox : bottomBoxes) {
                maxHeight = Math.max(maxHeight, commentBox.getSize().y);
                bottomWidth += commentBox.getSize().x;
            }
            bottomWidth += spacing / 2 * (bottomBoxes.size() - 1);
            margin.bottom += maxHeight + spacing;
        }
        
        // Check if the maximum width of the comments is wider than the node itself, which the comments
        // are centered on
        double maxCommentWidth = Math.max(topWidth, bottomWidth);
        if (maxCommentWidth > node.getSize().x) {
            double protrusion = (maxCommentWidth - node.getSize().x) / 2;
            margin.left = Math.max(margin.left, protrusion);
            margin.right = Math.max(margin.right, protrusion);
        }
    }
    
}
