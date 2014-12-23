/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2012 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.p4nodes;

import java.io.Serializable;
import java.util.*;

import com.google.common.collect.*;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.*;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.*;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * This algorithm is an implementation for solving the node placement problem
 * which is posed in phase 4 of the KLay Layered algorithm. Inspired by:
 * <ul>
 * <li>Ulrik Brandes and Boris K&ouml;pf, Fast and simple horizontal coordinate
 * assignment. In <i>Proceedings of the 9th International Symposium on Graph
 * Drawing (GD'01)</i>, LNCS vol. 2265, pp. 33-36, Springer, 2002.</li>
 * </ul>
 *
 * <p>
 * The original algorithm was extended to be able to cope with ports, node
 * sizes, and node margins, and was made more stable in general. The algorithm
 * is structured in five steps, which include two new steps which were not
 * included in the original algorithm by Brandes and Koepf. The middle three
 * steps are executed four times, traversing the graph in all combinations of
 * TOP or BOTTOM and LEFT or RIGHT.
 * </p>
 *
 * <p>
 * Although we have, in KLay Layered, the general idea of layouting from left to
 * right and transforming in the desired direction later, we decided to keep the
 * terminology from the original algorithm which thinks of a layout from top to
 * bottom. When placing coordinates, we have to differ from the original
 * algorithm, since node placement in KLay Layered has to assign y-coordinates
 * and not x-coordinates.
 * </p>
 *
 * <p>
 * The variable naming in this code is mostly chosen such that it matches the
 * original variable names in the paper. There are methods that divert from this
 * convention, though, to achieve better code readability.
 * </p>
 *
 * <h4>The algorithm:</h4>
 *
 * <p>
 * The first step checks the graphs' edges and marks short edges which cross
 * long edges (called type 1 conflict). The algorithm indents to draw long edges
 * as straight as possible, thus trying to solve the marked conflicts in a way
 * which keep the long edge straight.
 * </p>
 *
 * <p>=
 * =========== TOP, BOTTOM x LEFT, RIGHT ============
 * </p>
 *
 * <p>
 * The second step traverses the graph in the given directions and tries to
 * group connected nodes into (horizontal) blocks. These blocks, respectively
 * the contained nodes, will be drawn straight when the algorithm is finished.
 * Here, type 1 conflicts are resolved, so that the dummy nodes of a long edge
 * share the same block if possible, such that the long edge is drawn
 * straightly.
 * </p>
 *
 * <p>
 * The third step contains the addition of node size and port positions to the
 * original algorithm. Each block is investigated from TOP to BOTTOM. Nodes are
 * moved inside the blocks, such that the port of the edge going to the next
 * node is on the same level as that next node. Furthermore, the size of the
 * block is calculated, regarding node sizes and new space needed due to node
 * movement.
 * </p>
 *
 * <p>
 * In the fourth step, actual y-coordinates are determined. The blocks are
 * placed, start block and direction determined by the directions of the current
 * iteration. It is tried to place the blocks as compact as possible by grouping
 * blocks.
 * </p>
 *
 * <p>=
 * ====================== END =======================
 * </p>
 *
 * <p>
 * The action of the last step depends on a layout option. If "Less Edge Bends"
 * is set to true, one of the four calculated layouts is selected and applied,
 * choosing the layout which uses the least space. If it is false, a balanced
 * layout is chosen by calculating a median layout of all four layouts.
 * </p>
 *
 * <p>
 * In rare cases, it is possible that one or more layouts is not correct, e.g.
 * having nodes which overlap each other or violating the layer ordering
 * constraint. If the algorithm detects that, the respective layout is discarded
 * and another one is chosen.
 * </p>
 *
 * <dl>
 * <dt>Preconditions:</dt>
 * <dd>The graph has a proper layering with optimized nodes ordering</dd>
 * <dd>Ports are properly arranged</dd>
 * <dt>Postconditions:</dt>
 * <dd>Each node is assigned a vertical coordinate such that no two nodes
 * overlap</dd>
 * <dd>The size of each layer is set according to the area occupied by its nodes
 * </dd>
 * <dd>The height of the graph is set to the maximal layer height</dd>
 * </dl>
 *
 * @author jjc
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating yellow 2012-08-10 chsch grh KI-19
 */
public final class BKNodePlacer implements ILayoutPhase {

	/** Additional processor dependencies for graphs with hierarchical ports. */
	private static final IntermediateProcessingConfiguration HIERARCHY_PROCESSING_ADDITIONS = IntermediateProcessingConfiguration
			.createEmpty().addBeforePhase5(
					IntermediateProcessorStrategy.HIERARCHICAL_PORT_POSITION_PROCESSOR);

	/**
	 * In the compaction step, nodes connected with north south dummies are
	 * compacted in a way which doesn't leave enough space for e.g., arrowheads.
	 * Thus, a small offset is added to give north south dummies enough space.
	 */

	/** List of edges involved in type 1 conflicts (see above). */
	private final List<LEdge> markedEdges = new LinkedList<LEdge>();
	/** Basic spacing between nodes, determined by layout options. */
	private float normalSpacing;
	/** Spacing between dummy nodes, determined by layout options. */
	private float smallSpacing;
	/** Spacing between external ports, determined by layout options. */
	private float externalPortSpacing;
	/** Flag which switches debug output of the algorithm on or off. */
	private boolean debugMode = false;
	/** Whether to produce a balanced layout or not. */
	private boolean produceBalancedLayout = false;

	/**
	 * During block placement, the y position where the next block should be
	 * placed initially.
	 */

	/**
	 * {@inheritDoc}
	 */
	public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
			final LGraph graph) {

		if (graph.getProperty(InternalProperties.GRAPH_PROPERTIES).contains(
				GraphProperties.EXTERNAL_PORTS)) {
			return HIERARCHY_PROCESSING_ADDITIONS;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
		monitor.begin("Brandes & Koepf node placement", 1);

		// Determine overall node count for an optimal initialization of maps.
		int nodeCount = 0;
		for (final Layer layer : layeredGraph) {
			nodeCount += layer.getNodes().size();
		}

		// Initialize four layouts which result from the two possible directions
		// respectively.
		final BKAlignedLayout lefttop = new BKAlignedLayout(nodeCount, VDirection.LEFT,
				HDirection.TOP);
		final BKAlignedLayout righttop = new BKAlignedLayout(nodeCount, VDirection.RIGHT,
				HDirection.TOP);
		final BKAlignedLayout leftbottom = new BKAlignedLayout(nodeCount, VDirection.LEFT,
				HDirection.BOTTOM);
		final BKAlignedLayout rightbottom = new BKAlignedLayout(nodeCount, VDirection.RIGHT,
				HDirection.BOTTOM);

		// Initialize spacing value from layout options.
		normalSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING)
				* layeredGraph.getProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR);
		smallSpacing = normalSpacing * layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);
		externalPortSpacing = layeredGraph.getProperty(Properties.PORT_SPACING);

		// Regard possible other layout options.
		debugMode = layeredGraph.getProperty(Properties.DEBUG_MODE);
		produceBalancedLayout = layeredGraph.getProperty(Properties.FIXED_ALIGNMENT) == FixedAlignment.BALANCED;

		// Phase which marks type 1 conflicts, no difference between the
		// directions so only
		// one run is required.
		markConflicts(layeredGraph);

		// Phase which determines the nodes' memberships in blocks. This happens
		// in four different
		// ways, either from processing the nodes from the first layer to the
		// last or vice versa.
		verticalAlignment(layeredGraph, lefttop);
		verticalAlignment(layeredGraph, righttop);
		verticalAlignment(layeredGraph, leftbottom);
		verticalAlignment(layeredGraph, rightbottom);

		// Additional phase which is not included in the original Brandes-Koepf
		// Algorithm.
		// It makes sure that the connected ports within a block are aligned to
		// avoid unnecessary
		// bend points. Also, the required size of each block is determined.
		insideBlockShift(layeredGraph, lefttop);
		insideBlockShift(layeredGraph, righttop);
		insideBlockShift(layeredGraph, leftbottom);
		insideBlockShift(layeredGraph, rightbottom);

		// This phase determines the y coordinates of the blocks and thus the
		// vertical coordinates
		// of all nodes.
		horizontalCompaction(layeredGraph, lefttop);
		horizontalCompaction(layeredGraph, righttop);
		horizontalCompaction(layeredGraph, leftbottom);
		horizontalCompaction(layeredGraph, rightbottom);

		// Debug output
		if (debugMode) {
			System.out.println("lefttop size is " + lefttop.layoutSize());
			System.out.println("righttop size is " + righttop.layoutSize());
			System.out.println("leftbottom size is " + leftbottom.layoutSize());
			System.out.println("rightbottom size is " + rightbottom.layoutSize());
		}

		// Choose a layout from the four calculated layouts. Layouts that
		// contain errors are skipped.
		// The layout with the smallest size is selected. If more than one
		// smallest layout exists,
		// the first one of the competing layouts is selected.
		BKAlignedLayout chosenLayout = null;
		final LinkedList<BKAlignedLayout> layouts = new LinkedList<BKAlignedLayout>();
		switch (layeredGraph.getProperty(Properties.FIXED_ALIGNMENT)) {
			case LEFTDOWN:
				layouts.add(lefttop);
				break;
			case LEFTUP:
				layouts.add(leftbottom);
				break;
			case RIGHTDOWN:
				layouts.add(righttop);
				break;
			case RIGHTUP:
				layouts.add(rightbottom);
				break;
			default:
				layouts.add(lefttop);
				layouts.add(righttop);
				layouts.add(leftbottom);
				layouts.add(rightbottom);
		}

		BKAlignedLayout balanced = new BKAlignedLayout(nodeCount, null, null);

		// If layout options chose to use the balanced layout, it is calculated
		// and added here.
		// If it is broken for any reason, one of the four other layouts is
		// selected by the
		// given criteria.
		if (produceBalancedLayout) {
			balanced = createBalancedLayout(layouts, nodeCount);
			chosenLayout = balanced;
		}

		// Since it is possible that the balanced layout violates ordering
		// constraints, this cannot
		// simply be an else case to the previous if statement
		if (!produceBalancedLayout || !checkOrderConstraint(layeredGraph, balanced)) {
			chosenLayout = null;
			for (final BKAlignedLayout bal : layouts) {
				if (checkOrderConstraint(layeredGraph, bal)) {
					if ((chosenLayout == null) || (chosenLayout.layoutSize() > bal.layoutSize())) {
						chosenLayout = bal;
					}
				}
			}
		}

		// If no layout is correct (which should never happen but is not
		// strictly impossible),
		// the lefttop layout is chosen by default.
		if (chosenLayout == null) {
			chosenLayout = lefttop;
		}

		// Apply calculated positions to nodes.
		for (final Layer layer : layeredGraph.getLayers()) {
			for (final LNode node : layer.getNodes()) {
				node.getPosition().y = chosenLayout.y.get(node) + chosenLayout.innerShift.get(node);
			}
		}

		// Debug output
		if (debugMode) {
			System.out.println("Chosen node placement: " + chosenLayout);
			System.out.println("Blocks: " + getBlocks(chosenLayout));
			System.out.println("Classes: " + getClasses(chosenLayout));
			System.out.println("Marked edges: " + markedEdges);
		}

		markedEdges.clear();
		monitor.done();
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// Conflict Detection

	/** The minimum number of layers we need to have conflicts. */
	private static final int MIN_LAYERS_FOR_CONFLICTS = 3;

	/**
	 * This phase of the node placer marks all type 1 and type 2 conflicts.
	 *
	 * <p>
	 * The conflict types base on the distinction of inner segments and
	 * non-inner segments of edges. A inner segment is present if an edge is
	 * drawn between two dummy nodes and thus is part of a long edge. A
	 * non-inner segment is present if one of the connected nodes is not a dummy
	 * node.
	 * </p>
	 *
	 * <p>
	 * Type 0 conflicts occur if two non-inner segments cross each other. Type 1
	 * conflicts happen when a non-inner segment and a inner segment cross. Type
	 * 2 conflicts are present if two inner segments cross.
	 * </p>
	 *
	 * <p>
	 * The markers are later used to solve conflicts in favor of long edges. In
	 * case of type 2 conflicts, the marker favors the earlier node in layout
	 * order.
	 * </p>
	 *
	 * @param layeredGraph
	 *            The layered graph to be layouted
	 */
	private void markConflicts(final LGraph layeredGraph) {
		final int numberOfLayers = layeredGraph.getLayers().size();

		// Check if there are enough layers to detect conflicts
		if (numberOfLayers < MIN_LAYERS_FOR_CONFLICTS) {
			return;
		}

		// We'll need the number of nodes in the different layers quite often in
		// this method, so save
		// them up front
		final int[] layerSize = new int[numberOfLayers];
		int layerIndex = 0;
		for (final Layer layer : layeredGraph.getLayers()) {
			layerSize[layerIndex++] = layer.getNodes().size();
		}

		// The following call succeeds since there are at least 3 layers in the
		// graph
		final Iterator<Layer> layerIterator = layeredGraph.getLayers().listIterator(2);
		for (int i = 1; i < (numberOfLayers - 1); i++) {
			// The variable naming here follows the notation of the
			// corresponding paper
			// Normally, underscores are not allowed in local variable names,
			// but since there
			// is no way of properly writing indices beside underscores,
			// Checkstyle will be
			// disabled here and in future methods containing indexed variables
			// CHECKSTYLEOFF Local Variable Names
			final Layer currentLayer = layerIterator.next();
			final Iterator<LNode> nodeIterator = currentLayer.getNodes().iterator();

			int k_0 = 0;
			int l = 0;

			for (int l_1 = 0; l_1 < layerSize[i + 1]; l_1++) {
				// In the paper, l and i are indices for the layer and the
				// position in the layer
				final LNode v_l_i = nodeIterator.next(); // currentLayer.getNodes().get(l_1);

				if ((l_1 == ((layerSize[i + 1]) - 1)) || incidentToInnerSegment(v_l_i, i + 1, i)) {
					int k_1 = layerSize[i] - 1;
					if (incidentToInnerSegment(v_l_i, i + 1, i)) {
						k_1 = allUpperNeighbors(v_l_i).get(0).getIndex();
					}

					while (l <= l_1) {
						final LNode v_l = currentLayer.getNodes().get(l);

						if (!incidentToInnerSegment(v_l, i + 1, i)) {
							for (final LNode upperNeighbor : allUpperNeighbors(v_l)) {
								final int k = upperNeighbor.getIndex();

								if ((k < k_0) || (k > k_1)) {
									// Marked edge can't return null here,
									// because the upper neighbor
									// relationship between v_l and
									// upperNeighbor enforces the existence
									// of at least one edge between the two
									// nodes
									markedEdges.add(getEdge(upperNeighbor, v_l));
								}
							}
						}

						l++;
					}

					k_0 = k_1;
				}
			}
			// CHECKSTYLEON Local Variable Names
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// Block Building and Inner Shifting

	/**
	 * The graph is traversed in the given directions and nodes a grouped into
	 * blocks. The nodes in these blocks will later be placed such that the
	 * edges connecting them will be straight lines.
	 *
	 * <p>
	 * Type 1 conflicts are resolved, so that the dummy nodes of a long edge
	 * share the same block if possible, such that the long edge is drawn
	 * straightly.
	 * </p>
	 *
	 * @param layeredGraph
	 *            The layered graph to be layouted
	 * @param bal
	 *            One of the four layouts which shall be used in this step
	 */
	private void verticalAlignment(final LGraph layeredGraph, final BKAlignedLayout bal) {
		// Initialize root and align maps
		for (final Layer layer : layeredGraph.getLayers()) {
			for (final LNode v : layer.getNodes()) {
				bal.root.put(v, v);
				bal.align.put(v, v);
				bal.innerShift.put(v, 0.0);

				if (v.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORTH_SOUTH_PORT) {
					bal.blockContainsNorthSouth.put(v, true);
				} else {
					bal.blockContainsNorthSouth.put(v, false);
				}

				if (v.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORMAL) {
					bal.blockContainsRegularNode.put(v, true);
				} else {
					bal.blockContainsRegularNode.put(v, false);
				}
			}
		}

		List<Layer> layers = layeredGraph.getLayers();

		// If the horizontal direction is bottom, the layers are traversed from
		// right to left, thus a reverse iterator is needed
		if (bal.hdir == HDirection.BOTTOM) {
			layers = Lists.reverse(layers);
		}

		for (final Layer layer : layers) {
			// r denotes the position in layer order where the last block was
			// found
			// It is initialized with -1, since nothing is found and the
			// ordering starts with 0
			int r = -1;
			List<LNode> nodes = layer.getNodes();

			if (bal.vdir == VDirection.RIGHT) {
				// If the alignment direction is RIGHT, the nodes in a layer are
				// traversed
				// reversely, thus we start at INT_MAX and with the reversed
				// list of nodes.
				r = Integer.MAX_VALUE;
				nodes = Lists.reverse(nodes);
			}

			// Variable names here are again taken from the paper mentioned
			// above.
			// i denotes the index of the layer and k the position of the node
			// within the layer.
			// m denotes the position of a neighbor in the neighbor list of a
			// node.
			// CHECKSTYLEOFF Local Variable Names
			for (final LNode v_i_k : nodes) {
				List<LNode> neighbors = null;
				if (bal.hdir == HDirection.BOTTOM) {
					neighbors = allLowerNeighbors(v_i_k);
				} else {
					neighbors = allUpperNeighbors(v_i_k);
				}

				if (neighbors.size() > 0) {

					// When a node has many upper neighbors, consider only the
					// (two) nodes in the
					// middle.
					final int d = neighbors.size();
					final int low = ((int) Math.floor(((d + 1.0) / 2.0))) - 1;
					final int high = ((int) Math.ceil(((d + 1.0) / 2.0))) - 1;

					if (bal.vdir == VDirection.RIGHT) {
						// Check, whether v_i_k can be added to a block of its
						// upper/lower neighbor(s)
						for (int m = high; m >= low; m--) {
							if (bal.align.get(v_i_k).equals(v_i_k)) {
								final LNode u_m = neighbors.get(m);

								// Again, getEdge won't return null because the
								// neighbor relationship
								// ensures that at least one edge exists
								if (!markedEdges.contains(getEdge(u_m, v_i_k))
										&& (r > u_m.getIndex())) {
									bal.align.put(u_m, v_i_k);
									bal.root.put(v_i_k, bal.root.get(u_m));
									bal.align.put(v_i_k, bal.root.get(v_i_k));

									if (bal.blockContainsNorthSouth.get(v_i_k)) {
										bal.blockContainsNorthSouth.put(bal.root.get(v_i_k), true);
									}

									if (bal.blockContainsRegularNode.get(v_i_k)) {
										bal.blockContainsRegularNode.put(bal.root.get(v_i_k), true);
									}

									r = u_m.getIndex();
								}
							}
						}
					} else {
						// Check, whether vik can be added to a block of its
						// upper/lower neighbor(s)
						for (int m = low; m <= high; m++) {
							if (bal.align.get(v_i_k).equals(v_i_k)) {
								final LNode um = neighbors.get(m);
								if (!markedEdges.contains(getEdge(um, v_i_k))
										&& (r < um.getIndex())) {
									bal.align.put(um, v_i_k);
									bal.root.put(v_i_k, bal.root.get(um));
									bal.align.put(v_i_k, bal.root.get(v_i_k));

									if (bal.blockContainsNorthSouth.get(v_i_k)) {
										bal.blockContainsNorthSouth.put(bal.root.get(v_i_k), true);
									}

									if (bal.blockContainsRegularNode.get(v_i_k)) {
										bal.blockContainsRegularNode.put(bal.root.get(v_i_k), true);
									}

									r = um.getIndex();
								}
							}
						}
					}
				}
			}
			// CHECKSTYLEOFF Local Variable Names
		}
	}

	/**
	 * This phase moves the nodes inside a block, ensuring that all edges inside
	 * a block can be drawn as straight lines. It is not included in the
	 * original algorithm and adds port and node size handling.
	 *
	 * @param layeredGraph
	 *            The layered graph to be layouted
	 * @param bal
	 *            One of the four layouts which shall be used in this step
	 */
	private void insideBlockShift(final LGraph layeredGraph, final BKAlignedLayout bal) {
		final HashMap<LNode, List<LNode>> blocks = getBlocks(bal);
		for (final LNode root : blocks.keySet()) {
			/*
			 * For each block, we place the top left corner of the root node at
			 * coordinate (0,0). We then calculate the space required above the
			 * top left corner (due to other nodes placed above and to top
			 * margins of nodes, including the root node) and the space required
			 * below the top left corner. The sum of both becomes the block
			 * size, and the y coordinate of each node relative to the block's
			 * top border becomes the inner shift of that node.
			 */

			double spaceAbove = 0.0;
			double spaceBelow = 0.0;

			// Reserve space for the root node
			spaceAbove = root.getMargin().top;
			spaceBelow = root.getSize().y + root.getMargin().bottom;
			bal.innerShift.put(root, 0.0);

			// Iterate over all other nodes of the block
			LNode current = root;
			LNode next;
			while ((next = bal.align.get(current)) != root) {
				// Find the edge between the current and the next node
				final LEdge edge = getEdge(current, next);

				// Calculate the y coordinate difference between the two nodes
				// required to straighten
				// the edge
				double portPosDiff = 0.0;
				if (bal.hdir == HDirection.BOTTOM) {
					portPosDiff = (edge.getTarget().getPosition().y + edge.getTarget().getAnchor().y)
							- edge.getSource().getPosition().y - edge.getSource().getAnchor().y;
				} else {
					portPosDiff = (edge.getSource().getPosition().y + edge.getSource().getAnchor().y)
							- edge.getTarget().getPosition().y - edge.getTarget().getAnchor().y;
				}

				// The current node already has an inner shift value that we
				// need to use as the basis
				// to calculate the next node's inner shift
				final double nextInnerShift = bal.innerShift.get(current) + portPosDiff;
				bal.innerShift.put(next, nextInnerShift);

				// Update the space required above and below the root node's top
				// left corner
				spaceAbove = Math.max(spaceAbove, next.getMargin().top - nextInnerShift);
				spaceBelow = Math.max(spaceBelow,
						nextInnerShift + next.getSize().y + next.getMargin().bottom);

				// The next node is the current node in the next iteration
				current = next;
			}

			// Adjust each node's inner shift by the space required above the
			// root node's top left
			// corner (which the inner shifts are relative to at the moment)
			current = root;
			do {
				bal.innerShift.put(current, bal.innerShift.get(current) + spaceAbove);
				current = bal.align.get(current);
			} while (current != root);

			// Remember the block size
			bal.blockSize.put(root, spaceAbove + spaceBelow);
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// Block Placement

	/**
	 * In this step, actual coordinates are calculated for blocks and its nodes.
	 *
	 * <p>
	 * First, all blocks are placed, trying to avoid any crossing of the blocks.
	 * Then, the blocks are shifted towards each other if there is any space for
	 * compaction.
	 * </p>
	 *
	 * @param layeredGraph
	 *            The layered graph to be layouted
	 * @param bal
	 *            One of the four layouts which shall be used in this step
	 */
	private void horizontalCompaction(final LGraph layeredGraph, final BKAlignedLayout bal) {
		// Initialize fields with basic values, partially depending on the
		// direction
		for (final Layer layer : layeredGraph.getLayers()) {
			for (final LNode node : layer.getNodes()) {
				bal.sink.put(node, node);
				bal.shift.put(node, bal.vdir == VDirection.RIGHT ? Double.NEGATIVE_INFINITY
						: Double.POSITIVE_INFINITY);
			}
		}

		// If the horizontal direction is bottom, the layers are traversed from
		// right to left, thus
		// a reverse iterator is needed (note that this does not change the
		// original list of layers)
		List<Layer> layers = layeredGraph.getLayers();
		if (bal.hdir == HDirection.BOTTOM) {
			layers = Lists.reverse(layers);
		}

		for (final Layer layer : layers) {
			// As with layers, we need a reversed iterator for blocks for
			// different directions
			List<LNode> nodes = layer.getNodes();
			if (bal.vdir == VDirection.RIGHT) {
				nodes = Lists.reverse(nodes);
			}

			// Do an initial placement for all blocks
			for (final LNode v : nodes) {
				if (bal.root.get(v).equals(v)) {
					placeBlock(v, bal);
				}
			}
		}

		// Try to compact blocks by shifting them towards each other if there is
		// space between them.
		// It's important to traverse top-bottom or bottom-top here too
		for (final Layer layer : layers) {
			for (final LNode v : layer.getNodes()) {
				bal.y.put(v, bal.y.get(bal.root.get(v)));

				// If this is the root node of the block, check if the whole
				// block can be shifted to
				// further compact the drawing (the block's non-root nodes will
				// be processed later by
				// this loop and will thus use the updated y position calculated
				// here)
				if (v.equals(bal.root.get(v))) {
					final double sinkShift = bal.shift.get(bal.sink.get(v));

					if (((bal.vdir == VDirection.RIGHT) && (sinkShift > Double.NEGATIVE_INFINITY))
							|| ((bal.vdir == VDirection.LEFT) && (sinkShift < Double.POSITIVE_INFINITY))) {

						bal.y.put(v, bal.y.get(v) + sinkShift);
					}
				}
			}
		}
	}

	/*
	 * Note: The following methods diverts from the convention of naming
	 * variables as they were named in the original paper for better code
	 * readability. (Since this is one of the most intricate pieces of code in
	 * the algorithm.) The original variable names are mentioned in comments.
	 */

	/**
	 * Blocks are placed based on their root node. This is done by going through
	 * all layers the block occupies and moving the whole block upwards /
	 * downwards if there are blocks that it overlaps with.
	 *
	 * @param root
	 *            The root node of the block (usually called {@code v})
	 * @param bal
	 *            One of the four layouts which shall be used in this step
	 */
	private void placeBlock(final LNode root, final BKAlignedLayout bal) {
		// Skip if the block was already placed
		if (bal.y.containsKey(root)) {
			return;
		}

		// Initial placement
		// TODO Fix the following two lines
		// Placing the root at coordinate 0 causes problems later on. The
		// initial position should
		// be determined more intelligently. The second line was a first attempt
		// that fixed the problem
		// in the sample graph attached to KIPRA-1426, but I'm not convinced
		// that it is a good solution
		// in general.
		bal.y.put(root, 0.0);
		// bal.y.put(root, nextBlockYPosition);

		// Iterate through block and determine, where the block can be placed
		// (until we arrive at the
		// block's root node again)
		LNode currentNode = root;
		do {
			final int currentIndexInLayer = currentNode.getIndex();
			final int currentLayerSize = currentNode.getLayer().getNodes().size();
			final NodeType currentNodeType = currentNode.getProperty(InternalProperties.NODE_TYPE);

			// If the node is the top or bottom node of its layer, it can be
			// placed safely since it is
			// the first to be placed in its layer. If it's not, we'll have to
			// check its neighbours
			if (((bal.vdir == VDirection.LEFT) && (currentIndexInLayer > 0))
					|| ((bal.vdir == VDirection.RIGHT) && (currentIndexInLayer < (currentLayerSize - 1)))) {

				// Get the node which is above / below the current node as well
				// as the root of its block
				LNode neighbor = null;
				LNode neighborRoot = null;
				if (bal.vdir == VDirection.RIGHT) {
					neighbor = currentNode.getLayer().getNodes().get(currentIndexInLayer + 1);
				} else {
					neighbor = currentNode.getLayer().getNodes().get(currentIndexInLayer - 1);
				}
				neighborRoot = bal.root.get(neighbor);

				// The neighbour's node type is important for the spacing
				// between the two later on
				final NodeType neighborNodeType = neighbor
						.getProperty(InternalProperties.NODE_TYPE);

				// Ensure the neighbor was already placed
				placeBlock(neighborRoot, bal);

				// Note that the two nodes and their blocks form a unit called
				// class in the original
				// algorithm. These are combinations of blocks which play a role
				// in the final compaction
				if (bal.sink.get(root).equals(root)) {
					bal.sink.put(root, bal.sink.get(neighborRoot));
				}

				// Check if the blocks of the two nodes are members of the same
				// class
				if (bal.sink.get(root).equals(bal.sink.get(neighborRoot))) {
					// They are part of the same class

					// The minimal spacing between the two nodes depends on
					// their node type
					double spacing = smallSpacing;
					if ((currentNodeType == NodeType.EXTERNAL_PORT)
							&& (neighborNodeType == NodeType.EXTERNAL_PORT)) {

						spacing = externalPortSpacing;
					} else if ((currentNodeType == NodeType.NORMAL)
							&& (neighborNodeType == NodeType.NORMAL)) {

						spacing = normalSpacing;
					}

					// TODO Check what to do about NORTH_SOUTH_SPACING
					// (previous version of the algorithm did something here,
					// which the current version
					// does not)

					// Determine the block's final position
					if (bal.vdir == VDirection.RIGHT) {
						bal.y.put(
								root,
								Math.min(
										bal.y.get(root),
										(bal.y.get(neighborRoot) + bal.innerShift.get(neighbor))
										- neighbor.getMargin().top - spacing
										- currentNode.getMargin().bottom
										- currentNode.getSize().y
										- bal.innerShift.get(currentNode)));
					} else {
						bal.y.put(root, Math.max(
								bal.y.get(root),
								(bal.y.get(neighborRoot) + bal.innerShift.get(neighbor)
										+ neighbor.getSize().y + neighbor.getMargin().bottom
										+ spacing + currentNode.getMargin().top)
										- bal.innerShift.get(currentNode)));
					}
				} else {
					// TODO Take a look at this code

					// They are not part of the same class. Compute how the two
					// classes can be compacted
					// later
					final double spacing = normalSpacing;

					if (bal.vdir == VDirection.RIGHT) {
						bal.shift.put(
								bal.sink.get(neighborRoot),
								Math.max(
										bal.shift.get(bal.sink.get(neighborRoot)),
										(bal.y.get(root) - bal.y.get(neighborRoot))
										+ bal.blockSize.get(root) + spacing));
					} else {
						bal.shift.put(
								bal.sink.get(neighborRoot),
								Math.min(bal.shift.get(bal.sink.get(neighborRoot)), bal.y.get(root)
										- bal.y.get(neighborRoot) - bal.blockSize.get(neighborRoot)
										- spacing));
					}
				}
			}

			// Get the next node in the block
			currentNode = bal.align.get(currentNode);
		} while (currentNode != root);

		// determine position for next block
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// Layout Balancing

	/**
	 * Calculates a balanced layout by determining the median of the four
	 * layouts.
	 *
	 * <p>
	 * First, the layout with the smallest height, meaning the difference
	 * between the highest and the lowest y-coordinate placement, is used as a
	 * starting point. Then, the median position of each of the four layouts is
	 * used to determine the final position.
	 * </p>
	 *
	 * @param layouts
	 *            The four calculated layouts
	 * @param nodeCount
	 *            The number of nodes in the graph
	 * @return A balanced layout, the median of the four layouts
	 */
	private BKAlignedLayout createBalancedLayout(final List<BKAlignedLayout> layouts,
			final int nodeCount) {

		final int noOfLayouts = layouts.size();
		final BKAlignedLayout balanced = new BKAlignedLayout(nodeCount, null, null);
		final double[] width = new double[noOfLayouts];
		final double[] min = new double[noOfLayouts];
		final double[] max = new double[noOfLayouts];
		int minWidthLayout = 0;

		// Find the smallest layout
		for (int i = 0; i < noOfLayouts; i++) {
			min[i] = Integer.MAX_VALUE;
			max[i] = Integer.MIN_VALUE;
		}

		for (int i = 0; i < noOfLayouts; i++) {
			final BKAlignedLayout current = layouts.get(i);
			for (final double y : current.y.values()) {
				if (min[i] > y) {
					min[i] = y;
				}

				if (max[i] < y) {
					max[i] = y;
				}
			}

			width[i] = max[i] - min[i];
			if (width[minWidthLayout] > width[i]) {
				minWidthLayout = i;
			}
		}

		// Find the shift between the smallest and the four layouts
		final double[] shift = new double[noOfLayouts];
		for (int i = 0; i < noOfLayouts; i++) {
			if (layouts.get(i).vdir == VDirection.LEFT) {
				shift[i] = min[minWidthLayout] - min[i];
			} else {
				shift[i] = max[minWidthLayout] - max[i];
			}
		}

		// Calculated y-coordinates for a balanced placement
		final double[] calculatedYs = new double[noOfLayouts];
		for (final LNode node : layouts.get(0).y.keySet()) {
			for (int i = 0; i < noOfLayouts; i++) {
				calculatedYs[i] = layouts.get(i).y.get(node) + shift[i];
			}

			Arrays.sort(calculatedYs);
			balanced.y.put(node, (calculatedYs[1] + calculatedYs[2]) / 2.0);
			balanced.innerShift.put(node, layouts.get(minWidthLayout).innerShift.get(node));
		}

		return balanced;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// Utility Methods

	/**
	 * Checks whether the given node is part of a long edge between the two
	 * given layers. At this 'layer2' is left, or before, 'layer1'.
	 *
	 * @param node
	 *            Possible long edge node
	 * @param layer1
	 *            The first layer, the layer of the node
	 * @param layer2
	 *            The second layer
	 * @return True if the node is part of a long edge between the layers, false
	 *         else
	 */
	private boolean incidentToInnerSegment(final LNode node, final int layer1, final int layer2) {
		// consider that big nodes include their respective start and end node.
		if (node.getProperty(InternalProperties.NODE_TYPE) == NodeType.BIG_NODE) {
			// all nodes should be placed straightly
			for (final LEdge edge : node.getIncomingEdges()) {
				final LNode source = edge.getSource().getNode();
				if (((source.getProperty(InternalProperties.NODE_TYPE) == NodeType.BIG_NODE) || source
						.getProperty(InternalProperties.BIG_NODE_INITIAL))
						&& (edge.getSource().getNode().getLayer().getIndex() == layer2)
						&& (node.getLayer().getIndex() == layer1)) {

					return true;
				}
			}
		}

		if (node.getProperty(InternalProperties.NODE_TYPE) == NodeType.LONG_EDGE) {
			for (final LEdge edge : node.getIncomingEdges()) {
				final NodeType sourceNodeType = edge.getSource().getNode()
						.getProperty(InternalProperties.NODE_TYPE);

				// TODO Using layer indices here is not a good idea in terms of
				// performance
				if ((sourceNodeType == NodeType.LONG_EDGE)
						&& (edge.getSource().getNode().getLayer().getIndex() == layer2)
						&& (node.getLayer().getIndex() == layer1)) {

					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gives all upper neighbors of a given node. An upper neighbor is a node in
	 * a previous layer that has an edge pointing to the given node.
	 *
	 * @param node
	 *            The node which might have neighbors
	 * @return A list containing all upper neighbors
	 */
	private List<LNode> allUpperNeighbors(final LNode node) {
		final List<LNode> result = new LinkedList<LNode>();
		int maxPriority = 0;

		for (final LEdge edge : node.getIncomingEdges()) {
			if (edge.getProperty(Properties.PRIORITY) > maxPriority) {
				maxPriority = edge.getProperty(Properties.PRIORITY);
			}
		}

		for (final LEdge edge : node.getIncomingEdges()) {
			if ((node.getLayer() != edge.getSource().getNode().getLayer())
					&& (edge.getProperty(Properties.PRIORITY) == maxPriority)) {
				result.add(edge.getSource().getNode());
			}
		}

		Collections.sort(result, NeighborComparator.INSTANCE);
		return result;
	}

	/**
	 * Give all lower neighbors of a given node. A lower neighbor is a node in a
	 * following layer that has an edge coming from the given node.
	 *
	 * @param node
	 *            The node which might have neighbors
	 * @return A list containing all lower neighbors
	 */
	private List<LNode> allLowerNeighbors(final LNode node) {
		final List<LNode> result = new LinkedList<LNode>();
		int maxPriority = 0;

		for (final LEdge edge : node.getOutgoingEdges()) {
			if (edge.getProperty(Properties.PRIORITY) > maxPriority) {
				maxPriority = edge.getProperty(Properties.PRIORITY);
			}
		}

		for (final LEdge edge : node.getOutgoingEdges()) {
			if ((node.getLayer() != edge.getTarget().getNode().getLayer())
					&& (edge.getProperty(Properties.PRIORITY) == maxPriority)) {

				result.add(edge.getTarget().getNode());
			}
		}

		Collections.sort(result, NeighborComparator.INSTANCE);
		return result;
	}

	/**
	 * Find an edge between two given nodes.
	 *
	 * @param source
	 *            The source node of the edge
	 * @param target
	 *            The target node of the edge
	 * @return The edge between source and target, or null if there is none
	 */
	private LEdge getEdge(final LNode source, final LNode target) {
		for (final LEdge edge : source.getConnectedEdges()) {
			if (edge.getTarget().getNode().equals(target)
					|| edge.getSource().getNode().equals(target)) {
				return edge;
			}
		}

		return null;
	}

	/**
	 * Finds all blocks of a given layout.
	 *
	 * @param bal
	 *            The layout of which the blocks shall be found
	 * @return The blocks of the given layout
	 */
	private HashMap<LNode, List<LNode>> getBlocks(final BKAlignedLayout bal) {
		final HashMap<LNode, List<LNode>> blocks = new HashMap<LNode, List<LNode>>();

		for (final LNode node : bal.root.keySet()) {
			final LNode root = bal.root.get(node);
			List<LNode> blockContents = blocks.get(root);

			if (blockContents == null) {
				blockContents = Lists.newLinkedList();
				blocks.put(root, blockContents);
			}

			blockContents.add(node);
		}

		return blocks;
	}

	/**
	 * Finds all classes of a given layout.
	 *
	 * @param bal
	 *            The layout whose classes to find
	 * @return The classes of the given layout
	 */
	private HashMap<LNode, List<LNode>> getClasses(final BKAlignedLayout bal) {
		final HashMap<LNode, List<LNode>> classes = new HashMap<LNode, List<LNode>>();

		// We need to enumerate all block roots
		final Set<LNode> roots = Sets.newHashSet(bal.root.values());
		for (final LNode root : roots) {
			final LNode sink = bal.sink.get(root);
			List<LNode> classContents = classes.get(sink);

			if (classContents == null) {
				classContents = Lists.newLinkedList();
				classes.put(sink, classContents);
			}

			classContents.add(root);
		}

		return classes;
	}

	/**
	 * Checks whether all nodes are placed in the correct order in their layers
	 * and do not overlap each other.
	 *
	 * @param layeredGraph
	 *            the containing layered graph.
	 * @param bal
	 *            the layout which shall be checked.
	 * @return {@code true} if the order is preserved and no nodes overlap,
	 *         {@code false} otherwise.
	 */
	private boolean checkOrderConstraint(final LGraph layeredGraph, final BKAlignedLayout bal) {
		// Check if the layout contains Y coordinate information
		if (bal.y.isEmpty()) {
			return false;
		}

		// Flag indicating whether the layout is feasible or not
		boolean feasible = true;

		// Iterate over the layers
		for (final Layer layer : layeredGraph.getLayers()) {
			// Current Y position in the layer
			double pos = Double.NEGATIVE_INFINITY;

			// We remember the previous node for debug output
			LNode previous = null;

			// Iterate through the layer's nodes
			for (final LNode node : layer.getNodes()) {
				// For the layout to be correct, both the node's top border and
				// its bottom border must
				// be beyond the current position in the layer
				final double top = (bal.y.get(node) + bal.innerShift.get(node))
						- node.getMargin().top;
				final double bottom = bal.y.get(node) + bal.innerShift.get(node) + node.getSize().y
						+ node.getMargin().bottom;

				if ((top > pos) && (bottom > pos)) {
					previous = node;

					// Update the position inside the layer
					pos = bal.y.get(node) + bal.innerShift.get(node) + node.getSize().y
							+ node.getMargin().bottom;
				} else {
					// We've found an overlap
					feasible = false;
					if (debugMode) {
						System.out.println("bk node placement breaks on " + node
								+ " which should have been after " + previous);
					}
					break;
				}
			}

			// Don't bother continuing if we've already determined that the
			// layout is infeasible
			if (!feasible) {
				break;
			}
		}

		if (debugMode) {
			System.out.println(bal + " is feasible: " + feasible);
		}

		return feasible;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// Class BKAlignedLayout

	/**
	 * Class which holds all information about a layout in one of the four
	 * direction combinations.
	 */
	private static final class BKAlignedLayout {
		/** The root node of each node in a block. */
		private final HashMap<LNode, LNode> root;
		/** The size of a block. */
		private final HashMap<LNode, Double> blockSize;
		/**
		 * The next node in a block, or the first if the current node is the
		 * last, forming a ring.
		 */
		private final HashMap<LNode, LNode> align;
		/**
		 * The value by which a node must be shifted to stay straight inside a
		 * block.
		 */
		private final HashMap<LNode, Double> innerShift;
		/**
		 * The root node of a class, mapped from block root nodes to class root
		 * nodes.
		 */
		private final HashMap<LNode, LNode> sink;
		/**
		 * The value by which a block must be shifted for a more compact
		 * placement.
		 */
		private final HashMap<LNode, Double> shift;
		/** The y-coordinate of every node, forming the final layout. */
		private final HashMap<LNode, Double> y;
		/** Whether a block contains a NORTH SOUTH port dummy. */
		// TODO: I don't think this is necessary anymore. If it is, it doesn't
		// need to be a map.
		private final HashMap<LNode, Boolean> blockContainsNorthSouth;
		/** Whether a block contains a regular node. */
		// TODO: I don't think this is necessary anymore. If it is, it doesn't
		// need to be a map.
		private final HashMap<LNode, Boolean> blockContainsRegularNode;
		/** The vertical direction of the current layout. */
		private final VDirection vdir;
		/** The horizontal direction of the current layout. */
		private final HDirection hdir;

		/**
		 * Basic constructor for a layout.
		 */
		public BKAlignedLayout(final int nodeCount, final VDirection vdir, final HDirection hdir) {
			root = Maps.newHashMapWithExpectedSize(nodeCount);
			blockSize = Maps.newHashMapWithExpectedSize(nodeCount);
			align = Maps.newHashMapWithExpectedSize(nodeCount);
			innerShift = Maps.newHashMapWithExpectedSize(nodeCount);
			sink = Maps.newHashMapWithExpectedSize(nodeCount);
			shift = Maps.newHashMapWithExpectedSize(nodeCount);
			y = Maps.newHashMapWithExpectedSize(nodeCount);
			blockContainsNorthSouth = Maps.newHashMapWithExpectedSize(nodeCount);
			blockContainsRegularNode = Maps.newHashMapWithExpectedSize(nodeCount);
			this.vdir = vdir;
			this.hdir = hdir;
		}

		/**
		 * Calculate the layout size for comparison.
		 *
		 * @return The size of the layout
		 */
		public double layoutSize() {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			// Prior to KIPRA-1426 the size of the layout was determined
			// only based on y coordinates, neglecting any block sizes.
			// We now determine the maximal extend of the layout based on
			// the minimum y coordinate of any node and the maximum
			// y coordinate _plus_ the size of any block.
			for (final LNode n : y.keySet()) {
				final double yMin = y.get(n);
				final double yMax = yMin + blockSize.get(root.get(n));
				min = Math.min(min, yMin);
				max = Math.max(max, yMax);
			}
			return max - min;
		}

		@Override
		public String toString() {
			String result = "";
			if (vdir == VDirection.LEFT) {
				result += "LEFT";
			} else if (vdir == VDirection.RIGHT) {
				result += "RIGHT";
			} else {
				result += "BALANCED";
			}
			if (hdir == HDirection.TOP) {
				result += "TOP";
			} else if (hdir == HDirection.BOTTOM) {
				result += "BOTTOM";
			}
			return result;
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// Class NeighborComparator

	/**
	 * Comparator which determines the order of nodes in a layer.
	 */
	private static final class NeighborComparator implements Comparator<LNode>, Serializable {
		/** The serial version UID. */
		private static final long serialVersionUID = 7540379553811800233L;
		/** Singleton instance. */
		public static final NeighborComparator INSTANCE = new NeighborComparator();

		/**
		 * Private constructor. Singleton.
		 */
		private NeighborComparator() {

		}

		/**
		 * {@inheritDoc}
		 */
		public int compare(final LNode o1, final LNode o2) {
			int result = 0;
			if (o1.getIndex() < o2.getIndex()) {
				result = -1;
			} else if (o1.getIndex() > o2.getIndex()) {
				result = 1;
			}
			return result;
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// Enumerations

	/**
	 * Vertical direction enumeration.
	 */
	private static enum VDirection {
		LEFT, RIGHT;
	}

	/**
	 * Horizontal direction enumeration.
	 */
	private static enum HDirection {
		TOP, BOTTOM;
	}
}
