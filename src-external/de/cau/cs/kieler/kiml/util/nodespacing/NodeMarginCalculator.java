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
package de.cau.cs.kieler.kiml.util.nodespacing;

import java.awt.geom.Rectangle2D;

import de.cau.cs.kieler.kiml.options.EdgeLabelPlacement;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortLabelPlacement;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.EdgeAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.GraphAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.LabelAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.NodeAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.PortAdapter;
import de.cau.cs.kieler.kiml.util.nodespacing.Spacing.Margins;

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
 * @author uru
 * @kieler.design 2012-08-10 chsch grh
 */
public final class NodeMarginCalculator  {

    /**
     * {@inheritDoc}
     */
    public void processNodeMargin(final GraphAdapter<?> layeredGraph) {
        
        double spacing = layeredGraph.getProperty(LayoutOptions.SPACING);

        // Iterate through all nodes
        for (NodeAdapter<?> node : layeredGraph.getNodes()) {
            processNode(node, spacing);
        }
    }

    /**
     * Calculates the margin of the given node.
     * 
     * @param node the node whose margin to calculate.
     * @param spacing object spacing set on the layered graph.
     */
    private void processNode(final NodeAdapter<?> node, final double spacing) {
        // This will be our bounding box. We'll start with one that's the same size
        // as our node, and at the same position.
        Rectangle2D.Double boundingBox = new Rectangle2D.Double(
                node.getPosition().x,
                node.getPosition().y,
                node.getSize().x,
                node.getSize().y);
        
        // We'll reuse this rectangle as our box for elements to add to the bounding box
        Rectangle2D.Double elementBox = new Rectangle2D.Double();
        
        // Put the node's labels into the bounding box
        for (LabelAdapter<?> label : node.getLabels()) {
            elementBox.x = label.getPosition().x + node.getPosition().x;
            elementBox.y = label.getPosition().y + node.getPosition().y;
            elementBox.width = label.getSize().x;
            elementBox.height = label.getSize().y;
            
            Rectangle2D.union(boundingBox, elementBox, boundingBox);
        }
        
        // Do the same for ports and their labels
        for (PortAdapter<?> port : node.getPorts()) {
            // Calculate the port's upper left corner's x and y coordinate
            double portX = port.getPosition().x + node.getPosition().x;
            double portY = port.getPosition().y + node.getPosition().y;
            
            // The port itself
            elementBox.x = portX;
            elementBox.y = portY;
            elementBox.width = port.getSize().x;
            elementBox.height = port.getSize().y;
            
            Rectangle2D.union(boundingBox, elementBox, boundingBox);
            
            // The port's labels
            for (LabelAdapter<?> label : port.getLabels()) {
                elementBox.x = label.getPosition().x + portX;
                elementBox.y = label.getPosition().y + portY;
                elementBox.width = label.getSize().x;
                elementBox.height = label.getSize().y;
                
                Rectangle2D.union(boundingBox, elementBox, boundingBox);
            }
        }
        
        // Do the same for end labels and port labels on edges connected to the node
        for (PortAdapter<?> port : node.getPorts()) {
            // Calculate the port's upper left corner's x and y coordinate
            double portX = port.getPosition().x + node.getPosition().x;
            double portY = port.getPosition().y + node.getPosition().y;
            double maxPortLabelWidth = 0;
            double maxPortLabelHeight = 0;
            
            // TODO: maybe leave space for manually placed ports 
            if (node.getProperty(LayoutOptions.PORT_LABEL_PLACEMENT) == PortLabelPlacement.OUTSIDE) {
                for (LabelAdapter<?> label : port.getLabels()) {
                    if (maxPortLabelWidth < label.getSize().x) {
                        maxPortLabelWidth = label.getSize().x;
                    }
                    
                    if (maxPortLabelHeight < label.getSize().y) {
                        maxPortLabelHeight = label.getSize().y;
                    }
                }
            }

            // For each edge, the tail labels of outgoing edges ...
            for (EdgeAdapter<?> edge : port.getOutgoingEdges()) {
                for (LabelAdapter<?> label : edge.getLabels()) {
                    if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                            == EdgeLabelPlacement.TAIL) {
                        
                        elementBox.x = portX;
                        elementBox.y = portY;
                        elementBox.width = label.getSize().x + maxPortLabelWidth;
                        elementBox.height = label.getSize().y + maxPortLabelHeight;
                        
                        Rectangle2D.union(boundingBox, elementBox, boundingBox);
                    }
                }
            }

            // ... and the head label of incoming edges shall be considered 
            for (EdgeAdapter<?> edge : port.getIncomingEdges()) {
                for (LabelAdapter<?> label : edge.getLabels()) {
                    if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                            == EdgeLabelPlacement.HEAD) {
                        
                        elementBox.x = portX - maxPortLabelWidth - label.getSize().x;
                        elementBox.y = portY;
                        elementBox.width = label.getSize().x;
                        elementBox.height = label.getSize().y;
                        
                        Rectangle2D.union(boundingBox, elementBox, boundingBox);
                    }
                }
            }
        }
        
        // Reset the margin
        Margins margin = new Margins(node.getMargin());
        margin.top = node.getPosition().y - boundingBox.y;
        margin.bottom = boundingBox.getMaxY() - (node.getPosition().y + node.getSize().y);
        margin.left = node.getPosition().x - boundingBox.x;
        margin.right = boundingBox.getMaxX() - (node.getPosition().x + node.getSize().x);
        node.setMargin(margin);
    }
}
