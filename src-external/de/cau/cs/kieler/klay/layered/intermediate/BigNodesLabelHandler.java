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
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;

import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.NodeLabelPlacement;
import de.cau.cs.kieler.klay.layered.graph.*;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;

/**
 * Utility class that contains methods to split and distribute long labels on
 * {@link de.cau.cs.kieler.klay.layered.properties.NodeType.BIG_NODE}s.
 *
 * @author uru
 */
public final class BigNodesLabelHandler {

	/**
	 * Utility class.
	 */
	private BigNodesLabelHandler() {
	}

	/**
	 * Distribute, if necessary, the labels of the passed node.
	 * 
	 * @param node
	 *            the original node
	 * @param dummies
	 *            the list of the dummy nodes to which to distribute the label.
	 *            Note that the list should <b>include</b> the initial node.
	 * @param chunkWidth
	 *            the width of each dummy node
	 */
	public static void handle(final LNode node, final List<LNode> dummies, final double chunkWidth) {
		new Handler(node, dummies, chunkWidth);
	}

	/**
	 * Internal class storing a state for each node and its labels.
	 */
	private static final class Handler {

		private final LGraph layeredGraph;

		private final LNode node;
		private final int chunks;
		private final double minWidth;

		/**
		 * The dummy nodes created for this big node (include the node itself at
		 * index 0).
		 */
		private ArrayList<LNode> dummies;
		/** Last dummy node, stored for quick access. */
		private LNode lastDummy = null;

		/**
		 * A multimap holding the dummies created for a label. It is important
		 * to use a LinkedListMultimap in order to retain the order of the
		 * dummies.
		 */
		private final LinkedListMultimap<LLabel, LLabel> dumLabs = LinkedListMultimap.create();

		/**
		 * A list of post processing functions to be applied during
		 * {@link BigNodesPostProcessor}.
		 */
		private final List<Function<Void, Void>> postProcs = Lists.newLinkedList();

		/**
		 * 
		 * @param node
		 *            the original node
		 * @param dummies
		 *            the created dummy nodes
		 */
		private Handler(final LNode node, final List<LNode> dummies, final double chunkWidth) {

			layeredGraph = node.getGraph();
			this.node = node;
			chunks = dummies.size();

			// we require fast access to the elements
			if (dummies instanceof ArrayList<?>) {
				this.dummies = (ArrayList<LNode>) dummies;
			} else {
				this.dummies = Lists.newArrayList(dummies);
			}

			lastDummy = this.dummies.get(this.dummies.size() - 1);
			minWidth = chunkWidth;

			handleLabels();
		}

		/**
		 * Labels require special treatment depending on their value of
		 * {@link NodeLabelPlacement}.
		 */
		public void handleLabels() {

			// remember the original labels, they will be restored during post
			// processing
			// otherwise the graph exporter is not able to write back the new
			// label positions
			node.setProperty(InternalProperties.BIGNODES_ORIG_LABELS,
					Lists.newLinkedList(node.getLabels()));

			// assign V_CENTER, H_CENTER node placement to all middle dummy node
			// this is necessary to avoid unnecessary spacing to be introduced
			// due to
			// outside label placement
			for (int i = 1; i < (dummies.size() - 1); ++i) {
				dummies.get(i).setProperty(LayoutOptions.NODE_LABEL_PLACEMENT,
						NodeLabelPlacement.insideCenter());
			}

			// handle every label of the node
			for (final LLabel l : Lists.newLinkedList(node.getLabels())) {

				final EnumSet<NodeLabelPlacement> placement = node
						.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT);

				// we handle two cases differently where labels are placed
				// outside, horizontally
				// left or right and vertically centered
				// apart from that split the label and distribute it among the
				// dummy nodes

				// CHECKSTYLEOFF EmptyBlock
				if (placement.containsAll(EnumSet.of(NodeLabelPlacement.OUTSIDE,
						NodeLabelPlacement.H_LEFT, NodeLabelPlacement.V_CENTER))) {
					// leave the label unsplit on the left most node
					// spacing will be introduced on the left side of
					// the first big node dummy

				} else if (placement.containsAll(EnumSet.of(NodeLabelPlacement.OUTSIDE,
						NodeLabelPlacement.H_RIGHT, NodeLabelPlacement.V_CENTER))) {

					// assign to last node
					// spacing will be introduced on the right side of
					// the last dummy node
					lastDummy.getLabels().add(l);
					node.getLabels().remove(l);

					// however, the label's position is calculated relatively to
					// the
					// last dummy, thus we have to add an offset afterwards
					final Function<Void, Void> postProcess = new Function<Void, Void>() {
						public Void apply(final Void v) {
							l.getPosition().x += (minWidth * (chunks - 1));
							return null;
						}
					};
					node.setProperty(InternalProperties.BIGNODES_POST_PROCESS, postProcess);

				} else {
					// this case includes NO placement data at all

					splitAndDistributeLabel(l);

					// post processing is generated within the method above
					postProcs.add(funRemoveLabelDummies);
					node.setProperty(InternalProperties.BIGNODES_POST_PROCESS,
							CompoundFunction.of(postProcs));
				}
			}
		}

		/**
		 * Splits the label in consecutive chunks while the number of chunks
		 * corresponds to the number of dummy nodes (including the first node).
		 * 
		 */
		private void splitAndDistributeLabel(final LLabel lab) {

			// double labelChunkWidth = lab.getSize().x / chunks;

			// split into equal sized chunks
			final int length = lab.getText().length();
			final int labelChunkSize = (int) Math.ceil(length / (double) chunks);

			final String text = lab.getText();
			int lPos = 0, rPos = labelChunkSize;

			for (int i = 0; i < chunks; ++i) {
				final String subLabel = text.substring(Math.min(Math.max(0, lPos), length),
						Math.max(0, Math.min(rPos, length)));
				lPos = rPos;
				rPos += labelChunkSize;

				final LNode dummy = dummies.get(i);
				final LLabel dumLab = new LLabel(layeredGraph, subLabel);
				// TODO as soon as SizeConstraints are to be supported this
				// should be used
				// dumLab.getSize().x = labelChunkWidth;
				dumLab.getSize().y = lab.getSize().y;
				dumLabs.put(lab, dumLab);

				dummy.getLabels().add(dumLab);
			}
			// remove original label
			node.getLabels().remove(lab);

			postProcs.add(getPostProcFunctionForLabel(lab));
		}

		/**
		 * Creates a function that will be executed during the
		 * {@link BigNodesPostProcessor}.
		 * 
		 * The position of the split label has to be adapted depending on the
		 * specified node label placement.
		 */
		private Function<Void, Void> getPostProcFunctionForLabel(final LLabel label) {

			final Function<Void, Void> fun = new Function<Void, Void>() {

				public Void apply(final Void v) {
					final EnumSet<NodeLabelPlacement> placement = node
							.getProperty(LayoutOptions.NODE_LABEL_PLACEMENT);

					// CHECKSTYLEOFF EmptyBlock
					if (placement.equals(NodeLabelPlacement.fixed())) {
						// FIXED label positions
						// leave as they are

					} else if (placement.containsAll(EnumSet.of(NodeLabelPlacement.H_LEFT))) {
						// INSIDE || OUTSIDE
						// H_LEFT
						final LLabel dumLab = dumLabs.get(label).get(0);
						label.getPosition().x = dumLab.getPosition().x;
						label.getPosition().y = dumLab.getPosition().y;

					} else if (placement.containsAll(EnumSet.of(NodeLabelPlacement.H_RIGHT))) {
						// INSIDE || OUTSIDE
						// H_RIGHT
						final LNode rightMostDum = dummies.get(dummies.size() - 1);
						final LLabel rightMostLab = dumLabs.get(label).get(
								dumLabs.get(label).size() - 1);

						// get offset on the right side
						final double rightOffset = (rightMostDum.getSize().x)
								- (rightMostLab.getPosition().x + rightMostLab.getSize().x);

						// now get the offset on the left side and use it as the
						// label's position
						label.getPosition().x = node.getSize().x - rightOffset - label.getSize().x;
						label.getPosition().y = rightMostLab.getPosition().y;

					} else if (placement.containsAll(EnumSet.of(NodeLabelPlacement.V_CENTER,
							NodeLabelPlacement.H_CENTER))) {
						// V_CENTER && H_CENTER

						// use any calculated y pos, center manually for x
						final LLabel dumLab = dumLabs.get(label).get(0);

						// now get the offset on the left side and use it as the
						// label's position
						label.getPosition().x = (node.getSize().x - label.getSize().x) / 2f;
						label.getPosition().y = dumLab.getPosition().y;

					} else if (placement.containsAll(EnumSet.of(NodeLabelPlacement.V_CENTER))) {

						final LLabel dumLab = dumLabs.get(label).get(0);
						label.getPosition().y = dumLab.getPosition().y;

					} else if (placement.containsAll(EnumSet.of(NodeLabelPlacement.H_CENTER))) {

						final LLabel dumLab = dumLabs.get(label).get(0);
						label.getPosition().x = (node.getSize().x - label.getSize().x) / 2f;
						label.getPosition().y = dumLab.getPosition().y;
					}

					return null;
				}
			};

			return fun;
		}

		/**
		 * Post processing function removing all created label dummies, i.e
		 * labels that do not have an {@link Properties#ORIGIN}.
		 */
		private final Function<Void, Void> funRemoveLabelDummies = new Function<Void, Void>() {

			public Void apply(final Void v) {
				// remove all the dummies again
				for (final LNode dummy : dummies) {
					for (final LLabel l : Lists.newLinkedList(dummy.getLabels())) {
						if (l.getProperty(InternalProperties.ORIGIN) == null) {
							dummy.getLabels().remove(l);
						}
					}
				}
				return null;
			}
		};

	}

	/**
	 * Class to combine and execution multiple {@link Function} instances.
	 * 
	 * @author uru
	 */
	private static final class CompoundFunction implements Function<Void, Void> {
		private final Function<Void, Void>[] funs;

		private CompoundFunction(@SuppressWarnings("unchecked") final Function<Void, Void>... funs) {
			this.funs = funs;
		}

		public static CompoundFunction of(final List<Function<Void, Void>> funs) {
			@SuppressWarnings("unchecked")
			final Function<Void, Void>[] funsArr = new Function[funs.size()];
			int i = 0;
			for (final Function<Void, Void> f : funs) {
				funsArr[i++] = f;
			}
			return new CompoundFunction(funsArr);
		}

		public Void apply(final Void v) {
			for (final Function<Void, Void> f : funs) {
				f.apply(null);
			}
			return null;
		}
	}

}
