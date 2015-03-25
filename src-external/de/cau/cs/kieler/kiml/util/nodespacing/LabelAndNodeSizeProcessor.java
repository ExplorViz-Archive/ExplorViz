/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2014 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.kiml.util.nodespacing;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.options.*;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.GraphAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.LabelAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.NodeAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.PortAdapter;
import de.cau.cs.kieler.kiml.util.nodespacing.Spacing.Insets;
import de.cau.cs.kieler.kiml.util.nodespacing.Spacing.Margins;
import de.cau.cs.kieler.klay.layered.intermediate.LabelSideSelector;

/**
 * Calculates node sizes, places ports, and places node and port labels.
 *
 * @see LabelSideSelector
 * @author cds
 * @author uru
 * @author csp
 */
@SuppressWarnings("incomplete-switch")
public class LabelAndNodeSizeProcessor {

	/**
	 * Copy of the
	 * {@link de.cau.cs.kieler.klay.layered.properties.InternalProperties#PORT_RATIO_OR_POSITION}
	 * option. For further information see the documentation found there. We
	 * added this copy here to allow a generic treatment of spacing calculations
	 * for graph elements. See the
	 * {@link de.cau.cs.kieler.kiml.util.nodespacing} package. [programmatically
	 * set]
	 */
	public static final IProperty<Double> PORT_RATIO_OR_POSITION = new Property<Double>(
			"portRatioOrPosition", 0.0);

	/**
	 * The minimal port spacing we use.
	 */
	private static final double MIN_PORT_SPACING = 10;

	/*
	 * Entry point
	 */
	/**
	 * {@inheritDoc}
	 */
	public void process(final GraphAdapter<?> layeredGraph) {
		final double labelSpacing = layeredGraph.getProperty(LayoutOptions.LABEL_SPACING);

		// Iterate over all the graph's nodes
		for (final NodeAdapter<?> node : layeredGraph.getNodes()) {
			/*
			 * Note that, upon Miro's request, each phase of the algorithm was
			 * given a code name.
			 */

			/*
			 * PREPARATIONS: Create new NodeData containing all relevant context
			 * information.
			 */
			final NodeData data = new NodeData(node);
			data.labelSpacing = labelSpacing;
			data.portSpacing = Math.max(MIN_PORT_SPACING,
					node.getProperty(LayoutOptions.PORT_SPACING));

			/*
			 * PHASE 1 (SAD DUCK): PLACE PORT LABELS Port labels are placed and
			 * port margins are calculated.
			 */
			final PortLabelPlacement labelPlacement = node
					.getProperty(LayoutOptions.PORT_LABEL_PLACEMENT);
			final boolean compoundNodeMode = node.isCompoundNode();

			// Place port labels and calculate the margins
			for (final PortAdapter<?> port : node.getPorts()) {
				placePortLabels(port, labelPlacement, compoundNodeMode, labelSpacing);
				calculateAndSetPortMargins(port);
			}

			// Count ports on each side and calculate how much space they
			// require
			calculatePortInformation(data, node.getProperty(LayoutOptions.SIZE_CONSTRAINT)
					.contains(SizeConstraint.PORT_LABELS));

			/*
			 * PHASE 2 (DYNAMIC DONALD): CALCULATE INSETS We know the sides the
			 * ports will be placed at and we know where node labels are to be
			 * placed. Calculate the node's insets accordingly. Also compute the
			 * amount of space the node labels will need if stacked vertically.
			 * Note that we don't have to know the final position of ports and
			 * of node labels to calculate all this stuff.
			 * 
			 * IMPORTANT NOTE: From this point on, the labels' ID fields are
			 * used to assign the location of the labels.
			 */
			calculateRequiredPortLabelSpace(data);
			calculateRequiredNodeLabelSpace(data);

			/*
			 * PHASE 3 (DANGEROUS DUCKLING): RESIZE NODE If the node has labels,
			 * the node insets might have to be adjusted to reserve space for
			 * them, which is what this phase does.
			 */
			resizeNode(data);

			/*
			 * PHASE 4 (DUCK AND COVER): PLACE PORTS The node is resized, taking
			 * all node size constraints into account. The port spacing is not
			 * required for port placement since the placement will be based on
			 * the node's size (if it is not fixed anyway).
			 */
			placePorts(data);

			/*
			 * PHASE 5 (HAPPY DUCK): PLACE NODE LABELS With space reserved for
			 * the node labels, the labels are placed.
			 */
			placeNodeLabels(data);

			/*
			 * CLEANUP (THANKSGIVING): SET NODE INSETS Set the node insets to
			 * include space required for port and node labels. If the labels
			 * were not taken into account when calculating the node's size,
			 * this may result in insets that, taken together, are larger than
			 * the node's actual size.
			 */
			final Insets nodeInsets = new Insets(node.getInsets());
			nodeInsets.left = data.requiredNodeLabelSpace.left + data.requiredPortLabelSpace.left;
			nodeInsets.right = data.requiredNodeLabelSpace.right
					+ data.requiredPortLabelSpace.right;
			nodeInsets.top = data.requiredNodeLabelSpace.top + data.requiredPortLabelSpace.top;
			nodeInsets.bottom = data.requiredNodeLabelSpace.bottom
					+ data.requiredPortLabelSpace.bottom;
			node.setInsets(nodeInsets);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// PORT LABEL PLACEMENT

	/**
	 * Places the labels of the given port, if any.
	 *
	 * @param port
	 *            the port whose labels to place.
	 * @param placement
	 *            the port label placement that applies to the port.
	 * @param compoundNodeMode
	 *            {@code true} if the node contains further nodes in the
	 *            original graph. This influences the inner port label
	 *            placement.
	 * @param labelSpacing
	 *            spacing between labels and other objects.
	 */
	private void placePortLabels(final PortAdapter<?> port, final PortLabelPlacement placement,
			final boolean compoundNodeMode, final double labelSpacing) {

		if (placement.equals(PortLabelPlacement.INSIDE)) {
			placePortLabelsInside(port, compoundNodeMode, labelSpacing);
		} else if (placement.equals(PortLabelPlacement.OUTSIDE)) {
			placePortLabelsOutside(port, labelSpacing);
		}
	}

	/**
	 * Places the labels of the given port on the inside of the port's node.
	 *
	 * @param port
	 *            the port whose labels to place.
	 * @param compoundNodeMode
	 *            {@code true} if the node contains further nodes in the
	 *            original graph. In this case, port labels are not placed next
	 *            to ports, but a little down as well to avoid
	 *            edge-label-crossings if the port has edges connected to the
	 *            node's insides.
	 * @param labelSpacing
	 *            spacing between labels and other objects.
	 */
	private void placePortLabelsInside(final PortAdapter<?> port, final boolean compoundNodeMode,
			final double labelSpacing) {

		ImmutableList<LabelAdapter<?>> labels = ImmutableList.copyOf(port.getLabels());
		if (labels.isEmpty()) {
			return;
		}

		// The initial y position we'll be starting from depends on the port
		// side
		double y = 0;
		switch (port.getSide()) {
			case WEST:
			case EAST:
				// We need the first label's size here and we know that there is
				// at least one label
				y = compoundNodeMode && port.hasCompoundConnections() ? port.getSize().y : ((port
						.getSize().y - labels.get(0).getSize().y) / 2.0) - labelSpacing;
				break;
			case NORTH:
				y = port.getSize().y;
				break;
			case SOUTH:
				y = 0.0;
				break;
		}

		// In the usual case, we simply start at a given y position and place
		// the labels downwards.
		// For southern ports, however, we actually need to start with the last
		// label and place them
		// upwards. We thus first add all labels to a list that we may need to
		// reverse
		if (port.getSide() == PortSide.SOUTH) {
			labels = labels.reverse();
		}

		// Place da labels!
		for (final LabelAdapter<?> label : port.getLabels()) {
			final KVector position = new KVector(port.getPosition());
			switch (port.getSide()) {
				case WEST:
					position.x = port.getSize().x + labelSpacing;
					position.y = y + labelSpacing;

					y += labelSpacing + label.getSize().y;
					break;
				case EAST:
					position.x = -label.getSize().x - labelSpacing;
					position.y = y + labelSpacing;

					y += labelSpacing + label.getSize().y;
					break;
				case NORTH:
					position.x = (port.getSize().x - label.getSize().x) / 2;
					position.y = y + labelSpacing;

					y += labelSpacing + label.getSize().y;
					break;
				case SOUTH:
					position.x = (port.getSize().x - label.getSize().x) / 2;
					position.y = y - labelSpacing - label.getSize().y;

					y -= labelSpacing + label.getSize().y;
					break;
			}
			label.setPosition(position);
		}
	}

	/**
	 * Places the labels of the given port on the outside of the port's node. We
	 * suppose that the first label has label side information. Those are then
	 * used for all labels. We don't support having some labels above and others
	 * below incident edges.
	 *
	 * @param port
	 *            the port whose label to place.
	 * @param labelSpacing
	 *            spacing between labels and other objects.
	 */
	private void placePortLabelsOutside(final PortAdapter<?> port, final double labelSpacing) {
		ImmutableList<LabelAdapter<?>> labels = ImmutableList.copyOf(port.getLabels());
		if (labels.isEmpty()) {
			return;
		}

		// Retrieve the first label's side
		LabelSide labelSide = labels.get(0).getSide();
		// Default is BELOW.
		labelSide = labelSide == LabelSide.UNKNOWN ? LabelSide.BELOW : labelSide;

		// The initial y position we'll be starting from depends on port and
		// label sides
		double y = 0;
		switch (port.getSide()) {
			case WEST:
			case EAST:
				if (labelSide == LabelSide.BELOW) {
					y = port.getSize().y;
				}
				break;

			case SOUTH:
				y = port.getSize().y;
				break;
		}

		// If labels are below incident edges, we simply start at a given y
		// position and place the
		// labels downwards. Of they are placed above or if we have a northern
		// port, however, we
		// actually need to start with the last label and place them upwards. We
		// thus first add all
		// labels to a list that we may need to reverse
		if ((port.getSide() == PortSide.NORTH) || (labelSide == LabelSide.ABOVE)) {
			labels = labels.reverse();
		}

		for (final LabelAdapter<?> label : labels) {
			final KVector position = new KVector(label.getPosition());
			if (labelSide == LabelSide.ABOVE) {
				// Place label "above" edges
				switch (port.getSide()) {
					case WEST:
						position.x = -label.getSize().x - labelSpacing;
						position.y = y - labelSpacing - label.getSize().y;

						y -= labelSpacing + label.getSize().y;
						break;
					case EAST:
						position.x = port.getSize().x + labelSpacing;
						position.y = y - labelSpacing - label.getSize().y;

						y -= labelSpacing + label.getSize().y;
						break;
					case NORTH:
						position.x = -label.getSize().x - labelSpacing;
						position.y = y - labelSpacing - label.getSize().y;

						y -= labelSpacing + label.getSize().y;
						break;
					case SOUTH:
						position.x = -label.getSize().x - labelSpacing;
						position.y = y + labelSpacing;

						y += labelSpacing + label.getSize().y;
						break;
				}
			} else {
				// Place label "below" edges
				switch (port.getSide()) {
					case WEST:
						position.x = -label.getSize().x - labelSpacing;
						position.y = y + labelSpacing;

						y += labelSpacing + label.getSize().y;
						break;
					case EAST:
						position.x = port.getSize().x + labelSpacing;
						position.y = y + labelSpacing;

						y += labelSpacing + label.getSize().y;
						break;
					case NORTH:
						position.x = port.getSize().x + labelSpacing;
						position.y = y - labelSpacing - label.getSize().y;

						y -= labelSpacing + label.getSize().y;
						break;
					case SOUTH:
						position.x = port.getSize().x + labelSpacing;
						position.y = y + labelSpacing;

						y += labelSpacing + label.getSize().y;
						break;
				}
			}
			label.setPosition(position);
		}
	}

	/**
	 * Calculates the port's margins such that its labels are part of them and
	 * sets them accordingly.
	 *
	 * @param port
	 *            the port whose margins to calculate.
	 */
	private void calculateAndSetPortMargins(final PortAdapter<?> port) {
		// Get the port's labels, if any
		final Iterable<LabelAdapter<?>> labels = port.getLabels();
		if (labels.iterator().hasNext()) {
			final Rectangle portBox = new Rectangle(0.0, 0.0, port.getSize().x, port.getSize().y);

			// Add all labels to the port's bounding box
			for (final LabelAdapter<?> label : labels) {
				final Rectangle labelBox = new Rectangle(label.getPosition().x,
						label.getPosition().y, label.getSize().x, label.getSize().y);

				// Calculate the union of the two bounding boxes and calculate
				// the margins
				portBox.union(labelBox);
			}

			final Margins margin = new Margins(port.getMargin());
			margin.top = -portBox.y;
			margin.bottom = (portBox.y + portBox.height) - port.getSize().y;
			margin.left = -portBox.x;
			margin.right = (portBox.x + portBox.width) - port.getSize().x;
			port.setMargin(margin);
		}
	}

	/**
	 * Calculates the width of ports on the northern and southern sides, and the
	 * height of ports on the western and eastern sides of the given node. The
	 * information are stored in the class fields and are used later on when
	 * calculating the minimum node size and when placing ports.
	 *
	 * @param data
	 *            the data containing the node to calculate the port information
	 *            for.
	 * @param accountForLabels
	 *            if {@code true}, the port labels will be taken into account
	 *            when calculating the port information.
	 */
	private void calculatePortInformation(final NodeData data, final boolean accountForLabels) {
		// Iterate over the ports
		for (final PortAdapter<?> port : data.node.getPorts()) {
			switch (port.getSide()) {
				case WEST:
					data.westPortsCount++;
					data.westPortsHeight += port.getSize().y
							+ (accountForLabels ? port.getMargin().bottom + port.getMargin().top
									: 0.0);
					break;
				case EAST:
					data.eastPortsCount++;
					data.eastPortsHeight += port.getSize().y
							+ (accountForLabels ? port.getMargin().bottom + port.getMargin().top
									: 0.0);
					break;
				case NORTH:
					data.northPortsCount++;
					data.northPortsWidth += port.getSize().x
							+ (accountForLabels ? port.getMargin().left + port.getMargin().right
									: 0.0);
					break;
				case SOUTH:
					data.southPortsCount++;
					data.southPortsWidth += port.getSize().x
							+ (accountForLabels ? port.getMargin().left + port.getMargin().right
									: 0.0);
					break;
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// INSETS CALCULATION

	/**
	 * Calculates the space required to accommodate all port labels and sets
	 * {@link #requiredPortLabelSpace}. Also counts the number of ports on each
	 * side of the node.
	 *
	 * <p>
	 * <i>Note:</i> We currently only support one label per port.
	 * </p>
	 *
	 * @param data
	 *            the data containing the node whose insets to calculate and to
	 *            set.
	 */
	private void calculateRequiredPortLabelSpace(final NodeData data) {
		// Iterate over the ports and look at their margins
		for (final PortAdapter<?> port : data.node.getPorts()) {
			switch (port.getSide()) {
				case WEST:
					data.requiredPortLabelSpace.left = Math.max(data.requiredPortLabelSpace.left,
							port.getMargin().right);
					break;
				case EAST:
					data.requiredPortLabelSpace.right = Math.max(data.requiredPortLabelSpace.right,
							port.getMargin().left);
					break;
				case NORTH:
					data.requiredPortLabelSpace.top = Math.max(data.requiredPortLabelSpace.top,
							port.getMargin().bottom);
					break;
				case SOUTH:
					data.requiredPortLabelSpace.bottom = Math.max(
							data.requiredPortLabelSpace.bottom, port.getMargin().top);
					break;
			}
		}
	}

	/**
	 * Calculates the space required to accommodate the node labels (if any) and
	 * sets {@link #requiredNodeLabelSpace} as well as
	 * {@link #nodeLabelsBoundingBox}. If the labels are placed at the top or at
	 * the bottom, the top or bottom insets are set. If it is centered
	 * vertically, the left or right insets are set if the labels are
	 * horizontally aligned leftwards or rightwards. If they are centered in
	 * both directions, no insets are set. If they are placed outside the node,
	 * no insets are set.
	 *
	 * @param data
	 *            the data containing the node in question.
	 */
	private void calculateRequiredNodeLabelSpace(final NodeData data) {
		// Check if there are any labels
		if (!data.node.getLabels().iterator().hasNext()) {
			return;
		}

		// Retrieve the node's label placement policy
		final Location nodeLabelPlacement = Location.fromNodeLabelPlacement(data.node
				.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT));

		// Compute a bounding box for each location where labels should be
		// placed.
		// The size is calculated from the size of all labels stacked vertically
		// at that location.

		for (final LabelAdapter<?> label : data.node.getLabels()) {
			Location labelPlacement = Location.fromNodeLabelPlacement(label
					.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT));
			// If no valid placement is set on the label, use the node's
			// placement policy.
			if (labelPlacement == Location.IGNORED) {
				labelPlacement = nodeLabelPlacement;
			}
			// Save the location of this label in its id field for later use.
			label.setVolatileId(labelPlacement.ordinal());
			// Create or retrieve the label group for the current label.
			final Rectangle boundingBox = data.retrieveLabelGroupsBoundingBox(labelPlacement);
			boundingBox.width = Math.max(boundingBox.width, label.getSize().x);
			boundingBox.height += label.getSize().y + data.labelSpacing;
		}

		// Calculate the node label space required inside the node (only label
		// groups on the inside
		// are relevant here).
		for (final Entry<Location, LabelGroup> entry : data.labelGroupsBoundingBoxes.entrySet()) {
			final Rectangle boundingBox = entry.getValue();
			// From each existing label group, remove the last superfluous label
			// spacing
			// (the mere existence of a label group implies that it contains at
			// least one label)
			boundingBox.height -= data.labelSpacing;
			switch (entry.getKey()) {
			// Top 3 label groups
				case IN_T_L:
				case IN_T_C:
				case IN_T_R:
					data.requiredNodeLabelSpace.top = Math.max(data.requiredNodeLabelSpace.top,
							boundingBox.height + data.labelSpacing);
					break;
				// Left label group
				case IN_C_L:
					data.requiredNodeLabelSpace.left = Math.max(data.requiredNodeLabelSpace.left,
							boundingBox.width + data.labelSpacing);
					break;
				// Right label group
				case IN_C_R:
					data.requiredNodeLabelSpace.right = Math.max(data.requiredNodeLabelSpace.right,
							boundingBox.width + data.labelSpacing);
					break;
				// Bottom 3 label groups
				case IN_B_L:
				case IN_B_C:
				case IN_B_R:
					data.requiredNodeLabelSpace.bottom = Math.max(
							data.requiredNodeLabelSpace.bottom, boundingBox.height
									+ data.labelSpacing);
					break;
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// NODE RESIZING

	/**
	 * Resizes the given node subject to the sizing constraints and options.
	 *
	 * @param data
	 *            the data containing the node to resize.
	 */
	private void resizeNode(final NodeData data) {

		final KVector nodeSize = data.node.getSize();
		final KVector originalNodeSize = new KVector(nodeSize);
		final EnumSet<SizeConstraint> sizeConstraint = data.node
				.getProperty(LayoutOptions.SIZE_CONSTRAINT);
		final EnumSet<SizeOptions> sizeOptions = data.node.getProperty(LayoutOptions.SIZE_OPTIONS);
		final PortConstraints portConstraints = data.node
				.getProperty(LayoutOptions.PORT_CONSTRAINTS);
		final boolean accountForLabels = sizeConstraint.contains(SizeConstraint.PORT_LABELS);

		// If the size constraint is empty, we can't do anything
		if (sizeConstraint.isEmpty()) {
			return;
		}

		// It's not empty, so we will change the node size; we start by
		// resetting the size to zero
		nodeSize.x = 0.0;
		nodeSize.y = 0.0;

		// Find out how large the node will have to be to accommodate all ports.
		// If port
		// constraints are set to FIXED_RATIO, we can't do anything smart,
		// really; in this
		// case we will just assume the original node size to be the minimum for
		// ports
		KVector minSizeForPorts = null;
		switch (portConstraints) {
			case FREE:
			case FIXED_SIDE:
			case FIXED_ORDER:
				// Calculate the space necessary to accommodate all ports
				minSizeForPorts = calculatePortSpaceRequirements(data, data.portSpacing,
						accountForLabels);
				break;

			case FIXED_RATIO:
				// Keep original node size
				minSizeForPorts = new KVector(originalNodeSize);
				break;

			case FIXED_POS:
				// Find the maximum position of ports
				minSizeForPorts = calculateMinNodeSizeForFixedPorts(data.node, accountForLabels);
				break;
		}

		// If the node size should take port space requirements into account,
		// adjust it accordingly
		if (sizeConstraint.contains(SizeConstraint.PORTS)) {
			// Check if we have a minimum size required for all ports
			if (minSizeForPorts != null) {
				nodeSize.x = Math.max(nodeSize.x, minSizeForPorts.x);
				nodeSize.y = Math.max(nodeSize.y, minSizeForPorts.y);
			}

			// If we account for labels, we need to have the size account for
			// labels placed on the
			// inside of the node (this only affects port label placement
			// INSIDE; OUTSIDE is already
			// part of minSizeForPorts)
			if (accountForLabels) {
				nodeSize.x = Math.max(nodeSize.x, data.requiredPortLabelSpace.left
						+ data.requiredPortLabelSpace.right + data.portSpacing);
				nodeSize.y = Math.max(nodeSize.y, data.requiredPortLabelSpace.top
						+ data.requiredPortLabelSpace.bottom + data.portSpacing);
			}
		}

		// If the node label is to be accounted for, add its required space to
		// the node size
		if (sizeConstraint.contains(SizeConstraint.NODE_LABELS)
				&& data.node.getLabels().iterator().hasNext()) {
			enlargeNodeSizeForLabels(data, data.labelSpacing, nodeSize);
		}

		// Respect minimum size
		if (sizeConstraint.contains(SizeConstraint.MINIMUM_SIZE)) {
			double minWidth = data.node.getProperty(LayoutOptions.MIN_WIDTH);
			double minHeight = data.node.getProperty(LayoutOptions.MIN_HEIGHT);

			// If we are to use default minima, check if the values are properly
			// set
			if (sizeOptions.contains(SizeOptions.DEFAULT_MINIMUM_SIZE)) {
				if (minWidth <= 0) {
					minWidth = 20;
				}

				if (minHeight <= 0) {
					minHeight = 20;
				}
			}

			// We might have to take the insets into account
			if (sizeOptions.contains(SizeOptions.MINIMUM_SIZE_ACCOUNTS_FOR_INSETS)) {
				if (minWidth > 0) {
					nodeSize.x = Math.max(nodeSize.x, minWidth + data.requiredPortLabelSpace.left
							+ data.requiredPortLabelSpace.right);
				}

				if (minHeight > 0) {
					nodeSize.y = Math.max(nodeSize.y, minHeight + data.requiredPortLabelSpace.top
							+ data.requiredPortLabelSpace.bottom);
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
		// apply the calculated node size back to the wrapped node
		data.node.setSize(nodeSize);
	}

	/**
	 * Enlarges the node size to the required label space.
	 * <p>
	 * For outside labels, the minimal {width|height} results from the maximal
	 * sum of the 3 {top|bottom|left|right} label groups' {width|height}.
	 * </p>
	 * <p>
	 * For inside labels, the minimal height results from the sum of
	 * <ul>
	 * <li>top inset,</li>
	 * <li>maximum of height of 3 vertical centered label groups,</li>
	 * <li>bottom inset.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The minimal inside width results from the maximal sum of the 3 vertical
	 * {top|centered|bottom} label groups' width.
	 * </p>
	 * 
	 * @param data
	 *            the data containing the node to resize.
	 * @param labelSpacing
	 *            the amount of space to leave between labels and other objects.
	 * @param nodeSize
	 *            the node's size to adjust.
	 */
	private void enlargeNodeSizeForLabels(final NodeData data, final double labelSpacing,
			final KVector nodeSize) {

		double sumHeightOusideLeft = 0; // sum of heights of the 3 outside left
										// label groups
		double sumHeightOusideRight = 0; // sum of heights of the 3 outside
											// right label groups
		double sumWidthOutsideTop = 0; // sum of widths of the 3 outside top
										// label groups
		double sumWidthOutsideBottom = 0; // sum of widths of the 3 outside
											// bottom label groups
		double maxHeightInsideCenter = 0; // max of heights of the 3 inside
											// vertical center label groups
		double sumWidthInsideTop = 0; // sum of widths of the 3 inside vertical
										// top label groups
		double sumWidthInsideCenter = 0; // sum of widths of the 3 inside
											// vertical center label groups
		double sumWidthInsideBottom = 0; // sum of widths of the 3 inside
											// vertical bottom label groups

		for (final Entry<Location, LabelGroup> entry : data.labelGroupsBoundingBoxes.entrySet()) {
			final Rectangle boundingBox = entry.getValue();
			switch (entry.getKey()) {
			// Inside groups
				case IN_T_L:
				case IN_T_C:
				case IN_T_R:
					sumWidthInsideTop += boundingBox.width + labelSpacing;
					break;
				case IN_C_L:
				case IN_C_C:
				case IN_C_R:
					sumWidthInsideCenter += boundingBox.width + labelSpacing;
					maxHeightInsideCenter = Math.max(maxHeightInsideCenter, boundingBox.height
							+ labelSpacing);
					break;
				case IN_B_L:
				case IN_B_C:
				case IN_B_R:
					sumWidthInsideBottom += boundingBox.width + labelSpacing;
					break;

				// Outside groups

				// Top 3 label groups
				case OUT_T_L:
				case OUT_T_C:
				case OUT_T_R:
					sumWidthOutsideTop += boundingBox.width + labelSpacing;
					break;
				// Bottom 3 label groups
				case OUT_B_L:
				case OUT_B_C:
				case OUT_B_R:
					sumWidthOutsideBottom += boundingBox.width + labelSpacing;
					break;
				// Left 3 label groups
				case OUT_L_T:
				case OUT_L_C:
				case OUT_L_B:
					sumHeightOusideLeft += boundingBox.height + labelSpacing;
					break;
				// Right 3 label groups
				case OUT_R_T:
				case OUT_R_C:
				case OUT_R_B:
					sumHeightOusideRight += boundingBox.height + labelSpacing;
					break;
			}
		}

		// remove additionally added label spacing
		// (possible negative values doesn't affect the outcome of the max
		// function below)
		sumHeightOusideLeft -= labelSpacing;
		sumHeightOusideRight -= labelSpacing;
		sumWidthOutsideTop -= labelSpacing;
		sumWidthOutsideBottom -= labelSpacing;

		// add missing label spacing (only if not zero)
		sumWidthInsideTop += sumWidthInsideTop != 0 ? labelSpacing : 0;
		sumWidthInsideCenter += sumWidthInsideCenter != 0 ? labelSpacing : 0;
		sumWidthInsideBottom += sumWidthInsideBottom != 0 ? labelSpacing : 0;
		double minHeightInside = data.requiredNodeLabelSpace.top + maxHeightInsideCenter
				+ data.requiredNodeLabelSpace.bottom;
		minHeightInside += minHeightInside != 0 ? labelSpacing : 0;

		nodeSize.x = Math.max(nodeSize.x, sumWidthOutsideTop);
		nodeSize.x = Math.max(nodeSize.x, sumWidthInsideTop);
		nodeSize.x = Math.max(nodeSize.x, sumWidthInsideCenter);
		nodeSize.x = Math.max(nodeSize.x, sumWidthInsideBottom);
		nodeSize.x = Math.max(nodeSize.x, sumWidthOutsideBottom);
		nodeSize.y = Math.max(nodeSize.y, sumHeightOusideLeft);
		nodeSize.y = Math.max(nodeSize.y, minHeightInside);
		nodeSize.y = Math.max(nodeSize.y, sumHeightOusideRight);
	}

	/**
	 * Calculate how much space the ports will require at a minimum if they can
	 * be freely distributed on their respective node side. The x coordinate of
	 * the returned vector will be the minimum width required by the ports on
	 * the northern and southern side. The y coordinate, in turn, will be the
	 * minimum height required by the ports on the western and eastern side.
	 * This may include the space required for port labels placed outside of the
	 * node. If port labels are placed inside, their space requirements are not
	 * included in the result.
	 *
	 * @param data
	 *            the data containing the node to calculate the minimum size
	 *            for.
	 * @param portSpacing
	 *            the minimum amount of space to be left between ports if there
	 *            positions are not fixed.
	 * @param accountForLabels
	 *            if {@code true}, the port labels will be taken into account
	 *            when calculating the space requirements.
	 * @return minimum size.
	 */
	private KVector calculatePortSpaceRequirements(final NodeData data, final double portSpacing,
			final boolean accountForLabels) {

		// Calculate the additional port space to be left around the set of
		// ports on each side. If
		// this
		// is not set, we assume the spacing to be the minimum space left
		// between ports
		double additionalWidth;
		double additionalHeight;

		final Margins additionalPortSpace = data.node
				.getProperty(LayoutOptions.ADDITIONAL_PORT_SPACE);
		if (additionalPortSpace == null) {
			additionalWidth = 2 * portSpacing;
			additionalHeight = 2 * portSpacing;
		} else {
			additionalWidth = additionalPortSpace.left + additionalPortSpace.right;
			additionalHeight = additionalPortSpace.top + additionalPortSpace.bottom;
		}

		// Calculate the required width and height, taking the necessary spacing
		// between (and
		// around)
		// the ports into consideration as well
		final double requiredWidth = Math.max(data.northPortsCount > 0 ? additionalWidth
				+ ((data.northPortsCount - 1) * portSpacing) + data.northPortsWidth : 0.0,
				data.southPortsCount > 0 ? additionalWidth
						+ ((data.southPortsCount - 1) * portSpacing) + data.southPortsWidth : 0.0);
		final double requiredHeight = Math.max(data.westPortsCount > 0 ? additionalHeight
				+ ((data.westPortsCount - 1) * portSpacing) + data.westPortsHeight : 0.0,
				data.eastPortsCount > 0 ? additionalHeight
						+ ((data.eastPortsCount - 1) * portSpacing) + data.eastPortsHeight : 0.0);

		return new KVector(requiredWidth, requiredHeight);
	}

	/**
	 * For fixed port positions, returns the minimum size of the node to contain
	 * all ports. The minimum size equals the position plus the size of the most
	 * bottom-right port (biggest x- and y-coordinate)
	 *
	 * @param node
	 *            the node to calculate the minimum size for.
	 * @param accountForLabels
	 *            whether to regard port labels.
	 * @return the minimum size to contain all port positions.
	 */
	private KVector calculateMinNodeSizeForFixedPorts(final NodeAdapter<?> node,
			final boolean accountForLabels) {

		// Port positions must be fixed for this method to be called
		assert node.getProperty(LayoutOptions.PORT_CONSTRAINTS) == PortConstraints.FIXED_POS;

		final KVector result = new KVector();

		// Iterate over the ports
		for (final PortAdapter<?> port : node.getPorts()) {
			switch (port.getSide()) {
				case WEST:
				case EAST:
					result.y = Math.max(result.y, port.getPosition().y + port.getSize().y
							+ (accountForLabels ? port.getMargin().bottom : 0.0));
					break;

				case NORTH:
				case SOUTH:
					result.x = Math.max(result.x, port.getPosition().x + port.getSize().x
							+ (accountForLabels ? port.getMargin().right : 0.0));
					break;
			}
		}

		return result;
	}

	// /////////////////////////////////////////////////////////////////////////////
	// PORT PLACEMENT

	/**
	 * Places the given node's ports. If the node wasn't resized at all and port
	 * constraints are set to either {@link PortConstraints#FIXED_RATIO} or
	 * {@link PortConstraints#FIXED_POS}, the port positions are not touched.
	 *
	 * @param data
	 *            the data containing the node whose ports to place.
	 */
	private void placePorts(final NodeData data) {
		final PortConstraints portConstraints = data.node
				.getProperty(LayoutOptions.PORT_CONSTRAINTS);

		if (portConstraints == PortConstraints.FIXED_POS) {
			// Fixed Position
			placeFixedPosNodePorts(data.node);
		} else if (portConstraints == PortConstraints.FIXED_RATIO) {
			// Fixed Ratio
			placeFixedRatioNodePorts(data.node);
		} else {
			// Free, Fixed Side, Fixed Order
			if (data.node.getProperty(LayoutOptions.HYPERNODE)
					|| ((data.node.getSize().x == 0) && (data.node.getSize().y == 0))) {

				placeHypernodePorts(data.node);
			} else {
				placeNodePorts(data);
			}
		}
	}

	/**
	 * Places the ports of a node assuming that the port constraints are set to
	 * fixed port positions. Ports still need to be placed, though, because the
	 * node may have been resized.
	 *
	 * @param node
	 *            the node whose ports to place.
	 */
	private void placeFixedPosNodePorts(final NodeAdapter<?> node) {
		final KVector nodeSize = node.getSize();

		for (final PortAdapter<?> port : node.getPorts()) {
			Float portOffset = port.getProperty(LayoutOptions.OFFSET);
			if (portOffset == null) {
				portOffset = 0f;
			}

			final KVector position = new KVector(port.getPosition());
			switch (port.getSide()) {
				case WEST:
					position.x = -port.getSize().x - portOffset;
					break;
				case EAST:
					position.x = nodeSize.x + portOffset;
					break;
				case NORTH:
					position.y = -port.getSize().y - portOffset;
					break;
				case SOUTH:
					position.y = nodeSize.y + portOffset;
					break;
			}
			port.setPosition(position);
		}
	}

	/**
	 * Places the ports of a node keeping the ratio between their position and
	 * the length of their respective side intact.
	 *
	 * @param node
	 *            the node whose ports to place.
	 */
	private void placeFixedRatioNodePorts(final NodeAdapter<?> node) {
		final KVector nodeSize = node.getSize();

		// Adjust port positions depending on port side. Eastern ports have to
		// have their x
		// coordinate set to the node's current width; the same goes for the y
		// coordinate of
		// southern ports
		for (final PortAdapter<?> port : node.getPorts()) {
			Float portOffset = port.getProperty(LayoutOptions.OFFSET);
			if (portOffset == null) {
				portOffset = 0f;
			}

			switch (port.getSide()) {
				case WEST:
					port.getPosition().y = nodeSize.y * port.getProperty(PORT_RATIO_OR_POSITION);
					port.getPosition().x = -port.getSize().x - portOffset;
					break;
				case EAST:
					port.getPosition().y = nodeSize.y * port.getProperty(PORT_RATIO_OR_POSITION);
					port.getPosition().x = nodeSize.x + portOffset;
					break;
				case NORTH:
					port.getPosition().x = nodeSize.x * port.getProperty(PORT_RATIO_OR_POSITION);
					port.getPosition().y = -port.getSize().y - portOffset;
					break;
				case SOUTH:
					port.getPosition().x = nodeSize.x * port.getProperty(PORT_RATIO_OR_POSITION);
					port.getPosition().y = nodeSize.y + portOffset;
					break;
			}
			switch (port.getSide()) {
				case WEST:
					port.getPosition().y = nodeSize.y * port.getProperty(PORT_RATIO_OR_POSITION);
					port.getPosition().x = -port.getSize().x - portOffset;
					break;
				case EAST:
					port.getPosition().y = nodeSize.y * port.getProperty(PORT_RATIO_OR_POSITION);
					port.getPosition().x = nodeSize.x + portOffset;
					break;
				case NORTH:
					port.getPosition().x = nodeSize.x * port.getProperty(PORT_RATIO_OR_POSITION);
					port.getPosition().y = -port.getSize().y - portOffset;
					break;
				case SOUTH:
					port.getPosition().x = nodeSize.x * port.getProperty(PORT_RATIO_OR_POSITION);
					port.getPosition().y = nodeSize.y + portOffset;
					break;
			}
		}
	}

	/**
	 * Places the ports of a node, assuming that the ports are not fixed in
	 * their position or ratio.
	 *
	 * @param data
	 *            the data containing the node whose ports to place.
	 */
	private void placeNodePorts(final NodeData data) {
		final KVector nodeSize = data.node.getSize();
		final boolean accountForLabels = data.node.getProperty(LayoutOptions.SIZE_CONSTRAINT)
				.contains(SizeConstraint.PORT_LABELS);

		// Let someone compute the port placement data we'll need
		final PortPlacementData placementData = computePortPlacementData(data);

		// Arrange the ports
		for (final PortAdapter<?> port : data.node.getPorts()) {
			Float portOffset = port.getProperty(LayoutOptions.OFFSET);
			if (portOffset == null) {
				portOffset = 0f;
			}
			final KVector portSize = port.getSize();
			final Margins portMargins = port.getMargin();

			final KVector position = new KVector(port.getPosition());
			switch (port.getSide()) {
				case WEST:
					position.x = -portSize.x - portOffset;
					position.y = placementData.westY - portSize.y
							- (accountForLabels ? portMargins.bottom : 0.0);
					placementData.westY -= placementData.westGapSize + portSize.y
							+ (accountForLabels ? portMargins.top + portMargins.bottom : 0.0);
					break;
				case EAST:
					position.x = nodeSize.x + portOffset;
					position.y = placementData.eastY + (accountForLabels ? portMargins.top : 0.0);
					placementData.eastY += placementData.eastGapSize + portSize.y
							+ (accountForLabels ? portMargins.top + portMargins.bottom : 0.0);
					break;
				case NORTH:
					position.x = placementData.northX + (accountForLabels ? portMargins.left : 0.0);
					position.y = -port.getSize().y - portOffset;
					placementData.northX += placementData.northGapSize + portSize.x
							+ (accountForLabels ? portMargins.left + portMargins.right : 0.0);
					break;
				case SOUTH:
					position.x = placementData.southX - portSize.x
							- (accountForLabels ? portMargins.right : 0.0);
					position.y = nodeSize.y + portOffset;
					placementData.southX -= placementData.southGapSize + portSize.x
							+ (accountForLabels ? portMargins.left + portMargins.right : 0.0);
					break;
			}
			port.setPosition(position);
		}
	}

	/**
	 * Computes the port placement data for the given node.
	 *
	 * @param nodeData
	 *            the data containing the node to compute the placement data
	 *            for.
	 * @return the port placement data.
	 */
	// CHECKSTYLEOFF MethodLength
	// There's no sensible point to separate, too much parameters would have to
	// be introduced.
	private PortPlacementData computePortPlacementData(final NodeData nodeData) {
		final PortPlacementData portData = new PortPlacementData();
		final KVector nodeSize = nodeData.node.getSize();

		// Get the port distribution from the node.
		PortAlignment portAlignment = nodeData.node.getProperty(LayoutOptions.PORT_ALIGNMENT);
		// Use JUSTIFIED as default.
		portAlignment = portAlignment == PortAlignment.UNDEFINED ? PortAlignment.JUSTIFIED
				: portAlignment;

		// For each side get the port distribution. If it's null, replace it
		// with the nodes policy.
		PortAlignment portAlignmentNorth = nodeData.node
				.getProperty(LayoutOptions.PORT_ALIGNMENT_NORTH);
		PortAlignment portAlignmentSouth = nodeData.node
				.getProperty(LayoutOptions.PORT_ALIGNMENT_SOUTH);
		PortAlignment portAlignmentWest = nodeData.node
				.getProperty(LayoutOptions.PORT_ALIGNMENT_WEST);
		PortAlignment portAlignmentEast = nodeData.node
				.getProperty(LayoutOptions.PORT_ALIGNMENT_EAST);
		portAlignmentNorth = portAlignmentNorth == PortAlignment.UNDEFINED ? portAlignment
				: portAlignmentNorth;
		portAlignmentSouth = portAlignmentSouth == PortAlignment.UNDEFINED ? portAlignment
				: portAlignmentSouth;
		portAlignmentWest = portAlignmentWest == PortAlignment.UNDEFINED ? portAlignment
				: portAlignmentWest;
		portAlignmentEast = portAlignmentEast == PortAlignment.UNDEFINED ? portAlignment
				: portAlignmentEast;

		// The way we calculate everything depends on whether any additional
		// port space is specified
		Margins additionalPortSpace = nodeData.node
				.getProperty(LayoutOptions.ADDITIONAL_PORT_SPACE);

		if (additionalPortSpace == null) {
			// No additional port spacing set, so we set it to port spacing.
			additionalPortSpace = new Margins(nodeData.portSpacing, nodeData.portSpacing,
					nodeData.portSpacing, nodeData.portSpacing);
		}
		// Calculate how many gaps we have between ports (this is usually one
		// less than the number
		// of ports we have, but if it's just a single port, we have two gaps
		// that surround it)
		portData.northGaps = nodeData.northPortsCount == 1 ? 2 : nodeData.northPortsCount - 1;
		portData.southGaps = nodeData.southPortsCount == 1 ? 2 : nodeData.southPortsCount - 1;
		portData.westGaps = nodeData.westPortsCount == 1 ? 2 : nodeData.westPortsCount - 1;
		portData.eastGaps = nodeData.eastPortsCount == 1 ? 2 : nodeData.eastPortsCount - 1;

		// Calculate how much space on each side may actually be used by ports
		final double usableWidth = nodeSize.x - additionalPortSpace.left
				- additionalPortSpace.right;
		final double usableHeight = nodeSize.y - additionalPortSpace.top
				- additionalPortSpace.bottom;

		// Compute the space to be left between the ports and the coordinate of
		// the first port on
		// each side.
		// Note:
		// If the size constraints of this node are empty, the height and width
		// of the ports on each
		// side are zero. That is intentional: if this wasn't the case, bad
		// things would happen if
		// the ports would actually need more size than the node at its current
		// (unchanged) size
		// would be able to provide.

		// NORTH
		if (portAlignmentNorth == PortAlignment.JUSTIFIED) {
			portData.northGapSize = (usableWidth - nodeData.northPortsWidth) / portData.northGaps;
			portData.northX = additionalPortSpace.left
					+ (nodeData.northPortsCount == 1 ? portData.northGapSize : 0);
		} else {
			portData.northGapSize = nodeData.portSpacing;
			// Space occupied by all ports (including the in between gaps).
			final double usedPortSpace = nodeData.northPortsWidth
					+ (portData.northGapSize * (nodeData.northPortsCount - 1));
			switch (portAlignmentNorth) {
				case BEGIN:
					// Start at leftmost position, additionalSpace from the
					// edge.
					portData.northX = additionalPortSpace.left;
					break;
				case CENTER:
					// centered inside the usableWith
					portData.northX = additionalPortSpace.left
							+ ((usableWidth - usedPortSpace) / 2.0);
					break;
				case END:
					// Startposition is as far from the right edge as the ports'
					// used space plus
					// additionalSpace.
					portData.northX = nodeSize.x - usedPortSpace - additionalPortSpace.right;
					break;
			}
		}

		// SOUTH
		if (portAlignmentSouth == PortAlignment.JUSTIFIED) {
			portData.southGapSize = (usableWidth - nodeData.southPortsWidth) / portData.southGaps;
			portData.southX = nodeSize.x - additionalPortSpace.right
					- (nodeData.southPortsCount == 1 ? portData.southGapSize : 0);
		} else {
			portData.southGapSize = nodeData.portSpacing;
			// Space occupied by all ports (including the in between gaps).
			final double usedPortSpace = nodeData.southPortsWidth
					+ (portData.southGapSize * (nodeData.southPortsCount - 1));
			switch (portAlignmentSouth) {
				case BEGIN:
					// Startposition is as far from the right edge as the ports'
					// used space plus
					// additionalSpace.
					portData.southX = usedPortSpace + additionalPortSpace.left;
					break;
				case CENTER:
					// centered inside the usableWith (starting at the right)
					portData.southX = nodeSize.x - ((usableWidth - usedPortSpace) / 2.0)
							- additionalPortSpace.right;
					break;
				case END:
					// Start at rightmost position, additionalSpace from the
					// edge.
					portData.southX = nodeSize.x - additionalPortSpace.right;
					break;
			}
		}

		// WEST
		if (portAlignmentWest == PortAlignment.JUSTIFIED) {
			portData.westGapSize = (usableHeight - nodeData.westPortsHeight) / portData.westGaps;
			portData.westY = nodeSize.y - additionalPortSpace.bottom
					- (nodeData.westPortsCount == 1 ? portData.westGapSize : 0);
		} else {
			portData.westGapSize = nodeData.portSpacing;
			// Space occupied by all ports (including the in between gaps).
			final double usedPortSpace = nodeData.westPortsHeight
					+ (portData.westGapSize * (nodeData.westPortsCount - 1));
			switch (portAlignmentWest) {
				case BEGIN:
					// Startposition is as far from the top edge as the ports'
					// used space plus
					// additionalSpace.
					portData.westY = usedPortSpace + additionalPortSpace.top;
					break;
				case CENTER:
					// centered inside the usableWith (starting at the bottom)
					portData.westY = nodeSize.y - ((usableHeight - usedPortSpace) / 2.0)
							- additionalPortSpace.bottom;
					break;
				case END:
					// Start at bottommost position, additionalSpace from the
					// edge.
					portData.westY = nodeSize.y - additionalPortSpace.bottom;
					break;
			}
		}

		// EAST
		if (portAlignmentEast == PortAlignment.JUSTIFIED) {
			portData.eastGapSize = (usableHeight - nodeData.eastPortsHeight) / portData.eastGaps;
			portData.eastY = additionalPortSpace.top
					+ (nodeData.eastPortsCount == 1 ? portData.eastGapSize : 0);
		} else {
			portData.eastGapSize = nodeData.portSpacing;
			// Space occupied by all ports (including the in between gaps).
			final double usedPortSpace = nodeData.eastPortsHeight
					+ (portData.eastGapSize * (nodeData.eastPortsCount - 1));
			switch (portAlignmentEast) {
				case BEGIN:
					// Start at topmost position, additionalSpace from the edge.
					portData.eastY = additionalPortSpace.top;
					break;
				case CENTER:
					// centered inside the usableWith
					portData.eastY = additionalPortSpace.top
							+ ((usableHeight - usedPortSpace) / 2.0);
					break;
				case END:
					// Start position is as far from the bottom edge as the
					// ports' used space plus
					// additionalSpace.
					portData.eastY = nodeSize.y - usedPortSpace - additionalPortSpace.bottom;
					break;
			}
		}
		return portData;
	}

	// CHECKSTYLEON MethodLength

	/**
	 * Places the ports of a hypernode.
	 *
	 * @param node
	 *            the hypernode whose ports to place.
	 */
	private void placeHypernodePorts(final NodeAdapter<?> node) {
		for (final PortAdapter<?> port : node.getPorts()) {
			final KVector position = new KVector(port.getPosition());
			switch (port.getSide()) {
				case WEST:
					position.x = 0;
					position.y = node.getSize().y / 2;
					break;
				case EAST:
					position.x = node.getSize().x;
					position.y = node.getSize().y / 2;
					break;
				case NORTH:
					position.x = node.getSize().x / 2;
					position.y = 0;
					break;
				case SOUTH:
					position.x = node.getSize().x / 2;
					position.y = node.getSize().y;
					break;
			}
			port.setPosition(position);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// PLACING NODE LABELS

	/**
	 * Calculates the position of the node's label groups and places the labels.
	 *
	 * @param data
	 *            the data containing the node whose labels to place.
	 */
	private void placeNodeLabels(final NodeData data) {
		// Check if there are any node labels
		if (!data.node.getLabels().iterator().hasNext()) {
			return;
		}

		computeLabelGroupPositions(data);

		doPlaceNodeLabels(data);
	}

	/**
	 * Computes the top left position of each label group.
	 * 
	 * @param data
	 *            the data containing the node whose labels to place.
	 */
	private void computeLabelGroupPositions(final NodeData data) {
		// TODO Outside label placement doesn't take ports into account yet.

		// For each present location, calculate the position of the top left
		// corner of the label group
		for (final Entry<Location, LabelGroup> entry : data.labelGroupsBoundingBoxes.entrySet()) {
			final Rectangle boundingBox = entry.getValue();
			switch (entry.getKey()) {
				case OUT_T_L:
					boundingBox.x = 0;
					boundingBox.y = -(boundingBox.height + data.labelSpacing);
					break;
				case OUT_T_C:
					boundingBox.x = (data.node.getSize().x - boundingBox.width) / 2.0;
					boundingBox.y = -(boundingBox.height + data.labelSpacing);
					break;
				case OUT_T_R:
					boundingBox.x = data.node.getSize().x - boundingBox.width;
					boundingBox.y = -(boundingBox.height + data.labelSpacing);
					break;
				case OUT_B_L:
					boundingBox.x = 0;
					boundingBox.y = data.node.getSize().y + data.labelSpacing;
					break;
				case OUT_B_C:
					boundingBox.x = (data.node.getSize().x - boundingBox.width) / 2.0;
					boundingBox.y = data.node.getSize().y + data.labelSpacing;
					break;
				case OUT_B_R:
					boundingBox.x = data.node.getSize().x - boundingBox.width;
					boundingBox.y = data.node.getSize().y + data.labelSpacing;
					break;
				case OUT_L_T:
					boundingBox.x = -(boundingBox.width + data.labelSpacing);
					boundingBox.y = 0;
					break;
				case OUT_L_C:
					boundingBox.x = -(boundingBox.width + data.labelSpacing);
					boundingBox.y = (data.node.getSize().y - boundingBox.height) / 2.0;
					break;
				case OUT_L_B:
					boundingBox.x = -(boundingBox.width + data.labelSpacing);
					boundingBox.y = data.node.getSize().y - boundingBox.height;
					break;
				case OUT_R_T:
					boundingBox.x = data.node.getSize().x + data.labelSpacing;
					boundingBox.y = 0;
					break;
				case OUT_R_C:
					boundingBox.x = data.node.getSize().x + data.labelSpacing;
					boundingBox.y = (data.node.getSize().y - boundingBox.height) / 2.0;
					break;
				case OUT_R_B:
					boundingBox.x = data.node.getSize().x + data.labelSpacing;
					boundingBox.y = data.node.getSize().y - boundingBox.height;
					break;
				case IN_T_L:
					boundingBox.x = data.requiredPortLabelSpace.left + data.labelSpacing;
					boundingBox.y = data.requiredPortLabelSpace.top + data.labelSpacing;
					break;
				case IN_T_C:
					boundingBox.x = (data.node.getSize().x - boundingBox.width) / 2.0;
					boundingBox.y = data.requiredPortLabelSpace.top + data.labelSpacing;
					break;
				case IN_T_R:
					boundingBox.x = data.node.getSize().x - data.requiredPortLabelSpace.right
							- boundingBox.width - data.labelSpacing;
					boundingBox.y = data.requiredPortLabelSpace.top + data.labelSpacing;
					break;
				case IN_C_L:
					boundingBox.x = data.requiredPortLabelSpace.left + data.labelSpacing;
					boundingBox.y = (data.node.getSize().y - boundingBox.height) / 2.0;
					break;
				case IN_C_C:
					boundingBox.x = (data.node.getSize().x - boundingBox.width) / 2.0;
					boundingBox.y = (data.node.getSize().y - boundingBox.height) / 2.0;
					break;
				case IN_C_R:
					boundingBox.x = data.node.getSize().x - data.requiredPortLabelSpace.right
							- boundingBox.width - data.labelSpacing;
					boundingBox.y = (data.node.getSize().y - boundingBox.height) / 2.0;
					break;
				case IN_B_L:
					boundingBox.x = data.requiredPortLabelSpace.left + data.labelSpacing;
					boundingBox.y = data.node.getSize().y - data.requiredPortLabelSpace.bottom
							- boundingBox.height - data.labelSpacing;
					break;
				case IN_B_C:
					boundingBox.x = (data.node.getSize().x - boundingBox.width) / 2.0;
					boundingBox.y = data.node.getSize().y - data.requiredPortLabelSpace.bottom
							- boundingBox.height - data.labelSpacing;
					break;
				case IN_B_R:
					boundingBox.x = data.node.getSize().x - data.requiredPortLabelSpace.right
							- boundingBox.width - data.labelSpacing;
					boundingBox.y = data.node.getSize().y - data.requiredPortLabelSpace.bottom
							- boundingBox.height - data.labelSpacing;
					break;
			}
		}
	}

	/**
	 * Places the given node's labels in a vertical stack, starting at the given
	 * position.
	 *
	 * @param data
	 *            the data containing the node whose labels are to be placed.
	 */
	private void doPlaceNodeLabels(final NodeData data) {

		// Place all labels
		for (final LabelAdapter<?> label : data.node.getLabels()) {
			final KVector position = new KVector(label.getPosition());
			final Location location = Location.values()[label.getVolatileId()];
			final LabelGroup boundingBox = data.labelGroupsBoundingBoxes.get(location);

			// Set y coordinate
			position.y = boundingBox.y + boundingBox.nextLabelYPos;

			// The x coordinate depends on the text alignment
			if (location.horizontalAlignment == TextAlignment.LEFT) {
				position.x = boundingBox.x;
			} else if (location.horizontalAlignment == TextAlignment.CENTER) {
				position.x = boundingBox.x + ((boundingBox.width - label.getSize().x) / 2.0);
			} else if (location.horizontalAlignment == TextAlignment.RIGHT) {
				position.x = (boundingBox.x + boundingBox.width) - label.getSize().x;
			}

			// Apply new position
			label.setPosition(position);

			// Update next y coordinate
			boundingBox.nextLabelYPos += label.getSize().y + data.labelSpacing;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// CONTEXT UTILITIES

	/**
	 * Information holder to provide context information for the different
	 * phases of the algorithm. The information are usually computed by some
	 * phase to be made available to later phases.
	 *
	 * @author csp
	 */
	private static final class NodeData {

		/**
		 * The currently processed node.
		 */
		private final NodeAdapter<?> node;

		/*
		 * Spacing around labels.
		 */
		private double labelSpacing;

		/*
		 * Spacing around ports.
		 */
		private double portSpacing;

		/**
		 * Node insets required by port labels inside the node. This is always
		 * set, but not always taken into account to calculate the node size.
		 */
		private final Insets requiredPortLabelSpace = new Insets();

		/**
		 * Node insets required by node labels placed inside the node. This is
		 * always set, but not always taken into account to calculate the node
		 * size.
		 */
		private final Insets requiredNodeLabelSpace = new Insets();

		/**
		 * Number of ports on the western side. Only used if port constraints
		 * are not {@link PortConstraints#FIXED_RATIO} or
		 * {@link PortConstraints#FIXED_POS}.
		 */
		private int westPortsCount = 0;

		/**
		 * Height of the ports on the western side. If port labels are accounted
		 * for, the height includes the relevant port margins too. Only used if
		 * port constraints are not {@link PortConstraints#FIXED_RATIO} or
		 * {@link PortConstraints#FIXED_POS}.
		 */
		private double westPortsHeight = 0.0;

		/**
		 * Number of ports on the eastern side.Only used if port constraints are
		 * not {@link PortConstraints#FIXED_RATIO} or
		 * {@link PortConstraints#FIXED_POS}.
		 */
		private int eastPortsCount = 0;

		/**
		 * Height of the ports on the eastern side. If port labels are accounted
		 * for, the height includes the relevant port margins too. Only used if
		 * port constraints are not {@link PortConstraints#FIXED_RATIO} or
		 * {@link PortConstraints#FIXED_POS}.
		 */
		private double eastPortsHeight = 0.0;

		/**
		 * Number of ports on the northern side.Only used if port constraints
		 * are not {@link PortConstraints#FIXED_RATIO} or
		 * {@link PortConstraints#FIXED_POS}.
		 */
		private int northPortsCount = 0;

		/**
		 * Width of the ports on the northern side. If port labels are accounted
		 * for, the height includes the relevant port margins too. Only used if
		 * port constraints are not {@link PortConstraints#FIXED_RATIO} or
		 * {@link PortConstraints#FIXED_POS}.
		 */
		private double northPortsWidth = 0.0;

		/**
		 * Number of ports on the southern side.Only used if port constraints
		 * are not {@link PortConstraints#FIXED_RATIO} or
		 * {@link PortConstraints#FIXED_POS}.
		 */
		private int southPortsCount = 0;

		/**
		 * Width of the ports on the southern side. If port labels are accounted
		 * for, the height includes the relevant port margins too. Only used if
		 * port constraints are not {@link PortConstraints#FIXED_RATIO} or
		 * {@link PortConstraints#FIXED_POS}.
		 */
		private double southPortsWidth = 0.0;

		/**
		 * Contains the size and position of the corresponding label group for
		 * each element of {@link Location}.
		 */
		private final Map<Location, LabelGroup> labelGroupsBoundingBoxes = new EnumMap<Location, LabelGroup>(
				Location.class);

		/**
		 * Create a new information holder with default values and the given,
		 * currently processed node.
		 *
		 * @param node
		 *            the node currently processed.
		 */
		private NodeData(final NodeAdapter<?> node) {
			this.node = node;
		}

		/**
		 * Returns the bounding box of all node labels placed at the specified
		 * location. If there is no bounding box for the location yet, a new one
		 * is added and returned.
		 *
		 * @param location
		 *            the location for which to retrieve the bounding box.
		 * @return the corresponding bounding box.
		 */
		public Rectangle retrieveLabelGroupsBoundingBox(final Location location) {
			if (!labelGroupsBoundingBoxes.containsKey(location)) {
				final LabelGroup boundingBox = new LabelGroup();
				labelGroupsBoundingBoxes.put(location, boundingBox);
				return boundingBox;
			} else {
				return labelGroupsBoundingBoxes.get(location);
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// LABEL PLACEMENT UTILITIES

	/**
	 * Enumeration over all possible label placements.
	 *
	 * @see NodeLabelPlacement
	 */
	private static enum Location {
		OUT_T_L(ImmutableList.of(EnumSet.of(NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.V_TOP,
				NodeLabelPlacement.H_LEFT)), TextAlignment.LEFT), OUT_T_C(ImmutableList.of(EnumSet
				.of(NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.V_TOP,
						NodeLabelPlacement.H_CENTER), EnumSet.of(NodeLabelPlacement.OUTSIDE,
				NodeLabelPlacement.V_TOP, NodeLabelPlacement.H_CENTER,
				NodeLabelPlacement.H_PRIORITY)), TextAlignment.CENTER), OUT_T_R(ImmutableList
				.of(EnumSet.of(NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.V_TOP,
						NodeLabelPlacement.H_RIGHT)), TextAlignment.RIGHT), OUT_B_L(ImmutableList
				.of(EnumSet.of(NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.V_BOTTOM,
						NodeLabelPlacement.H_LEFT)), TextAlignment.LEFT), OUT_B_C(ImmutableList.of(
				EnumSet.of(NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.V_BOTTOM,
						NodeLabelPlacement.H_CENTER), EnumSet.of(NodeLabelPlacement.OUTSIDE,
						NodeLabelPlacement.V_BOTTOM, NodeLabelPlacement.H_CENTER,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.CENTER), OUT_B_R(
				ImmutableList.of(EnumSet.of(NodeLabelPlacement.OUTSIDE,
						NodeLabelPlacement.V_BOTTOM, NodeLabelPlacement.H_RIGHT)),
				TextAlignment.RIGHT), OUT_L_T(ImmutableList.of(EnumSet.of(
				NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.H_LEFT, NodeLabelPlacement.V_TOP,
				NodeLabelPlacement.H_PRIORITY)), TextAlignment.RIGHT), OUT_L_C(ImmutableList.of(
				EnumSet.of(NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.H_LEFT,
						NodeLabelPlacement.V_CENTER), EnumSet.of(NodeLabelPlacement.OUTSIDE,
						NodeLabelPlacement.H_LEFT, NodeLabelPlacement.V_CENTER,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.RIGHT), OUT_L_B(
				ImmutableList.of(EnumSet.of(NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.H_LEFT,
						NodeLabelPlacement.V_BOTTOM, NodeLabelPlacement.H_PRIORITY)),
				TextAlignment.RIGHT), OUT_R_T(ImmutableList.of(EnumSet.of(
				NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.H_RIGHT, NodeLabelPlacement.V_TOP,
				NodeLabelPlacement.H_PRIORITY)), TextAlignment.LEFT), OUT_R_C(ImmutableList.of(
				EnumSet.of(NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.H_RIGHT,
						NodeLabelPlacement.V_CENTER), EnumSet.of(NodeLabelPlacement.OUTSIDE,
						NodeLabelPlacement.H_RIGHT, NodeLabelPlacement.V_CENTER,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.LEFT), OUT_R_B(ImmutableList
				.of(EnumSet.of(NodeLabelPlacement.OUTSIDE, NodeLabelPlacement.H_RIGHT,
						NodeLabelPlacement.V_BOTTOM, NodeLabelPlacement.H_PRIORITY)),
				TextAlignment.LEFT), IN_T_L(ImmutableList.of(EnumSet.of(NodeLabelPlacement.INSIDE,
				NodeLabelPlacement.V_TOP, NodeLabelPlacement.H_LEFT), EnumSet.of(
				NodeLabelPlacement.INSIDE, NodeLabelPlacement.V_TOP, NodeLabelPlacement.H_LEFT,
				NodeLabelPlacement.H_PRIORITY)), TextAlignment.LEFT), IN_T_C(ImmutableList.of(
				EnumSet.of(NodeLabelPlacement.INSIDE, NodeLabelPlacement.V_TOP,
						NodeLabelPlacement.H_CENTER), EnumSet.of(NodeLabelPlacement.INSIDE,
						NodeLabelPlacement.V_TOP, NodeLabelPlacement.H_CENTER,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.CENTER), IN_T_R(
				ImmutableList.of(EnumSet.of(NodeLabelPlacement.INSIDE, NodeLabelPlacement.V_TOP,
						NodeLabelPlacement.H_RIGHT), EnumSet.of(NodeLabelPlacement.INSIDE,
						NodeLabelPlacement.V_TOP, NodeLabelPlacement.H_RIGHT,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.RIGHT), IN_C_L(ImmutableList
				.of(EnumSet.of(NodeLabelPlacement.INSIDE, NodeLabelPlacement.V_CENTER,
						NodeLabelPlacement.H_LEFT), EnumSet.of(NodeLabelPlacement.INSIDE,
						NodeLabelPlacement.V_CENTER, NodeLabelPlacement.H_LEFT,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.LEFT), IN_C_C(ImmutableList
				.of(EnumSet.of(NodeLabelPlacement.INSIDE, NodeLabelPlacement.V_CENTER,
						NodeLabelPlacement.H_CENTER), EnumSet.of(NodeLabelPlacement.INSIDE,
						NodeLabelPlacement.V_CENTER, NodeLabelPlacement.H_CENTER,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.CENTER), IN_C_R(
				ImmutableList.of(EnumSet.of(NodeLabelPlacement.INSIDE, NodeLabelPlacement.V_CENTER,
						NodeLabelPlacement.H_RIGHT), EnumSet.of(NodeLabelPlacement.INSIDE,
						NodeLabelPlacement.V_CENTER, NodeLabelPlacement.H_RIGHT,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.RIGHT), IN_B_L(ImmutableList
				.of(EnumSet.of(NodeLabelPlacement.INSIDE, NodeLabelPlacement.V_BOTTOM,
						NodeLabelPlacement.H_LEFT), EnumSet.of(NodeLabelPlacement.INSIDE,
						NodeLabelPlacement.V_BOTTOM, NodeLabelPlacement.H_LEFT,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.LEFT), IN_B_C(ImmutableList
				.of(EnumSet.of(NodeLabelPlacement.INSIDE, NodeLabelPlacement.V_BOTTOM,
						NodeLabelPlacement.H_CENTER), EnumSet.of(NodeLabelPlacement.INSIDE,
						NodeLabelPlacement.V_BOTTOM, NodeLabelPlacement.H_CENTER,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.CENTER), IN_B_R(
				ImmutableList.of(EnumSet.of(NodeLabelPlacement.INSIDE, NodeLabelPlacement.V_BOTTOM,
						NodeLabelPlacement.H_RIGHT), EnumSet.of(NodeLabelPlacement.INSIDE,
						NodeLabelPlacement.V_BOTTOM, NodeLabelPlacement.H_RIGHT,
						NodeLabelPlacement.H_PRIORITY)), TextAlignment.RIGHT), IGNORED(
				ImmutableList.<EnumSet<NodeLabelPlacement>> of(), null);

		/* The corresponding placements to this location. */
		private final List<? extends Set<NodeLabelPlacement>> assignedPlacements;
		/* The horizontal text alignment for this location. */
		private final TextAlignment horizontalAlignment;

		/**
		 * Creates a new location with valid {@link NodeLabelPlacement} for this
		 * location.
		 *
		 * @param assignedPlacements
		 *            the valid {@link NodeLabelPlacement}s for this location.
		 * @param horizontalAlignment
		 *            the horizontal text alignment for this location.
		 */
		private Location(final List<? extends Set<NodeLabelPlacement>> assignedPlacements,
				final TextAlignment horizontalAlignment) {
			this.assignedPlacements = assignedPlacements;
			this.horizontalAlignment = horizontalAlignment;
		}

		/**
		 * Converts a set of {@link NodeLabelPlacement}s to a {@link Location}
		 * if possible.
		 * 
		 * @param labelPlacement
		 *            the set of placements to convert.
		 * @return the corresponding location. If no valid combination is given,
		 *         {@code Location.IGNORED} is returned.
		 */
		private static Location fromNodeLabelPlacement(
				final EnumSet<NodeLabelPlacement> labelPlacement) {
			for (final Location location : Location.values()) {
				if (location.assignedPlacements.contains(labelPlacement)) {
					return location;
				}
			}
			return Location.IGNORED;
		}

	}

	/**
	 * Enumeration for horizontal alignment of text.
	 *
	 * @author csp
	 */
	public static enum TextAlignment {
		/** Text is left-aligned. */
		LEFT,
		/** Text is centered. */
		CENTER,
		/** Text is right-aligned. */
		RIGHT;
	}

	/**
	 * Information wrapper for size and position of a group of labels. Basically
	 * a {@link Rectangle} with an additional field for the current y-offset
	 * inside the group, used while placing the labels.
	 * 
	 * @author csp
	 */
	private static final class LabelGroup extends Rectangle {
		private double nextLabelYPos = 0;
	}

	// /////////////////////////////////////////////////////////////////////////////
	// PORT PLACEMENT UTILITIES

	/**
	 * Holds information necessary to place the ports on each side. Since a lot
	 * of information are necessary, we define a small data holder class just
	 * for them. Not all of the fields specified here are always required.
	 *
	 * @author cds
	 */
	private static final class PortPlacementData {
		// The number of gaps between the ports (this is usually one less than
		// the number of ports
		// we have, but if it's just a single port, we have two gaps that
		// surround it)
		private double westGaps;
		private double eastGaps;
		private double northGaps;
		private double southGaps;

		// The size of each gap on the different sides
		private double westGapSize;
		private double eastGapSize;
		private double northGapSize;
		private double southGapSize;

		// The position of the next port on each side
		private double westY;
		private double eastY;
		private double northX;
		private double southX;
	}
}
