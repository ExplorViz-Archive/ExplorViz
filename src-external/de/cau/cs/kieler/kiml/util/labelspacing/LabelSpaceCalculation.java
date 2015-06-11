/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2015 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.kiml.util.labelspacing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.LabelAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.NodeAdapter;
import de.cau.cs.kieler.kiml.util.nodespacing.Rectangle;
import de.cau.cs.kieler.kiml.util.nodespacing.Spacing.Insets;

/**
 * Utility class for node label space calculation.
 *
 * @author csp
 */
public final class LabelSpaceCalculation {
    
    // Prevent instantiation.
    private LabelSpaceCalculation() {
    }

    /**
     * Calculates the space required to accommodate the node labels (if any) and sets
     * {@link #requiredNodeLabelSpace} as well as {@link #nodeLabelsBoundingBox}. If inside labels are
     * placed at the top or at the bottom, the top or bottom insets are set. If it is centered
     * vertically, the left or right insets are set if the labels are horizontally aligned leftwards
     * or rightwards. If they are centered in both directions, no insets are set. If they are placed
     * outside the node, no insets are set.
     * 
     * @param node
     *            the node whose labels are to be placed.
     * @param labelSpacing
     *            the default label spacing.
     * @return the adjusted insets.
     */
    public static Insets calculateRequiredNodeLabelSpace(final NodeAdapter<?> node,
            final double labelSpacing) {

        return calculateRequiredNodeLabelSpace(node, labelSpacing,
                        new HashMap<LabelLocation, LabelGroup>(), new Insets(node.getInsets()));
    }

    /**
     * Calculates the space required to accommodate the node labels (if any) and sets
     * {@link #requiredNodeLabelSpace} as well as {@link #nodeLabelsBoundingBox}. If inside labels are
     * placed at the top or at the bottom, the top or bottom insets are set. If it is centered
     * vertically, the left or right insets are set if the labels are horizontally aligned leftwards
     * or rightwards. If they are centered in both directions, no insets are set. If they are placed
     * outside the node, no insets are set.
     * 
     * @param node
     *            the node whose labels are to be placed.
     * @param labelSpacing
     *            the default label spacing.
     * @param labelGroupsBoundingBoxes
     *            map of locations to corresponding bounding boxes.
     * @param insets
     *            the insets to adjust.
     * @return the adjusted insets.
     */
    public static Insets calculateRequiredNodeLabelSpace(final NodeAdapter<?> node,
            final double labelSpacing, final Map<LabelLocation, LabelGroup> labelGroupsBoundingBoxes,
            final Insets insets) {

        // Check if there are any labels
        if (!node.getLabels().iterator().hasNext()) {
            return insets;
        }
        
        // Retrieve the node's label placement policy
        final LabelLocation nodeLabelPlacement = LabelLocation.fromNodeLabelPlacement(
                node.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT));

        // Compute a bounding box for each location where labels should be placed.
        // The size is calculated from the size of all labels stacked vertically at that location.

        for (final LabelAdapter<?> label : node.getLabels()) {
            LabelLocation labelPlacement =
                    LabelLocation.fromNodeLabelPlacement(label
                            .getProperty(LayoutOptions.NODE_LABEL_PLACEMENT));
            // If no valid placement is set on the label, use the node's placement policy.
            if (labelPlacement == LabelLocation.UNDEFINED) {
                labelPlacement = nodeLabelPlacement;
            }
            // Save the location of this label in its id field for later use.
            label.setVolatileId(labelPlacement.ordinal());
            // Create or retrieve the label group for the current label.
            final Rectangle boundingBox =
                    retrieveLabelGroupsBoundingBox(labelGroupsBoundingBoxes, labelPlacement);
            boundingBox.width = Math.max(boundingBox.width, label.getSize().x);
            boundingBox.height += label.getSize().y + labelSpacing;
        }

        // Calculate the node label space required inside the node (only label groups on the inside
        // are relevant here).
        for (final Entry<LabelLocation, LabelGroup> entry : labelGroupsBoundingBoxes.entrySet()) {
            final Rectangle boundingBox = entry.getValue();
            // From each existing label group, remove the last superfluous label spacing
            // (the mere existence of a label group implies that it contains at least one label)
            boundingBox.height -= labelSpacing;
            switch (entry.getKey()) {
            // Top 3 label groups
            case IN_T_L:
            case IN_T_C:
            case IN_T_R:
                insets.top =
                        Math.max(insets.top, boundingBox.height
                                + labelSpacing);
                break;
            // Left label group
            case IN_C_L:
                insets.left =
                        Math.max(insets.left, boundingBox.width
                                + labelSpacing);
                break;
            // Right label group
            case IN_C_R:
                insets.right =
                        Math.max(insets.right, boundingBox.width
                                + labelSpacing);
                break;
            // Bottom 3 label groups
            case IN_B_L:
            case IN_B_C:
            case IN_B_R:
                insets.bottom =
                        Math.max(insets.bottom, boundingBox.height
                                + labelSpacing);
                break;
            }
        }
        return insets;
    }

    /**
     * Returns the bounding box of all node labels placed at the specified location. If there is no
     * bounding box for the location yet, a new one is added and returned.
     *
     * @param labelGroupsBoundingBoxes
     *            map of already existing bounding boxes.
     * @param location
     *            the location for which to retrieve the bounding box.
     * @return the corresponding bounding box.
     */
    private static Rectangle retrieveLabelGroupsBoundingBox(
            final Map<LabelLocation, LabelGroup> labelGroupsBoundingBoxes,
            final LabelLocation location) {
        if (!labelGroupsBoundingBoxes.containsKey(location)) {
            LabelGroup boundingBox = new LabelGroup();
            labelGroupsBoundingBoxes.put(location, boundingBox);
            return boundingBox;
        } else {
            return labelGroupsBoundingBoxes.get(location);
        }
    }
}
