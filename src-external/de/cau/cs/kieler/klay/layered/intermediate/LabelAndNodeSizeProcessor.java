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

import java.awt.geom.Rectangle2D;
import java.util.EnumSet;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.NodeLabelPlacement;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortLabelPlacement;
import de.cau.cs.kieler.kiml.options.SizeConstraint;
import de.cau.cs.kieler.kiml.options.SizeOptions;
import de.cau.cs.kieler.kiml.util.KimlUtil;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LLabel.LabelSide;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Calculates node sizes, places ports, and places node and port labels.
 * 
 * <p><i>Note:</i> Regarding port placement, this processor now does what the old
 * {@code PortPositionProcessor} did and thus replaces it.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>The graph is layered.</dd>
 *     <dd>Crossing minimization is finished.</dd>
 *     <dd>Port constraints are at least at {@code FIXED_ORDER}.</dd>
 *     <dd>Port lists are properly sorted going clockwise, starting at the leftmost northern port.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>Port positions are fixed.</dd>
 *     <dd>Port labels are placed.</dd>
 *     <dd>Node labels are placed.</dd>
 *     <dd>Node sizes are set.</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>{@link LabelSideSelector}</dd>
 * </dl>
 * 
 * @see LabelSideSelector
 * @author cds
 */
public final class LabelAndNodeSizeProcessor implements ILayoutProcessor {
    
    /* The following variables provide context information for the different phases of the algorithm.
     * The information are usually computed by some phase to be made available to later phases. This
     * would probably be better kept in some kind of a context object, but is kept here for performance
     * reasons to avoid having to create new context objects all the time.
     */
    
    /**
     * Node insets required by port labels inside the node. This is always set, but not always taken
     * into account to calculate the node size.
     * 
     * <p><i>Note:</i> This is only valid for the currently processed node!</p>
     */
    private LInsets requiredPortLabelSpace = new LInsets();
    
    /**
     * Node insets required by node labels placed inside the node. This is always set, but not always
     * taken into account to calculate the node size.
     * 
     * <p><i>Note:</i> This is only valid for the currently processed node!</p>
     */
    private LInsets requiredNodeLabelSpace = new LInsets();
    
    /**
     * Space required by the node labels if stacked vertically.
     * 
     * <p><i>Note:</i> This is only valid for the currently processed node!</p>
     */
    private KVector nodeLabelsBoundingBox = new KVector();
    
    /**
     * Number of ports on the western side. Only used if port constraints are not
     * {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}.
     */
    private int westPortsCount = 0;
    
    /**
     * Height of the ports on the western side. If port labels are accounted for, the height includes
     * the relevant port margins too. Only used if port constraints are not
     * {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}.
     */
    private double westPortsHeight = 0.0;
    
    /**
     * Number of ports on the eastern side.Only used if port constraints are not
     * {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}.
     */
    private int eastPortsCount = 0;
    
    /**
     * Height of the ports on the eastern side. If port labels are accounted for, the height includes
     * the relevant port margins too. Only used if port constraints are not
     * {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}.
     */
    private double eastPortsHeight = 0.0;
    
    /**
     * Number of ports on the northern side.Only used if port constraints are not
     * {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}.
     */
    private int northPortsCount = 0;
    
    /**
     * Width of the ports on the northern side. If port labels are accounted for, the height includes
     * the relevant port margins too. Only used if port constraints are not
     * {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}.
     */
    private double northPortsWidth = 0.0;
    
    /**
     * Number of ports on the southern side.Only used if port constraints are not
     * {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}.
     */
    private int southPortsCount = 0;
    
    /**
     * Width of the ports on the southern side. If port labels are accounted for, the height includes
     * the relevant port margins too. Only used if port constraints are not
     * {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}.
     */
    private double southPortsWidth = 0.0;
    

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Node and Port Label Placement and Node Sizing", 1);
        
        double objectSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING);
        double labelSpacing = layeredGraph.getProperty(LayoutOptions.LABEL_SPACING);

        // Iterate over all the graph's nodes
        for (Layer layer : layeredGraph) {
            for (LNode node : layer) {
                /* Note that, upon Miro's request, each phase of the algorithm was given a code name. */
                
                /* PREPARATIONS
                 * Reset stuff, fill the port information fields, and remember the node's old size.
                 */
                resetContext();
                calculatePortInformation(node, node.getProperty(LayoutOptions.SIZE_CONSTRAINT).contains(
                        SizeConstraint.PORT_LABELS));
                KVector originalNodeSize = new KVector(node.getSize());
                
                
                /* PHASE 1 (SAD DUCK): PLACE PORT LABELS
                 * Port labels are placed and port margins are calculated. We currently only support
                 * one label per port.
                 */
                PortLabelPlacement labelPlacement = node.getProperty(LayoutOptions.PORT_LABEL_PLACEMENT);
                boolean compoundNodeMode = node.getProperty(Properties.COMPOUND_NODE);
                
                // Place port labels and calculate the margins
                for (LPort port : node.getPorts()) {
                    placePortLabels(port, labelPlacement, compoundNodeMode, labelSpacing);
                    calculateAndSetPortMargins(port);
                }
                
                
                /* PHASE 2 (DYNAMIC DONALD): CALCULATE INSETS
                 * We know the sides the ports will be placed at and we know where node labels are to
                 * be placed. Calculate the node's insets accordingly. Also compute the amount of space
                 * the node labels will need if stacked vertically. Note that we don't have to know the
                 * final port or label positions to calculate all this stuff.
                 */
                calculateRequiredPortLabelSpace(node);
                calculateRequiredNodeLabelSpace(node, labelSpacing);
                
                
                /* PHASE 3 (DANGEROUS DUCKLING): RESIZE NODE
                 * If the node has labels, the node insets might have to be adjusted to reserve space
                 * for them, which is what this phase does.
                 */
                resizeNode(node, objectSpacing, labelSpacing);
                
                
                /* PHASE 4 (DUCK AND COVER): PLACE PORTS
                 * The node is resized, taking all node size constraints into account. The port spacing
                 * is not required for port placement since the placement will be based on the node's
                 * size (if it is not fixed anyway).
                 */
                placePorts(node, originalNodeSize);
                
                
                /* PHASE 5 (HAPPY DUCK): PLACE NODE LABELS
                 * With space reserved for the node labels, the labels are placed.
                 */
                placeNodeLabels(node, labelSpacing);
                
                
                /* CLEANUP (THANKSGIVING): SET NODE INSETS
                 * Set the node insets to include space required for port and node labels. If the labels
                 * were not taken into account when calculating the node's size, this may result in
                 * insets that, taken together, are larger than the node's actual size.
                 */
                LInsets nodeInsets = node.getInsets();
                nodeInsets.left = requiredNodeLabelSpace.left + requiredPortLabelSpace.left;
                nodeInsets.right = requiredNodeLabelSpace.right + requiredPortLabelSpace.right;
                nodeInsets.top = requiredNodeLabelSpace.top + requiredPortLabelSpace.top;
                nodeInsets.bottom = requiredNodeLabelSpace.bottom + requiredPortLabelSpace.bottom;
            }
        }
        
        monitor.done();
    }
    
    /**
     * Resets the fields providing context information to the algorithm.
     */
    private void resetContext() {
        requiredPortLabelSpace.set(0.0, 0.0, 0.0, 0.0);
        requiredNodeLabelSpace.set(0.0, 0.0, 0.0, 0.0);
        
        westPortsCount = 0;
        westPortsHeight = 0.0;
        eastPortsCount = 0;
        eastPortsHeight = 0.0;
        northPortsCount = 0;
        northPortsWidth = 0.0;
        southPortsCount = 0;
        southPortsWidth = 0.0;
    }

    /**
     * Calculates the width of ports on the northern and southern sides, and the height of ports on the
     * western and eastern sides of the given node. The information are stored in the class fields and
     * are used later on when calculating the minimum node size and when placing ports.
     * 
     * @param node the node to calculate the port information for.
     * @param accountForLabels if {@code true}, the port labels will be taken into account
     *                         when calculating the port information.
     */
    private void calculatePortInformation(final LNode node, final boolean accountForLabels) {
        // Iterate over the ports
        for (LPort port : node.getPorts()) {
            switch (port.getSide()) {
            case WEST:
                westPortsCount++;
                westPortsHeight += port.getSize().y
                    + (accountForLabels ? port.getMargin().bottom + port.getMargin().top : 0.0);
                break;
            case EAST:
                eastPortsCount++;
                eastPortsHeight += port.getSize().y
                    + (accountForLabels ? port.getMargin().bottom + port.getMargin().top : 0.0);
                break;
            case NORTH:
                northPortsCount++;
                northPortsWidth += port.getSize().x
                    + (accountForLabels ? port.getMargin().left + port.getMargin().right : 0.0);
                break;
            case SOUTH:
                southPortsCount++;
                southPortsWidth += port.getSize().x
                    + (accountForLabels ? port.getMargin().left + port.getMargin().right : 0.0);
                break;
            }
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    // PORT LABEL PLACEMENT

    /**
     * Places the label of the given port, if any. We assume that the label is actually part of the
     * given port.
     * 
     * <p><i>Note:</i> We currently only support one label per port.</p>
     * 
     * @param port the port whose labels to place.
     * @param placement the port label placement that applies to the port.
     * @param compoundNodeMode {@code true} if the node contains further nodes in the original graph.
     *                         This influences the inner port label placement.
     * @param labelSpacing spacing between labels and other objects.
     */
    private void placePortLabels(final LPort port, final PortLabelPlacement placement,
            final boolean compoundNodeMode, final double labelSpacing) {
        
        // Get the port's label, if any
        if (!port.getLabels().isEmpty()) {
            // We use different implementations based on whether port labels are to be placed
            // inside or outside the node
            if (placement.equals(PortLabelPlacement.INSIDE)) {
                placePortLabelsInside(port, port.getLabels().get(0), compoundNodeMode, labelSpacing);
            } else if (placement.equals(PortLabelPlacement.OUTSIDE)) {
                placePortLabelsOutside(port, port.getLabels().get(0), labelSpacing);
            }
        }
    }

    /**
     * Places the label of the given port on the inside of the port's node.
     * 
     * @param port the port whose label to place.
     * @param label the label to place.
     * @param compoundNodeMode {@code true} if the node contains further nodes in the original graph. In
     *                         this case, port labels are not placed next to ports, but a little down as
     *                         well to avoid edge-label-crossings.
     * @param labelSpacing spacing between labels and other objects.
     */
    private void placePortLabelsInside(final LPort port, final LLabel label,
            final boolean compoundNodeMode, final double labelSpacing) {
        
        switch (port.getSide()) {
        case WEST:
            label.getPosition().x = port.getSize().x + labelSpacing;
            label.getPosition().y = compoundNodeMode
                    ? port.getSize().y + labelSpacing
                    : (port.getSize().y - label.getSize().y) / 2.0;
            break;
        case EAST:
            label.getPosition().x = -label.getSize().x - labelSpacing;
            label.getPosition().y = compoundNodeMode
                    ? port.getSize().y + labelSpacing
                    : (port.getSize().y - label.getSize().y) / 2.0;
            break;
        case NORTH:
            label.getPosition().x = -label.getSize().x / 2;
            label.getPosition().y = port.getSize().y + labelSpacing;
            break;
        case SOUTH:
            label.getPosition().x = -label.getSize().x / 2;
            label.getPosition().y = -label.getSize().y - labelSpacing;
            break;
        }
    }
    
    /**
     * Places the label of the given port on the outside of the port's node.
     * 
     * @param port the port whose label to place.
     * @param label the label to place.
     * @param labelSpacing spacing between labels and other objects.
     */
    private void placePortLabelsOutside(final LPort port, final LLabel label,
            final double labelSpacing) {
        
        if (label.getSide() == LabelSide.ABOVE) {
            // Place label "above" edges
            switch (port.getSide()) {
            case WEST:
                label.getPosition().x = -label.getSize().x - labelSpacing;
                label.getPosition().y = -label.getSize().y - labelSpacing;
                break;
            case EAST:
                label.getPosition().x = port.getSize().x + labelSpacing;
                label.getPosition().y = -label.getSize().y - labelSpacing;
                break;
            case NORTH:
                label.getPosition().x = -label.getSize().x - labelSpacing;
                label.getPosition().y = -label.getSize().y - labelSpacing;
                break;
            case SOUTH:
                label.getPosition().x = -label.getSize().x - labelSpacing;
                label.getPosition().y = port.getSize().y + labelSpacing;
                break;
            }
        } else {
            // Place label "below" edges
            switch (port.getSide()) {
            case WEST:
                label.getPosition().x = -label.getSize().x - labelSpacing;
                label.getPosition().y = port.getSize().y + labelSpacing;
                break;
            case EAST:
                label.getPosition().x = port.getSize().x + labelSpacing;
                label.getPosition().y = port.getSize().y + labelSpacing;
                break;
            case NORTH:
                label.getPosition().x = port.getSize().x + labelSpacing;
                label.getPosition().y = -label.getSize().y - labelSpacing;
                break;
            case SOUTH:
                label.getPosition().x = port.getSize().x + labelSpacing;
                label.getPosition().y = port.getSize().y + labelSpacing;
                break;
            }
        }
    }

    /**
     * Calculates the port's margins such that its label is part of them and sets them accordingly.
     * 
     * <p><i>Note:</i> We currently only support one label per port.</p>
     * 
     * @param port the port whose margins to calculate.
     */
    private void calculateAndSetPortMargins(final LPort port) {
        // Get the port's label, if any
        if (!port.getLabels().isEmpty()) {
            Rectangle2D.Double portBox = new Rectangle2D.Double(
                    0.0,
                    0.0,
                    port.getSize().x,
                    port.getSize().y);
            
            // We only support one label, so retrieve it
            LLabel label = port.getLabels().get(0);
            Rectangle2D.Double labelBox = new Rectangle2D.Double(
                    label.getPosition().x,
                    label.getPosition().y,
                    label.getSize().x,
                    label.getSize().y);
            
            // Calculate the union of the two bounding boxes and calculate the margins
            Rectangle2D.union(portBox, labelBox, portBox);

            LInsets margin = port.getMargin();
            margin.top = -portBox.y;
            margin.bottom = portBox.getMaxY() - port.getSize().y;
            margin.left = -portBox.x;
            margin.right = portBox.getMaxX() - port.getSize().x;
        }
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    // INSETS CALCULATION
    
    /**
     * Calculates the space required to accommodate all port labels and sets
     * {@link #requiredPortLabelSpace}. Also counts the number of ports on
     * each side of the node.
     * 
     * <p><i>Note:</i> We currently only support one label per port.</p>
     * 
     * @param node the node whose insets to calculate and to set.
     */
    private void calculateRequiredPortLabelSpace(final LNode node) {
        // Iterate over the ports and look at their margins
        for (LPort port : node.getPorts()) {
            switch (port.getSide()) {
            case WEST:
                requiredPortLabelSpace.left =
                    Math.max(requiredPortLabelSpace.left, port.getMargin().right);
                break;
            case EAST:
                requiredPortLabelSpace.right =
                    Math.max(requiredPortLabelSpace.right, port.getMargin().left);
                break;
            case NORTH:
                requiredPortLabelSpace.top =
                    Math.max(requiredPortLabelSpace.top, port.getMargin().bottom);
                break;
            case SOUTH:
                requiredPortLabelSpace.bottom =
                    Math.max(requiredPortLabelSpace.bottom, port.getMargin().top);
                break;
            }
        }
    }

    /**
     * Calculates the space required to accommodate the node labels (if any) and sets
     * {@link #requiredNodeLabelSpace} as well as {@link #nodeLabelsBoundingBox}. If the labels are
     * placed at the top or at the bottom, the top or bottom insets are set. If it is centered
     * vertically, the left or right insets are set if the labels are horizontally aligned leftwards
     * or rightwards. If they are centered in both directions, no insets are set. If they are placed
     * outside the node, no insets are set.
     * 
     * @param node
     *            the node in question.
     * @param labelSpacing
     *            spacing between labels and other objects.
     */
    private void calculateRequiredNodeLabelSpace(final LNode node, final double labelSpacing) {
        // Check if there are any labels
        if (node.getLabels().isEmpty()) {
            return;
        }
        
        // Compute a bounding box for all labels stacked vertically
        nodeLabelsBoundingBox.x = 0;
        nodeLabelsBoundingBox.y = 0;
        
        for (LLabel label : node.getLabels()) {
            nodeLabelsBoundingBox.x = Math.max(nodeLabelsBoundingBox.x, label.getSize().x);
            nodeLabelsBoundingBox.y += label.getSize().y + labelSpacing;
        }
        nodeLabelsBoundingBox.y -= labelSpacing;
        
        // Retrieve label placement policy
        EnumSet<NodeLabelPlacement> nodeLabelPlacement =
                node.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT);
        
        // This method only sets insets if the node label is to be placed on the inside
        if (nodeLabelPlacement.contains(NodeLabelPlacement.INSIDE)) {
            // The primary distinction criterion is the vertical placement
            if (nodeLabelPlacement.contains(NodeLabelPlacement.V_TOP)) {
                requiredNodeLabelSpace.top = nodeLabelsBoundingBox.y + labelSpacing;
            } else if (nodeLabelPlacement.contains(NodeLabelPlacement.V_BOTTOM)) {
                requiredNodeLabelSpace.bottom = nodeLabelsBoundingBox.y + labelSpacing;
            } else if (nodeLabelPlacement.contains(NodeLabelPlacement.V_CENTER)) {
                // Check whether the label will be placed left or right
                if (nodeLabelPlacement.contains(NodeLabelPlacement.H_LEFT)) {
                    requiredNodeLabelSpace.left = nodeLabelsBoundingBox.x + labelSpacing;
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.H_RIGHT)) {
                    requiredNodeLabelSpace.right = nodeLabelsBoundingBox.x + labelSpacing;
                }
            }
        }
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    // NODE RESIZING
    
    /**
     * Resizes the given node subject to the sizing constraints and options.
     * 
     * @param node the node to resize.
     * @param portSpacing the amount of space to leave between ports.
     * @param labelSpacing the amount of space to leave between labels and other objects.
     */
    private void resizeNode(final LNode node, final double portSpacing, final double labelSpacing) {
        KVector nodeSize = node.getSize();
        KVector originalNodeSize = new KVector(nodeSize);
        EnumSet<SizeConstraint> sizeConstraint = node.getProperty(LayoutOptions.SIZE_CONSTRAINT);
        EnumSet<SizeOptions> sizeOptions = node.getProperty(LayoutOptions.SIZE_OPTIONS);
        PortConstraints portConstraints = node.getProperty(LayoutOptions.PORT_CONSTRAINTS);
        boolean accountForLabels = sizeConstraint.contains(SizeConstraint.PORT_LABELS);
        
        // If the size constraint is empty, we can't do anything
        if (sizeConstraint.isEmpty()) {
            return;
        }
        
        // It's not empty, so we will change the node size; we start by resetting the size to zero
        nodeSize.x = 0.0;
        nodeSize.y = 0.0;
        
        // Find out how large the node will have to be to accommodate all ports. If port
        // constraints are set to FIXED_RATIO, we can't do anything smart, really; in this
        // case we will just assume the original node size to be the minumum for ports
        KVector minSizeForPorts = null;
        switch (portConstraints) {
        case FREE:
        case FIXED_SIDE:
        case FIXED_ORDER:
            // Calculate the space necessary to accomodate all ports
            minSizeForPorts = calculatePortSpaceRequirements(node, portSpacing, accountForLabels);
            break;
        
        case FIXED_RATIO:
            // Keep original node size
            minSizeForPorts = new KVector(originalNodeSize);
            break;
        
        case FIXED_POS:
            // Find the maximum position of ports
            minSizeForPorts = calculateMaxPortPositions(node, accountForLabels);
            break;
        }
        
        // If the node size should take port space requirements into account, adjust it accordingly
        if (sizeConstraint.contains(SizeConstraint.PORTS)) {
            // Check if we have a minimum size required for all ports
            if (minSizeForPorts != null) {
                nodeSize.x = Math.max(nodeSize.x, minSizeForPorts.x);
                nodeSize.y = Math.max(nodeSize.y, minSizeForPorts.y);
            }
            
            // If we account for labels, we need to have the size account for labels placed on the
            // inside of the node
            if (accountForLabels) {
                nodeSize.x = Math.max(nodeSize.x,
                        requiredPortLabelSpace.left + requiredPortLabelSpace.right + portSpacing);
                nodeSize.y = Math.max(nodeSize.y,
                        requiredPortLabelSpace.top + requiredPortLabelSpace.bottom + portSpacing);
            }
        }
        
        // If the node label is to be accounted for, add its required space to the node size
        if (sizeConstraint.contains(SizeConstraint.NODE_LABELS) && !node.getLabels().isEmpty()) {
            EnumSet<NodeLabelPlacement> nodeLabelPlacement =
                    node.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT);
            
            // Check if the label is to be placed inside or outside the node
            if (nodeLabelPlacement.contains(NodeLabelPlacement.INSIDE)) {
                nodeSize.x += requiredNodeLabelSpace.left + requiredNodeLabelSpace.right;
                nodeSize.y += requiredNodeLabelSpace.top + requiredNodeLabelSpace.bottom;

                // For center placement, the insets don't cover everything
                if (nodeLabelPlacement.contains(NodeLabelPlacement.V_CENTER)) {
                    nodeSize.y += nodeLabelsBoundingBox.y + 2 * labelSpacing;
                }

                if (nodeLabelPlacement.contains(NodeLabelPlacement.H_CENTER)) {
                    nodeSize.x += nodeLabelsBoundingBox.x + 2 * labelSpacing;
                }
            } else if (nodeLabelPlacement.contains(NodeLabelPlacement.OUTSIDE)) {
                // The node must be at least as high or wide as the label
                if (nodeLabelPlacement.contains(NodeLabelPlacement.V_TOP)
                        || nodeLabelPlacement.contains(NodeLabelPlacement.V_BOTTOM)) {
                    
                    nodeSize.x = Math.max(nodeSize.x, nodeLabelsBoundingBox.x);
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.V_CENTER)) {
                    nodeSize.y = Math.max(nodeSize.y, nodeLabelsBoundingBox.y);
                }
            }
        }
        
        // Respect minimum size
        if (sizeConstraint.contains(SizeConstraint.MINIMUM_SIZE)) {
            double minWidth = node.getProperty(LayoutOptions.MIN_WIDTH);
            double minHeight = node.getProperty(LayoutOptions.MIN_HEIGHT);
            
            // If we are to use default minima, check if the values are properly set
            if (sizeOptions.contains(SizeOptions.DEFAULT_MINIMUM_SIZE)) {
                if (minWidth <= 0) {
                    minWidth = KimlUtil.DEFAULT_MIN_WIDTH;
                }
                
                if (minHeight <= 0) {
                    minHeight = KimlUtil.DEFAULT_MIN_HEIGHT;
                }
            }
            
            // We might have to take the insets into account
            if (sizeOptions.contains(SizeOptions.MINIMUM_SIZE_ACCOUNTS_FOR_INSETS)) {
                if (minWidth > 0) {
                    nodeSize.x = Math.max(nodeSize.x,
                            minWidth
                            + requiredPortLabelSpace.left
                            + requiredPortLabelSpace.right);
                }
                
                if (minHeight > 0) {
                    nodeSize.y = Math.max(nodeSize.y,
                            minHeight
                            + requiredPortLabelSpace.top
                            + requiredPortLabelSpace.bottom);
                }
            } else {
                if (minWidth > 0) {
                    nodeSize.x = Math.max(nodeSize.x, minWidth);
                }
                
                if (minHeight > 0) {
                    nodeSize.y = Math.max(nodeSize.y, minHeight);
                }
            }
        }
    }
    
    /**
     * Calculate how much space the ports will need if they can be freely distributed on their
     * respective node side. The x coordinate of the returned vector will be the minimum width
     * required by the ports on the northern and southern side. The y coordinate, in turn, will
     * be the minimum height required by the ports on the western and eastern side. This means
     * that 
     * 
     * @param node the node to calculate the minimum size for.
     * @param portSpacing the amount of space to leave between ports.
     * @param accountForLabels if {@code true}, the port labels will be taken into account
     *                         when calculating the space requirements.
     * @return minimum size.
     */
    private KVector calculatePortSpaceRequirements(final LNode node, final double portSpacing,
            final boolean accountForLabels) {
        
        // Calculate the maximum width and height, taking the necessary spacing between (and around)
        // the ports into consideration as well
        double maxWidth = Math.max(
                northPortsCount > 0 ? (northPortsCount + 1) * portSpacing + northPortsWidth : 0.0,
                southPortsCount > 0 ? (southPortsCount + 1) * portSpacing + southPortsWidth : 0.0);
        double maxHeight = Math.max(
                westPortsCount > 0 ? (westPortsCount + 1) * portSpacing + westPortsHeight : 0.0,
                eastPortsCount > 0 ? (eastPortsCount + 1) * portSpacing + eastPortsHeight : 0.0);
        
        return new KVector(maxWidth, maxHeight);
    }
    
    /**
     * For fixed node positions, returns the minimum size of the node to contain all ports.
     * 
     * @param node the node to calculate the minimum size for.
     * @param accountForLabels
     * @return
     */
    private KVector calculateMaxPortPositions(final LNode node, final boolean accountForLabels) {
        KVector result = new KVector();
        
        // Iterate over the ports
        for (LPort port : node.getPorts()) {
            switch (port.getSide()) {
            case WEST:
            case EAST:
                result.y = Math.max(result.y,
                        port.getPosition().y
                        + port.getSize().y
                        + (accountForLabels ? port.getMargin().bottom : 0.0));
                break;
                
            case NORTH:
            case SOUTH:
                result.x = Math.max(result.x,
                        port.getPosition().x
                        + port.getSize().x
                        + (accountForLabels ? port.getMargin().right : 0.0));
                break;
            }
        }
        
        return result;
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    // PORT PLACEMENT

    /**
     * Places the given node's ports. If the node wasn't resized at all and port constraints are set
     * to either {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}, the port
     * positions are not touched.
     * 
     * @param node the node whose ports to place.
     * @param originalNodeSize the node's size before it was (possibly) resized.
     */
    private void placePorts(final LNode node, final KVector originalNodeSize) {
        PortConstraints portConstraints = node.getProperty(LayoutOptions.PORT_CONSTRAINTS);
        
        if (portConstraints == PortConstraints.FIXED_POS) {
            // Fixed Position
            placeFixedPosNodePorts(node);
        } else if (portConstraints == PortConstraints.FIXED_RATIO) {
            // Fixed Ratio
            placeFixedRatioNodePorts(node);
        } else {
            // Free, Fixed Side, Fixed Order
            if (node.getProperty(LayoutOptions.HYPERNODE)
                    || (node.getSize().x == 0 && node.getSize().y == 0)) {
                
                placeHypernodePorts(node);
            } else {
                placeNodePorts(node);
            }
        }
    }
    
    /**
     * Places the ports of a node assuming that the port constraints are set to fixed port positions.
     * Ports still need to be placed, though, because the node may have been resized.
     * 
     * @param node the node whose ports to place.
     */
    private void placeFixedPosNodePorts(final LNode node) {
        KVector nodeSize = node.getSize();

        for (LPort port : node.getPorts()) {
            float portOffset = port.getProperty(Properties.OFFSET);
            
            switch (port.getSide()) {
            case WEST:
                port.getPosition().x = -port.getSize().x - portOffset;
                break;
            case EAST:
                port.getPosition().x = nodeSize.x + portOffset;
                break;
            case NORTH:
                port.getPosition().y = -port.getSize().y - portOffset;
                break;
            case SOUTH:
                port.getPosition().y = nodeSize.y + portOffset;
                break;
            }
        }
    }

    /**
     * Places the ports of a node keeping the ratio between their position and the length of their
     * respective side intact.
     * 
     * @param node the node whose ports to place.
     */
    private void placeFixedRatioNodePorts(final LNode node) {
        KVector nodeSize = node.getSize();
        
        // Adjust port positions depending on port side. Eastern ports have to have their x coordinate
        // set to the node's current width; the same goes for the y coordinate of southern ports
        for (LPort port : node.getPorts()) {
            float portOffset = port.getProperty(Properties.OFFSET);
            
            switch (port.getSide()) {
            case WEST:
                port.getPosition().y = nodeSize.y * port.getProperty(Properties.PORT_RATIO_OR_POSITION);
                port.getPosition().x = -port.getSize().x - portOffset;
                break;
            case EAST:
                port.getPosition().y = nodeSize.y * port.getProperty(Properties.PORT_RATIO_OR_POSITION);
                port.getPosition().x = nodeSize.x + portOffset;
                break;
            case NORTH:
                port.getPosition().x = nodeSize.x * port.getProperty(Properties.PORT_RATIO_OR_POSITION);
                port.getPosition().y = -port.getSize().y - portOffset;
                break;
            case SOUTH:
                port.getPosition().x = nodeSize.x * port.getProperty(Properties.PORT_RATIO_OR_POSITION);
                port.getPosition().y = nodeSize.y + portOffset;
                break;
            }
        }
    }
    
    /**
     * Places the ports of a node, assuming that the ports are not fixed in their position or ratio.
     * 
     * @param node the node whose ports to place.
     */
    private void placeNodePorts(final LNode node) {
        KVector nodeSize = node.getSize();
        boolean accountForLabels =
                node.getProperty(LayoutOptions.SIZE_CONSTRAINT).contains(SizeConstraint.PORT_LABELS);
        
        // Compute the space to be left between the ports
        // Note: If the size constraints of this node are empty, the height and width of the ports
        // on each side are zero. That is intentional: if this wasn't the case, bad things would
        // happen if the ports would actually need more size than the node at its current (unchanged)
        // size would be able to provide.
        double westDelta = (nodeSize.y - westPortsHeight) / (westPortsCount + 1);
        double westY = nodeSize.y - westDelta;
        double eastDelta = (nodeSize.y - eastPortsHeight) / (eastPortsCount + 1);
        double eastY = eastDelta;
        double northDelta = (nodeSize.x - northPortsWidth) / (northPortsCount + 1);
        double northX = northDelta;
        double southDelta = (nodeSize.x - southPortsWidth) / (southPortsCount + 1);
        double southX = nodeSize.x - southDelta;
        
        // Arrange the ports
        for (LPort port : node.getPorts()) {
            float portOffset = port.getProperty(Properties.OFFSET);
            KVector portSize = port.getSize();
            LInsets portMargins = port.getMargin();
            
            switch (port.getSide()) {
            case WEST:
                port.getPosition().x = -portSize.x - portOffset;
                port.getPosition().y = westY - portSize.y
                        - (accountForLabels ? portMargins.bottom : 0.0);
                westY -= westDelta + portSize.y
                        + (accountForLabels ? portMargins.top + portMargins.bottom : 0.0);
                break;
            case EAST:
                port.getPosition().x = nodeSize.x + portOffset;
                port.getPosition().y = eastY
                        + (accountForLabels ? portMargins.top : 0.0);
                eastY += eastDelta + portSize.y
                        + (accountForLabels ? portMargins.top + portMargins.bottom : 0.0);
                break;
            case NORTH:
                port.getPosition().x = northX
                        + (accountForLabels ? portMargins.left : 0.0);
                port.getPosition().y = -port.getSize().y - portOffset;
                northX += northDelta + portSize.x
                        + (accountForLabels ? portMargins.left + portMargins.right : 0.0);
                break;
            case SOUTH:
                port.getPosition().x = southX - portSize.x
                        - (accountForLabels ? portMargins.right : 0.0);
                port.getPosition().y = nodeSize.y + portOffset;
                southX -= southDelta + portSize.x
                        + (accountForLabels ? portMargins.left + portMargins.right : 0.0);
                break;
            }
        }
    }
    
    /**
     * Places the ports of a hypernode.
     * 
     * @param node the hypernode whose ports to place.
     */
    private void placeHypernodePorts(final LNode node) {
        for (LPort port : node.getPorts()) {
            switch (port.getSide()) {
            case WEST:
                port.getPosition().x = 0;
                port.getPosition().y = node.getSize().y / 2;
                break;
            case EAST:
                port.getPosition().x = node.getSize().x;
                port.getPosition().y = node.getSize().y / 2;
                break;
            case NORTH:
                port.getPosition().x = node.getSize().x / 2;
                port.getPosition().y = 0;
                break;
            case SOUTH:
                port.getPosition().x = node.getSize().x / 2;
                port.getPosition().y = node.getSize().y;
                break;
            }
        }
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    // PLACING NODE LABELS
    
    /**
     * Calculates the position of the node's labels.
     * 
     * @param node the node whose labels to place.
     * @param labelSpacing spacing between labels and other objects.
     */
    private void placeNodeLabels(final LNode node, final double labelSpacing) {
        // Retrieve the first node label, if any
        if (node.getLabels().isEmpty()) {
            return;
        }
        
        // Top left position where the node labels will start to be placed in a vertical stack
        KVector labelGroupPos = new KVector();
        
        // Retrieve label placement policy
        EnumSet<NodeLabelPlacement> nodeLabelPlacement =
                node.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT);
        
        // This method only sets insets if the node label is to be placed on the inside
        if (nodeLabelPlacement.contains(NodeLabelPlacement.INSIDE)) {
            // Y coordinate
            if (nodeLabelPlacement.contains(NodeLabelPlacement.V_TOP)) {
                labelGroupPos.y = requiredPortLabelSpace.top + labelSpacing;
            } else if (nodeLabelPlacement.contains(NodeLabelPlacement.V_CENTER)) {
                labelGroupPos.y = (node.getSize().y - nodeLabelsBoundingBox.y) / 2.0;
            } else if (nodeLabelPlacement.contains(NodeLabelPlacement.V_BOTTOM)) {
                labelGroupPos.y = node.getSize().y - requiredPortLabelSpace.bottom
                        - nodeLabelsBoundingBox.y - labelSpacing;
            }
            
            // X coordinate
            if (nodeLabelPlacement.contains(NodeLabelPlacement.H_LEFT)) {
                labelGroupPos.x = requiredPortLabelSpace.left + labelSpacing;
            } else if (nodeLabelPlacement.contains(NodeLabelPlacement.H_CENTER)) {
                labelGroupPos.x = (node.getSize().x - nodeLabelsBoundingBox.x) / 2.0;
            } else if (nodeLabelPlacement.contains(NodeLabelPlacement.H_RIGHT)) {
                labelGroupPos.x = node.getSize().x - requiredPortLabelSpace.right
                        - nodeLabelsBoundingBox.x - labelSpacing;
            }
        } else if (nodeLabelPlacement.contains(NodeLabelPlacement.OUTSIDE)) {
            // TODO: Outside placement doesn't take ports and port labels into account yet.
            
            // Different placement logic depending on whether horizontal or vertical placement
            // is prioritized
            if (nodeLabelPlacement.contains(NodeLabelPlacement.H_PRIORITY)) {
                boolean leftOrRight = false;
                
                // X coordinate
                if (nodeLabelPlacement.contains(NodeLabelPlacement.H_LEFT)) {
                    labelGroupPos.x = -(nodeLabelsBoundingBox.x + labelSpacing);
                    leftOrRight = true;
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.H_CENTER)) {
                    labelGroupPos.x = (node.getSize().x - nodeLabelsBoundingBox.x) / 2.0;
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.H_RIGHT)) {
                    labelGroupPos.x = node.getSize().x + labelSpacing;
                    leftOrRight = true;
                }
                
                // Y coordinate
                if (nodeLabelPlacement.contains(NodeLabelPlacement.V_TOP)) {
                    if (leftOrRight) {
                        labelGroupPos.y = 0;
                    } else {
                        labelGroupPos.y = -(nodeLabelsBoundingBox.y + labelSpacing);
                    }
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.V_CENTER)) {
                    labelGroupPos.y = (node.getSize().y - nodeLabelsBoundingBox.y) / 2.0;
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.V_BOTTOM)) {
                    if (leftOrRight) {
                        labelGroupPos.y = node.getSize().y - nodeLabelsBoundingBox.y;
                    } else {
                        labelGroupPos.y = node.getSize().y + labelSpacing;
                    }
                }
            } else {
                boolean topOrBottom = false;
                
                // Y coordinate
                if (nodeLabelPlacement.contains(NodeLabelPlacement.V_TOP)) {
                    labelGroupPos.y = -(nodeLabelsBoundingBox.y + labelSpacing);
                    topOrBottom = true;
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.V_CENTER)) {
                    labelGroupPos.y = (node.getSize().y - nodeLabelsBoundingBox.y) / 2.0;
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.V_BOTTOM)) {
                    labelGroupPos.y = node.getSize().y + labelSpacing;
                    topOrBottom = true;
                }
                
                // X coordinate
                if (nodeLabelPlacement.contains(NodeLabelPlacement.H_LEFT)) {
                    if (topOrBottom) {
                        labelGroupPos.x = 0;
                    } else {
                        labelGroupPos.x = -(nodeLabelsBoundingBox.x + labelSpacing);
                    }
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.H_CENTER)) {
                    labelGroupPos.x = (node.getSize().x - nodeLabelsBoundingBox.x) / 2.0;
                } else if (nodeLabelPlacement.contains(NodeLabelPlacement.H_RIGHT)) {
                    if (topOrBottom) {
                        labelGroupPos.x = node.getSize().x - nodeLabelsBoundingBox.x;
                    } else {
                        labelGroupPos.x = node.getSize().x + labelSpacing;
                    }
                }
            }
        }
        
        // Place labels
        applyNodeLabelPositions(node, labelGroupPos, labelSpacing);
    }
    
    /**
     * Places the given node's labels in a vertical stack, starting at the given position.
     * 
     * @param node the node whose labels are to be placed.
     * @param startPosition coordinates where the first label is to be placed.
     * @param labelSpacing space to be left between the labels.
     */
    private void applyNodeLabelPositions(final LNode node, final KVector startPosition,
            final double labelSpacing) {
        
        // The horizontal alignment depends on where the labels are placed exactly
        EnumSet<NodeLabelPlacement> nodeLabelPlacement =
                node.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT);
        NodeLabelPlacement horizontalPlacement = NodeLabelPlacement.H_CENTER;
        
        if (nodeLabelPlacement.contains(NodeLabelPlacement.INSIDE)) {
            // Inside placement
            if (nodeLabelPlacement.contains(NodeLabelPlacement.H_LEFT)) {
                horizontalPlacement = NodeLabelPlacement.H_LEFT;
            } else if (nodeLabelPlacement.contains(NodeLabelPlacement.H_RIGHT)) {
                horizontalPlacement = NodeLabelPlacement.H_RIGHT;
            }
        } else {
            // Outside placement; alignment is reversed
            if (nodeLabelPlacement.contains(NodeLabelPlacement.H_LEFT)) {
                horizontalPlacement = NodeLabelPlacement.H_RIGHT;
            } else if (nodeLabelPlacement.contains(NodeLabelPlacement.H_RIGHT)) {
                horizontalPlacement = NodeLabelPlacement.H_LEFT;
            }
        }
        
        // We have to keep track of the current y coordinate
        double currentY = startPosition.y;
        
        // Place all labels
        for (LLabel label : node.getLabels()) {
            // Apply y coordinate
            label.getPosition().y = currentY;
            
            // The x coordinate depends on the H_xxx constants
            if (horizontalPlacement == NodeLabelPlacement.H_LEFT) {
                label.getPosition().x = startPosition.x;
            } else if (horizontalPlacement == NodeLabelPlacement.H_CENTER) {
                label.getPosition().x = startPosition.x
                        + (nodeLabelsBoundingBox.x - label.getSize().x) / 2.0; 
            } else if (horizontalPlacement == NodeLabelPlacement.H_RIGHT) {
                label.getPosition().x = startPosition.x + nodeLabelsBoundingBox.x - label.getSize().x;
            }
            
            // Update y position
            currentY += label.getSize().y + labelSpacing;
        }
    }
    
}
