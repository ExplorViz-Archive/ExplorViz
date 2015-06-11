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
package de.cau.cs.kieler.klay.layered.p4nodes;

import java.util.*;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.*;
import de.cau.cs.kieler.klay.layered.graph.LNode.NodeType;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.*;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Node placement implementation that aligns long edges using linear segments.
 * Inspired by Section 4 of
 * <ul>
 * <li>Georg Sander, A fast heuristic for hierarchical Manhattan layout. In
 * <i>Proceedings of the Symposium on Graph Drawing (GD'95)</i>, LNCS vol. 1027,
 * pp. 447-458, Springer, 1996.</li>
 * </ul>
 *
 * <dl>
 * <dt>Precondition:</dt>
 * <dd>the graph has a proper layering with optimized nodes ordering; ports are
 * properly arranged</dd>
 * <dt>Postcondition:</dt>
 * <dd>each node is assigned a vertical coordinate such that no two nodes
 * overlap; the size of each layer is set according to the area occupied by
 * contained nodes; the height of the graph is set to the maximal layer height</dd>
 * </dl>
 *
 * @author msp
 * @author grh
 * @author cds
 * @author ima
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class LinearSegmentsNodePlacer implements ILayoutPhase {

	/**
	 * A linear segment contains a single regular node or all dummy nodes of a
	 * long edge.
	 */
	public static class LinearSegment implements Comparable<LinearSegment> {
		/** Nodes of the linear segment. */
		private final List<LNode> nodes = Lists.newArrayList();
		/** Identifier value, used as index in the segments array. */
		private int id;
		/** Index in the previous layer. Used for cycle avoidance. */
		private int indexInLastLayer = -1;
		/**
		 * The last layer where a node belonging to this segment was discovered.
		 * Used for cycle avoidance.
		 */
		private int lastLayer = -1;
		/** The accumulated force of the contained nodes. */
		private double deflection;
		/** The current weight of the contained nodes. */
		private int weight;
		/** The reference segment, if this has been unified with another. */
		private LinearSegment refSegment;
		/** The nodetype contained in this linear segment. */
		private NodeType nodeType;

		/**
		 * Determine the reference segment for the region to which this segment
		 * is associated.
		 * 
		 * @return the region segment
		 */
		LinearSegment region() {
			LinearSegment seg = this;
			while (seg.refSegment != null) {
				seg = seg.refSegment;
			}
			return seg;
		}

		/**
		 * Splits this linear segment before the given node. The returned
		 * segment contains all nodes from the given node onward, with their ID
		 * set to the new segment's ID. Those nodes are removed from this
		 * segment.
		 * 
		 * @param node
		 *            the node to split the segment at.
		 * @param newId
		 *            the new segment's id.
		 * @return new linear segment with ID {@code -1} and all nodes from
		 *         {@code node} onward.
		 */
		LinearSegment split(final LNode node, final int newId) {
			final int nodeIndex = nodes.indexOf(node);

			// Prepare the new segment
			final LinearSegment newSegment = new LinearSegment();
			newSegment.id = newId;

			// Move nodes to the new segment
			final ListIterator<LNode> iterator = nodes.listIterator(nodeIndex);
			while (iterator.hasNext()) {
				final LNode movedNode = iterator.next();
				movedNode.id = newId;
				newSegment.nodes.add(movedNode);
				iterator.remove();
			}

			return newSegment;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "ls" + nodes.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		public int compareTo(final LinearSegment other) {
			return id - other.id;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(final Object object) {
			if (object instanceof LinearSegment) {
				final LinearSegment other = (LinearSegment) object;
				return id == other.id;
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return id;
		}

	}

	/** additional processor dependencies for graphs with hierarchical ports. */
	private static final IntermediateProcessingConfiguration HIERARCHY_PROCESSING_ADDITIONS = IntermediateProcessingConfiguration
			.createEmpty().addBeforePhase5(
					IntermediateProcessorStrategy.HIERARCHICAL_PORT_POSITION_PROCESSOR);

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

	/** property for maximal priority of incoming edges. */
	private static final Property<Integer> INPUT_PRIO = new Property<Integer>(
			"linearSegments.inputPrio", 0);
	/** property for maximal priority of outgoing edges. */
	private static final Property<Integer> OUTPUT_PRIO = new Property<Integer>(
			"linearSegments.outputPrio", 0);

	/** array of sorted linear segments. */
	private LinearSegment[] linearSegments;

	/**
	 * {@inheritDoc}
	 */
	public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
		monitor.begin("Linear segments node placement", 1);

		// sort the linear segments of the layered graph
		sortLinearSegments(layeredGraph);

		// create an unbalanced placement from the sorted segments
		createUnbalancedPlacement(layeredGraph);

		// balance the placement
		balancePlacement(layeredGraph);

		// post-process the placement for small corrections
		postProcess(layeredGraph);

		// release the created resources
		linearSegments = null;
		monitor.done();
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Linear Segments Creation

	/**
	 * Sorts the linear segments of the given layered graph by finding a
	 * topological ordering in the corresponding segment ordering graph.
	 * 
	 * @param layeredGraph
	 *            layered graph to process
	 * @return a sorted array of linear segments
	 */
	private LinearSegment[] sortLinearSegments(final LGraph layeredGraph) {
		// set the identifier and input / output priority for all nodes
		final List<LinearSegment> segmentList = Lists.newArrayList();
		for (final Layer layer : layeredGraph) {
			for (final LNode node : layer) {
				node.id = -1;
				int inprio = Integer.MIN_VALUE, outprio = Integer.MIN_VALUE;
				for (final LPort port : node.getPorts()) {
					for (final LEdge edge : port.getIncomingEdges()) {
						final int prio = edge.getProperty(InternalProperties.PRIORITY);
						inprio = Math.max(inprio, prio);
					}
					for (final LEdge edge : port.getOutgoingEdges()) {
						final int prio = edge.getProperty(InternalProperties.PRIORITY);
						outprio = Math.max(outprio, prio);
					}
				}
				node.setProperty(INPUT_PRIO, inprio);
				node.setProperty(OUTPUT_PRIO, outprio);
			}
		}

		// create linear segments for the layered graph, ignoring odd port side
		// dummies
		int nextLinearSegmentID = 0;
		for (final Layer layer : layeredGraph) {
			for (final LNode node : layer) {
				// Test for the node ID; calls to fillSegment(...) may have
				// caused the node ID
				// to be != -1.
				if (node.id < 0) {
					final LinearSegment segment = new LinearSegment();
					segment.id = nextLinearSegmentID++;
					fillSegment(node, segment);
					segmentList.add(segment);
				}
			}
		}

		// create and initialize segment ordering graph
		final List<List<LinearSegment>> outgoingList = Lists.newArrayListWithCapacity(segmentList
				.size());
		final List<Integer> incomingCountList = Lists.newArrayListWithCapacity(segmentList.size());
		for (int i = 0; i < segmentList.size(); i++) {
			outgoingList.add(new ArrayList<LinearSegment>());
			incomingCountList.add(0);
		}

		// create edges for the segment ordering graph
		createDependencyGraphEdges(layeredGraph, segmentList, outgoingList, incomingCountList);

		// turn lists into arrays
		final LinearSegment[] segments = segmentList.toArray(new LinearSegment[segmentList.size()]);

		@SuppressWarnings("unchecked")
				final List<LinearSegment>[] outgoing = outgoingList.toArray(new List[outgoingList.size()]);

		final int[] incomingCount = new int[incomingCountList.size()];
		for (int i = 0; i < incomingCount.length; i++) {
			incomingCount[i] = incomingCountList.get(i);
		}

		// gather the sources of the segment ordering graph
		int nextRank = 0;
		final List<LinearSegment> noIncoming = Lists.newArrayList();
		for (int i = 0; i < segments.length; i++) {
			if (incomingCount[i] == 0) {
				noIncoming.add(segments[i]);
			}
		}

		// find a topological ordering of the segment ordering graph
		final int[] newRanks = new int[segments.length];
		while (!noIncoming.isEmpty()) {
			final LinearSegment segment = noIncoming.remove(0);
			newRanks[segment.id] = nextRank++;

			while (!outgoing[segment.id].isEmpty()) {
				final LinearSegment target = outgoing[segment.id].remove(0);
				incomingCount[target.id]--;

				if (incomingCount[target.id] == 0) {
					noIncoming.add(target);
				}
			}
		}

		// apply the new ordering to the array of linear segments
		linearSegments = new LinearSegment[segments.length];
		for (int i = 0; i < segments.length; i++) {
			assert outgoing[i].isEmpty();
			final LinearSegment ls = segments[i];
			final int rank = newRanks[i];
			linearSegments[rank] = ls;
			ls.id = rank;
			for (final LNode node : ls.nodes) {
				node.id = rank;
			}
		}

		return linearSegments;
	}

	/**
	 * Fills the dependency graph with dependencies. If a dependency would
	 * introduce a cycle, the offending linear segment is split into two linear
	 * segments.
	 * 
	 * @param layeredGraph
	 *            the layered graph.
	 * @param segmentList
	 *            the list of segments. Updated to include the newly created
	 *            linear segments.
	 * @param outgoingList
	 *            the lists of outgoing dependencies for each segment. This
	 *            essentially encodes the edges of the dependency graph.
	 * @param incomingCountList
	 *            the number of incoming dependencies for each segment.
	 */
	private void createDependencyGraphEdges(final LGraph layeredGraph,
			final List<LinearSegment> segmentList, final List<List<LinearSegment>> outgoingList,
			final List<Integer> incomingCountList) {

		/*
		 * There's some <scaryVoice> faaaancy </scaryVoice> stuff going on here.
		 * Basically, we go through all the layers, from left to right. In each
		 * layer, we go through all the nodes. For each node, we retrieve the
		 * linear segment it's part of and add a dependency to the next node's
		 * linear segment. So far so good.
		 * 
		 * This works perfectly fine as long as we assume that the relative
		 * order of linear segments doesn't change from one layer to the next.
		 * However, since the introduction of north / south port dummies, it
		 * can. We now have to avoid creating cycles in the dependency graph.
		 * This is done by remembering the indices of each linear segment in the
		 * previous layer. When we encounter a segment x, we check if there is a
		 * segment y that came before x in the previous layer. (that would
		 * introduce a cycle) If that's the case, we split x at the current
		 * layer, resulting in two segments, x1 and x2, x2 starting at the
		 * current layer. Now, we proceed as usual, adding a dependency from x2
		 * to y. But we have avoided a cycle because y does not depend on x2,
		 * but on x1.
		 */

		int nextLinearSegmentID = segmentList.size();
		int layerIndex = 0;
		for (final Layer layer : layeredGraph) {
			final List<LNode> nodes = layer.getNodes();
			if (nodes.isEmpty()) {
				// Ignore empty layers
				continue;
			}

			final Iterator<LNode> nodeIter = nodes.iterator();
			int indexInLayer = 0;

			// We carry the previous node with us for dependency management
			LNode previousNode = null;

			// Get the layer's first node
			LNode currentNode = nodeIter.next();
			LinearSegment currentSegment = null;

			while (currentNode != null) {
				// Get the current node's segment
				currentSegment = segmentList.get(currentNode.id);

				/*
				 * Check if we have a cycle. That's the case if the following
				 * holds: - The current segment appeared in the previous layer
				 * as well. - In the previous layer, we find a segment after the
				 * current segment that appears before the current segment in
				 * the current layer.
				 */
				if (currentSegment.indexInLastLayer >= 0) {
					LinearSegment cycleSegment = null;
					final Iterator<LNode> cycleNodesIter = layer.getNodes().listIterator(
							indexInLayer + 1);
					while (cycleNodesIter.hasNext()) {
						final LNode cycleNode = cycleNodesIter.next();
						cycleSegment = segmentList.get(cycleNode.id);

						if ((cycleSegment.lastLayer == currentSegment.lastLayer)
								&& (cycleSegment.indexInLastLayer < currentSegment.indexInLastLayer)) {

							break;
						} else {
							cycleSegment = null;
						}
					}

					// If we have found a cycle segment, we need to split the
					// current linear segment
					if (cycleSegment != null) {
						// Update the current segment before it's split
						if (previousNode != null) {
							incomingCountList.set(currentNode.id,
									incomingCountList.get(currentNode.id) - 1);
							outgoingList.get(previousNode.id).remove(currentSegment);
						}

						currentSegment = currentSegment.split(currentNode, nextLinearSegmentID++);
						segmentList.add(currentSegment);
						outgoingList.add(new ArrayList<LinearSegment>());

						if (previousNode != null) {
							outgoingList.get(previousNode.id).add(currentSegment);
							incomingCountList.add(1);
						} else {
							incomingCountList.add(0);
						}
					}
				}

				// Now add a dependency to the next node, if any
				LNode nextNode = null;
				if (nodeIter.hasNext()) {
					nextNode = nodeIter.next();
					final LinearSegment nextSegment = segmentList.get(nextNode.id);

					outgoingList.get(currentNode.id).add(nextSegment);
					incomingCountList.set(nextNode.id, incomingCountList.get(nextNode.id) + 1);
				}

				// Update segment's layer information
				currentSegment.lastLayer = layerIndex;
				currentSegment.indexInLastLayer = indexInLayer++;

				// Cycle nodes
				previousNode = currentNode;
				currentNode = nextNode;
			}

			layerIndex++;
		}

	}

	/**
	 * Put a node into the given linear segment and check for following parts of
	 * a long edge.
	 * 
	 * @param node
	 *            the node to put into the linear segment
	 * @param segment
	 *            a linear segment
	 * @return {@code true} if the given node was not already part of another
	 *         segment and was thus added to the given segment.
	 */
	private boolean fillSegment(final LNode node, final LinearSegment segment) {
		NodeType nodeType = node.getNodeType();

		// handle initial big nodes as big node type
		if (node.getProperty(InternalProperties.BIG_NODE_INITIAL)) {
			nodeType = NodeType.BIG_NODE;
		}

		if (node.id >= 0) {
			// The node is already part of another linear segment
			return false;
		} else if ((segment.nodeType != null)
				&& ((nodeType == NodeType.BIG_NODE) && (nodeType != segment.nodeType))) {
			// Big nodes are not allowed to share a linear segment with other
			// dummy nodes
			return false;
		} else {
			// Add the node to the given linear segment
			node.id = segment.id;
			segment.nodes.add(node);
		}
		segment.nodeType = nodeType;

		if ((nodeType == NodeType.LONG_EDGE) || (nodeType == NodeType.NORTH_SOUTH_PORT)
				|| (nodeType == NodeType.BIG_NODE)) {

			// This is a LONG_EDGE, NORTH_SOUTH_PORT or BIG_NODE dummy; check if
			// any of its
			// successors are of one of these types too. If so, we can form a
			// linear segment
			// with one of them. (not with more than one, though)
			// Note 1: LONG_EDGES and NORTH_SOUTH_PORTs can share a common
			// linear segment
			// Note 2: we must take care not to make a segment out of nodes that
			// are in the same layer
			// Note 3: for BIG_NODEs also the first BIG_NODE_INITIAL which is no
			// actual dummy node has
			// to be considered here
			for (final LPort sourcePort : node.getPorts()) {
				for (final LPort targetPort : sourcePort.getSuccessorPorts()) {
					final LNode targetNode = targetPort.getNode();
					final NodeType targetNodeType = targetNode.getNodeType();

					if (node.getLayer() != targetNode.getLayer()) {
						if (nodeType == NodeType.BIG_NODE) {
							// current AND the next node are BIG_NODE dummies
							if (targetNodeType == NodeType.BIG_NODE) {
								if (fillSegment(targetNode, segment)) {
									// We just added another node to this node's
									// linear segment.
									// That's quite enough.
									return true;
								}
							}
						} else {
							// current no bignode and next node is LONG_EDGE and
							// NORTH_SOUTH_PORT
							if ((targetNodeType == NodeType.LONG_EDGE)
									|| (targetNodeType == NodeType.NORTH_SOUTH_PORT)) {
								if (fillSegment(targetNode, segment)) {
									// We just added another node to this node's
									// linear segment.
									// That's quite enough.
									return true;
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Unbalanced Placement

	/**
	 * Creates an unbalanced placement for the sorted linear segments.
	 * 
	 * @param layeredGraph
	 *            the layered graph to create an unbalanced placement for.
	 */
	private void createUnbalancedPlacement(final LGraph layeredGraph) {
		final float normalSpacing = layeredGraph.getProperty(InternalProperties.SPACING)
				* layeredGraph.getProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR);
		final float smallSpacing = normalSpacing
				* layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);
		final float externalPortSpacing = layeredGraph.getProperty(InternalProperties.PORT_SPACING);

		// How many nodes are currently placed in each layer
		final int[] nodeCount = new int[layeredGraph.getLayers().size()];

		// The type of the node most recently placed in a given layer
		final NodeType[] recentNodeType = new NodeType[layeredGraph.getLayers().size()];

		// Iterate through the linear segments (in proper order!) and place them
		for (final LinearSegment segment : linearSegments) {
			// Determine the uppermost placement for the linear segment
			double uppermostPlace = 0.0f;
			for (final LNode node : segment.nodes) {
				final NodeType nodeType = node.getNodeType();
				final int layerIndex = node.getLayer().getIndex();
				nodeCount[layerIndex]++;

				// Calculate how much space to leave between the linear segment
				// and the last
				// node of the given layer
				float spacing = smallSpacing;
				if (nodeCount[layerIndex] > 0) {
					if ((recentNodeType[layerIndex] == NodeType.NORMAL)
							|| (nodeType == NodeType.NORMAL)) {

						spacing = normalSpacing;
					} else if ((recentNodeType[layerIndex] == NodeType.EXTERNAL_PORT)
							&& (nodeType == NodeType.EXTERNAL_PORT)) {

						spacing = externalPortSpacing;
					}
				}

				uppermostPlace = Math.max(uppermostPlace, node.getLayer().getSize().y + spacing);
			}

			// Apply the uppermost placement to all elements
			for (final LNode node : segment.nodes) {
				// Set the node position
				node.getPosition().y = uppermostPlace + node.getMargin().top;

				// Adjust layer size
				final Layer layer = node.getLayer();
				layer.getSize().y = uppermostPlace + node.getMargin().top + node.getSize().y
						+ node.getMargin().bottom;

				recentNodeType[layer.getIndex()] = node.getNodeType();
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Balanced Placement

	/** Definition of balancing modes. */
	private static enum Mode {
		FORW_PENDULUM, BACKW_PENDULUM, RUBBER;
	}

	/** factor for threshold after which balancing is aborted. */
	private static final double THRESHOLD_FACTOR = 20.0;
	/** the minimal number of iterations in pendulum mode. */
	private static final int PENDULUM_ITERS = 4;
	/** the number of additional iterations after the abort condition was met. */
	private static final int FINAL_ITERS = 3;

	/**
	 * Balance the initial placement by force-based movement of regions. First
	 * perform <em>pendulum</em> iterations, where only one direction of edges
	 * is considered, then <em>rubber</em> iterations, where both incoming and
	 * outgoing edges are considered. In each iteration first determine the
	 * <em>deflection</em> of each linear segment, i.e. the optimal position
	 * delta that leads to a balanced placement with respect to its adjacent
	 * segments. Then merge regions that touch each other, building mean values
	 * of the involved deflections, and finally apply the resulting deflection
	 * values to all segments. The iterations stop when no further improvement
	 * is done.
	 * 
	 * @param layeredGraph
	 *            a layered graph
	 */
	private void balancePlacement(final LGraph layeredGraph) {
		final float spacing = layeredGraph.getProperty(InternalProperties.SPACING)
				* layeredGraph.getProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR);
		final float smallSpacing = spacing
				* layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);
		final float externalPortSpacing = layeredGraph.getProperty(InternalProperties.PORT_SPACING);
		final double deflectionDampening = layeredGraph.getProperty(
				Properties.LINEAR_SEGMENTS_DEFLECTION_DAMPENING).doubleValue();

		// Determine a suitable number of pendulum iterations
		final int thoroughness = layeredGraph.getProperty(Properties.THOROUGHNESS);
		int pendulumIters = PENDULUM_ITERS;
		int finalIters = FINAL_ITERS;
		final double threshold = THRESHOLD_FACTOR / thoroughness;

		// Iterate the balancing
		boolean ready = false;
		Mode mode = Mode.FORW_PENDULUM;
		double lastTotalDeflection = Integer.MAX_VALUE;
		do {

			// Calculate force for every linear segment
			final boolean incoming = mode != Mode.BACKW_PENDULUM;
			final boolean outgoing = mode != Mode.FORW_PENDULUM;
			double totalDeflection = 0;
			for (final LinearSegment segment : linearSegments) {
				segment.refSegment = null;
				calcDeflection(segment, incoming, outgoing, deflectionDampening);
				totalDeflection += Math.abs(segment.deflection);
			}

			// Merge linear segments to form regions
			boolean merged;
			do {
				merged = mergeRegions(layeredGraph, spacing, smallSpacing, externalPortSpacing);
			} while (merged);

			// Move the nodes according to the deflection value of their region
			for (final LinearSegment segment : linearSegments) {
				final double deflection = segment.region().deflection;
				if (deflection != 0) {
					for (final LNode node : segment.nodes) {
						node.getPosition().y += deflection;
					}
				}
			}

			// Update the balancing mode
			if ((mode == Mode.FORW_PENDULUM) || (mode == Mode.BACKW_PENDULUM)) {
				pendulumIters--;
				if ((pendulumIters <= 0)
						&& ((totalDeflection < lastTotalDeflection) || (-pendulumIters > thoroughness))) {
					mode = Mode.RUBBER;
					lastTotalDeflection = Integer.MAX_VALUE;
				} else if (mode == Mode.FORW_PENDULUM) {
					mode = Mode.BACKW_PENDULUM;
					lastTotalDeflection = totalDeflection;
				} else {
					mode = Mode.FORW_PENDULUM;
					lastTotalDeflection = totalDeflection;
				}
			} else {
				ready = (totalDeflection >= lastTotalDeflection)
						|| ((lastTotalDeflection - totalDeflection) < threshold);
				lastTotalDeflection = totalDeflection;
				if (ready) {
					finalIters--;
				}
			}
		} while (!(ready && (finalIters <= 0)));
	}

	/**
	 * Calculate the force acting on the given linear segment. The force is
	 * stored in the segment's deflection field.
	 * 
	 * @param segment
	 *            the linear segment whose force is to be calculated
	 * @param incoming
	 *            whether incoming edges should be considered
	 * @param outgoing
	 *            whether outgoing edges should be considered
	 * @param deflectionDampening
	 *            factor by which deflections are dampened
	 */
	private void calcDeflection(final LinearSegment segment, final boolean incoming,
			final boolean outgoing, final double deflectionDampening) {

		double segmentDeflection = 0;
		int nodeWeightSum = 0;
		for (final LNode node : segment.nodes) {
			double nodeDeflection = 0;
			int edgeWeightSum = 0;
			final int inputPrio = incoming ? node.getProperty(INPUT_PRIO) : Integer.MIN_VALUE;
			final int outputPrio = outgoing ? node.getProperty(OUTPUT_PRIO) : Integer.MIN_VALUE;
			final int minPrio = Math.max(inputPrio, outputPrio);

			// Calculate force for every port/edge
			for (final LPort port : node.getPorts()) {
				final double portpos = node.getPosition().y + port.getPosition().y
						+ port.getAnchor().y;
				if (outgoing) {
					for (final LEdge edge : port.getOutgoingEdges()) {
						final LPort otherPort = edge.getTarget();
						final LNode otherNode = otherPort.getNode();
						if (segment != linearSegments[otherNode.id]) {
							final int otherPrio = Math.max(otherNode.getProperty(INPUT_PRIO),
									otherNode.getProperty(OUTPUT_PRIO));
							final int prio = edge.getProperty(InternalProperties.PRIORITY);
							if ((prio >= minPrio) && (prio >= otherPrio)) {
								nodeDeflection += (otherNode.getPosition().y
										+ otherPort.getPosition().y + otherPort.getAnchor().y)
										- portpos;
								edgeWeightSum++;
							}
						}
					}
				}

				if (incoming) {
					for (final LEdge edge : port.getIncomingEdges()) {
						final LPort otherPort = edge.getSource();
						final LNode otherNode = otherPort.getNode();
						if (segment != linearSegments[otherNode.id]) {
							final int otherPrio = Math.max(otherNode.getProperty(INPUT_PRIO),
									otherNode.getProperty(OUTPUT_PRIO));
							final int prio = edge.getProperty(InternalProperties.PRIORITY);
							if ((prio >= minPrio) && (prio >= otherPrio)) {
								nodeDeflection += (otherNode.getPosition().y
										+ otherPort.getPosition().y + otherPort.getAnchor().y)
										- portpos;
								edgeWeightSum++;
							}
						}
					}
				}
			}

			// Avoid division by zero
			if (edgeWeightSum > 0) {
				segmentDeflection += nodeDeflection / edgeWeightSum;
				nodeWeightSum++;
			}
		}
		if (nodeWeightSum > 0) {
			segment.deflection = (deflectionDampening * segmentDeflection) / nodeWeightSum;
			segment.weight = nodeWeightSum;
		} else {
			segment.deflection = 0;
			segment.weight = 0;
		}
	}

	/** factor for threshold within which node overlapping is detected. */
	private static final double OVERLAP_DETECT = 0.01;

	/**
	 * Merge regions by testing whether they would overlap after applying the
	 * deflection.
	 * 
	 * @param layeredGraph
	 *            the layered graph
	 * @param normalSpacing
	 *            the normal object spacing
	 * @param smallSpacing
	 *            the dummy object spacing
	 * @param externalPortSpacing
	 *            the external port dummy spacing
	 * @return true if any two regions have been merged
	 */
	private boolean mergeRegions(final LGraph layeredGraph, final float normalSpacing,
			final float smallSpacing, final float externalPortSpacing) {

		boolean changed = false;
		final double threshold = OVERLAP_DETECT * normalSpacing;
		for (final Layer layer : layeredGraph) {
			final Iterator<LNode> nodeIter = layer.getNodes().iterator();

			// Get the first node
			LNode node1 = nodeIter.next();
			NodeType node1Type = node1.getNodeType();
			LinearSegment region1 = linearSegments[node1.id].region();

			// While there are still nodes following the current node
			while (nodeIter.hasNext()) {
				// Test whether nodes have different regions
				final LNode node2 = nodeIter.next();
				final NodeType node2Type = node2.getNodeType();
				final LinearSegment region2 = linearSegments[node2.id].region();

				if (region1 != region2) {
					// Calculate how much space is allowed between the nodes
					double spacing = smallSpacing;
					if ((node1Type == NodeType.NORMAL) || (node2Type == NodeType.NORMAL)) {

						spacing = normalSpacing;
					} else if ((node1Type == NodeType.EXTERNAL_PORT)
							&& (node2Type == NodeType.EXTERNAL_PORT)) {

						spacing = externalPortSpacing;
					}

					final double node1Extent = node1.getPosition().y + node1.getSize().y
							+ node1.getMargin().bottom + region1.deflection + spacing;
					final double node2Extent = (node2.getPosition().y - node2.getMargin().top)
							+ region2.deflection;

					// Test if the nodes are overlapping
					if (node1Extent > (node2Extent + threshold)) {
						// Merge the first region under the second top level
						// segment
						final int weightSum = region1.weight + region2.weight;
						assert weightSum > 0;
						region2.deflection = ((region2.weight * region2.deflection) + (region1.weight * region1.deflection))
								/ weightSum;
						region2.weight = weightSum;
						region1.refSegment = region2;
						changed = true;
					}
				}

				node1 = node2;
				region1 = region2;
				node1Type = node2Type;
			}
		}
		return changed;
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Post Processing for Correction

	/**
	 * Post-process the balanced placement by moving linear segments where
	 * obvious improvements can be made.
	 * 
	 * @param layeredGraph
	 *            the layered graph
	 */
	private void postProcess(final LGraph layeredGraph) {
		final float normalSpacing = layeredGraph.getProperty(InternalProperties.SPACING)
				* layeredGraph.getProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR);
		final float smallSpacing = normalSpacing
				* layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);

		// process each linear segment independently
		for (final LinearSegment segment : linearSegments) {
			double minRoomAbove = Integer.MAX_VALUE, minRoomBelow = Integer.MAX_VALUE;

			for (final LNode node : segment.nodes) {
				double roomAbove, roomBelow;
				final int index = node.getIndex();
				final boolean isNodeNormal = node.getNodeType() == NodeType.NORMAL;

				// determine the amount by which the linear segment can be moved
				// up without overlap
				if (index > 0) {
					final LNode neighbor = node.getLayer().getNodes().get(index - 1);
					final boolean isNeighborNormal = neighbor.getNodeType() == NodeType.NORMAL;
					final float spacing = isNodeNormal || isNeighborNormal ? normalSpacing
							: smallSpacing;
					roomAbove = node.getPosition().y
							- node.getMargin().top
							- (neighbor.getPosition().y + neighbor.getSize().y
									+ neighbor.getMargin().bottom + spacing);
				} else {
					roomAbove = node.getPosition().y - node.getMargin().top;
				}
				minRoomAbove = Math.min(roomAbove, minRoomAbove);

				// determine the amount by which the linear segment can be moved
				// down without
				// overlap
				if (index < (node.getLayer().getNodes().size() - 1)) {
					final LNode neighbor = node.getLayer().getNodes().get(index + 1);
					final boolean isNeighborNormal = neighbor.getNodeType() == NodeType.NORMAL;
					final float spacing = isNodeNormal || isNeighborNormal ? normalSpacing
							: smallSpacing;
					roomBelow = neighbor.getPosition().y
							- neighbor.getMargin().top
							- (node.getPosition().y + node.getSize().y + node.getMargin().bottom + spacing);
				} else {
					roomBelow = 2 * node.getPosition().y;
				}
				minRoomBelow = Math.min(roomBelow, minRoomBelow);
			}

			double minDisplacement = Integer.MAX_VALUE;
			boolean foundPlace = false;

			// determine the minimal displacement that would make one incoming
			// edge straight
			final LNode firstNode = segment.nodes.get(0);
			for (final LPort target : firstNode.getPorts()) {
				final double pos = firstNode.getPosition().y + target.getPosition().y
						+ target.getAnchor().y;
				for (final LEdge edge : target.getIncomingEdges()) {
					final LPort source = edge.getSource();
					final double d = (source.getNode().getPosition().y + source.getPosition().y + source
							.getAnchor().y) - pos;
					if ((Math.abs(d) < Math.abs(minDisplacement))
							&& (Math.abs(d) < (d < 0 ? minRoomAbove : minRoomBelow))) {
						minDisplacement = d;
						foundPlace = true;
					}
				}
			}

			// determine the minimal displacement that would make one outgoing
			// edge straight
			final LNode lastNode = segment.nodes.get(segment.nodes.size() - 1);
			for (final LPort source : lastNode.getPorts()) {
				final double pos = lastNode.getPosition().y + source.getPosition().y
						+ source.getAnchor().y;
				for (final LEdge edge : source.getOutgoingEdges()) {
					final LPort target = edge.getTarget();
					final double d = (target.getNode().getPosition().y + target.getPosition().y + target
							.getAnchor().y) - pos;
					if ((Math.abs(d) < Math.abs(minDisplacement))
							&& (Math.abs(d) < (d < 0 ? minRoomAbove : minRoomBelow))) {
						minDisplacement = d;
						foundPlace = true;
					}
				}
			}

			// if such a displacement could be found, apply it to the whole
			// linear segment
			if (foundPlace && (minDisplacement != 0)) {
				for (final LNode node : segment.nodes) {
					node.getPosition().y += minDisplacement;
				}
			}
		}
	}

}
