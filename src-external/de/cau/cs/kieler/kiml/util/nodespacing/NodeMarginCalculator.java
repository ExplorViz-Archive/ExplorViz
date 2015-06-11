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

import de.cau.cs.kieler.core.math.KVector;
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

    private boolean includeLabels = true;
    private boolean includePorts = true;
    private boolean includePortLabels = true;
    private boolean includeEdgeHeadTailLabels = true;

    private GraphAdapter<?> adapter;

    /**
     * Creates a new calculator for the passed adapter.
     * 
     * @param adapter
     *            for the underlying graph type.
     */
    protected NodeMarginCalculator(final GraphAdapter<?> adapter) {
        this.adapter = adapter;
    }

    // ------------
    //  configure
    // ------------

    /**
     * Do not consider labels during margin calculation.
     * 
     * @return this
     */
    public NodeMarginCalculator excludeLabels() {
        this.includeLabels = false;
        return this;
    }
    
    /**
     * Do not consider ports during margin calculation.
     * 
     * @return this
     */
    public NodeMarginCalculator excludePorts() {
        this.includePorts = false;
        return this;
    }
    
    /**
     * Do not consider port labels during margin calculation.
     * 
     * @return this
     */
    public NodeMarginCalculator excludePortLabels() {
        this.includePortLabels = false;
        return this;
    }
    
    /**
     * Do not consider an edge's head and tail labels during margin calculation.
     * 
     * @return this
     */
    public NodeMarginCalculator excludeEdgeHeadTailLabels() {
        this.includeEdgeHeadTailLabels = false;
        return this;
    }
    
    // ------------
    //   process
    // ------------
    
    /**
     * Calculates and assigns margins to all nodes.
     */
    public void process() {
        double spacing = adapter.getProperty(LayoutOptions.LABEL_SPACING);

        // Iterate through all nodes
        for (NodeAdapter<?> node : adapter.getNodes()) {
            processNode(node, spacing);
        }
    }

    /**
     * Calculates the margin of the given node.
     * 
     * @param node the node whose margin to calculate.
     * @param labelSpacing label spacing set on the layered graph.
     */
    private void processNode(final NodeAdapter<?> node, final double labelSpacing) {
        // This will be our bounding box. We'll start with one that's the same size
        // as our node, and at the same position.
        Rectangle boundingBox = new Rectangle(
                node.getPosition().x,
                node.getPosition().y,
                node.getSize().x,
                node.getSize().y);
        
        // We'll reuse this rectangle as our box for elements to add to the bounding box
        Rectangle elementBox = new Rectangle();
        
        // Put the node's labels into the bounding box
        if (includeLabels) {
            for (LabelAdapter<?> label : node.getLabels()) {
                elementBox.x = label.getPosition().x + node.getPosition().x;
                elementBox.y = label.getPosition().y + node.getPosition().y;
                elementBox.width = label.getSize().x;
                elementBox.height = label.getSize().y;
                
                boundingBox.union(elementBox);
            }
        }
        
        // Do the same for ports and their labels
        for (PortAdapter<?> port : node.getPorts()) {
            // Calculate the port's upper left corner's x and y coordinate
            double portX = port.getPosition().x + node.getPosition().x;
            double portY = port.getPosition().y + node.getPosition().y;
            
            // The port itself
            if (includePorts) {
                elementBox.x = portX;
                elementBox.y = portY;
                elementBox.width = port.getSize().x;
                elementBox.height = port.getSize().y;
                
                boundingBox.union(elementBox);
            }
            
            // The port's labels
            if (includePortLabels) {
                for (LabelAdapter<?> label : port.getLabels()) {
                    elementBox.x = label.getPosition().x + portX;
                    elementBox.y = label.getPosition().y + portY;
                    elementBox.width = label.getSize().x;
                    elementBox.height = label.getSize().y;
                    
                    boundingBox.union(elementBox);
                }
            }
            
            // End labels of edges connected to the port
            if (includeEdgeHeadTailLabels) {
                KVector requiredPortLabelSpace = new KVector(-labelSpacing, -labelSpacing);
                
                // TODO: maybe leave space for manually placed ports 
                if (node.getProperty(LayoutOptions.PORT_LABEL_PLACEMENT) == PortLabelPlacement.OUTSIDE) {
                    for (LabelAdapter<?> label : port.getLabels()) {
                        requiredPortLabelSpace.x += label.getSize().x + labelSpacing;
                        requiredPortLabelSpace.y += label.getSize().y + labelSpacing;
                    }
                }
                
                requiredPortLabelSpace.x = Math.max(requiredPortLabelSpace.x, 0.0);
                requiredPortLabelSpace.y = Math.max(requiredPortLabelSpace.y, 0.0);
                
                processEdgeHeadTailLabels(boundingBox, port.getOutgoingEdges(), port.getIncomingEdges(),
                        node, port, requiredPortLabelSpace, labelSpacing);
            }
        }
        
        // Process end labels of edges directly connected to the node
        if (includeEdgeHeadTailLabels) {
            processEdgeHeadTailLabels(boundingBox, node.getOutgoingEdges(), node.getIncomingEdges(),
                    null, null, null, labelSpacing);
        }
        
        // Reset the margin
        Margins margin = new Margins(node.getMargin());
        margin.top = node.getPosition().y - boundingBox.y;
        margin.bottom = boundingBox.y + boundingBox.height - (node.getPosition().y + node.getSize().y);
        margin.left = node.getPosition().x - boundingBox.x;
        margin.right = boundingBox.x + boundingBox.width - (node.getPosition().x + node.getSize().x);
        node.setMargin(margin);
    }
    
    /**
     * Adds the bounding boxes of the head or tail labels of the given sets of outgoing and incoming
     * edges to the given bounding box.
     * 
     * @param boundingBox the bounding box that should be enlarged to include head and tail labels.
     * @param outgoingEdges set of outgoing edges whose tail labels should be included.
     * @param incomingEdges set of incoming edges whose head labels should be included.
     * @param node the node we're processing labels for.
     * @param port the port if the edges are connected to one.
     * @param portLabelSpace if the edges are connected to a port, this is the space required to
     *                               place the port's labels.
     * @param labelSpacing label spacing.
     */
    private void processEdgeHeadTailLabels(final Rectangle boundingBox,
            final Iterable<EdgeAdapter<?>> outgoingEdges, final Iterable<EdgeAdapter<?>> incomingEdges,
            final NodeAdapter<?> node, final PortAdapter<?> port, final KVector portLabelSpace,
            final double labelSpacing) {
        
        Rectangle labelBox = new Rectangle();
        
        // For each edge, the tail labels of outgoing edges ...
        for (EdgeAdapter<?> edge : outgoingEdges) {
            for (LabelAdapter<?> label : edge.getLabels()) {
                if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT) == EdgeLabelPlacement.TAIL) {
                    computeLabelBox(labelBox, label, false, node, port, portLabelSpace, labelSpacing);
                    boundingBox.union(labelBox);
                }
            }
        }
   
        // ... and the head label of incoming edges shall be considered
        for (EdgeAdapter<?> edge : incomingEdges) {
            for (LabelAdapter<?> label : edge.getLabels()) {
                if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT) == EdgeLabelPlacement.HEAD) {
                    computeLabelBox(labelBox, label, true, node, port, portLabelSpace, labelSpacing);
                    boundingBox.union(labelBox);
                }
            }
        }
    }
    
    /**
     * Computes the given edge label's bounding box. The position of the box is just a rough estimate
     * and might not match what the label positioning algorithm used ends up computing.
     * 
     * @param labelBox result of the computation.
     * @param label the label whose bounding box to compute.
     * @param incomingEdge {@code true} if the edge the label belongs to is an incoming edge to the node.
     * @param node the node the label's edge is incident to.
     * @param port the port the edge might be connected to. {@code null} if the edge is connected
     *             directly to the node.
     * @param portLabelSpace if the edges are connected to a port, this is the space required to
     *                       place the port's labels.
     * @param labelSpacing label spacing.
     */
    private void computeLabelBox(final Rectangle labelBox, final LabelAdapter<?> label,
            final boolean incomingEdge, final NodeAdapter<?> node, final PortAdapter<?> port,
            final KVector portLabelSpace, final double labelSpacing) {
        
        // The label box is set to the label's size; the position needs to be determined depending on
        // further details.
        labelBox.width = label.getSize().x;
        labelBox.height = label.getSize().y;
        
        if (port == null) {
            // The edge is connected directly to the node
            if (incomingEdge) {
                // Assume the edge enters the node at its western side
                labelBox.x = 0.0 - labelSpacing - label.getSize().x;
                labelBox.y = 0;
            } else {
                // Assume the edge leaves the node at its eastern side
                labelBox.x = node.getSize().x + labelSpacing;
                labelBox.y = 0;
            }
        } else {
            switch (port.getSide()) {
            case UNDEFINED:
            case EAST:
                labelBox.x = port.getPosition().x
                           + port.getSize().x
                           + labelSpacing
                           + portLabelSpace.x
                           + labelSpacing;
                labelBox.y = port.getPosition().y;
                break;
                
            case WEST:
                labelBox.x = port.getPosition().x
                           - labelSpacing
                           - portLabelSpace.x
                           - labelSpacing
                           - label.getSize().x;
                labelBox.y = port.getPosition().y;
                break;
                
            case NORTH:
                labelBox.x = port.getPosition().x
                           + port.getSize().x
                           + labelSpacing;
                labelBox.y = port.getPosition().y
                           - labelSpacing
                           - portLabelSpace.y
                           - labelSpacing
                           - label.getSize().y;
                break;
                
            case SOUTH:
                labelBox.x = port.getPosition().x
                           + port.getSize().x
                           + labelSpacing;
                labelBox.y = port.getPosition().y
                           + port.getSize().y
                           + labelSpacing
                           + portLabelSpace.y
                           + labelSpacing;
                break;
            }
        }
    }
}
