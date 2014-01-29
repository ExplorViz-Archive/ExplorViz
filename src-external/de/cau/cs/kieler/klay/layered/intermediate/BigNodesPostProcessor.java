/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2013 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * This class merges the series of big node dummy nodes introduced by the
 * {@link BigNodesPreProcessor} back into the original node. I.e., the original width is assigned to
 * the first node of the series, all other dummies are dropped. Furthermore, the EAST ports that were
 * moved to the last dummy node, are moved back to the original node. Here, the x coordinate of the
 * moved ports have to be adapted properly.
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>a graph with routed edges.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>all big node dummy nodes are removed from the graph.</dd>
 *   <dt>Slots:</dt>
 *     <dd>After phase 5.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>Before {@link LongEdgeJoiner}</dd>
 * </dl>
 * 
 * @see BigNodesPreProcessor
 * @see BigNodesIntermediateProcessor
 * 
 * @author uru
 */
public class BigNodesPostProcessor implements ILayoutProcessor {

    private LGraph layeredGraph;

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph theLayeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Big nodes post-processing", 1);

        this.layeredGraph = theLayeredGraph;

        for (Layer layer : layeredGraph) {

            // collect all starting big nodes
            Iterable<LNode> bigNodes = Iterables.filter(layer.getNodes(), new Predicate<LNode>() {
                public boolean apply(final LNode node) {
                    return isInitialBigNode(node);
                }
            });

            for (LNode node : bigNodes) {
                // set the original size
                Float originalSize = node.getProperty(Properties.BIG_NODE_ORIGINAL_SIZE);
                node.getSize().x = originalSize.doubleValue();

                // remove the dummy nodes
                LNode lastDummy = removeBigNodeChain(node);

                // move the east ports
                List<LPort> toMove = Lists.newLinkedList();
                for (LPort p : lastDummy.getPorts()) {
                    if (p.getSide() == PortSide.EAST) {
                        toMove.add(p);

                        // adjust position
                        p.getPosition().x = node.getSize().x;
                    }
                }

                for (LPort p : toMove) {
                    p.setNode(node);
                }

                // layers that contain only dummy nodes are considered to be of width 0. Hence it is
                // possible that a merged big node might exceed the calculated width of the layered
                // graph (during edge routing). In such a case we expand the bounding box here.
                if (layeredGraph.getSize().x < node.getPosition().x + node.getSize().x) {
                    layeredGraph.getSize().x = node.getPosition().x + node.getSize().x;
                }

            }

        }

        monitor.done();
    }

    /**
     * Remove the big node dummy nodes from the graph and return the last node from the chain.
     * 
     * @param start
     * @return
     */
    private LNode removeBigNodeChain(final LNode start) {

        List<LEdge> outs = Lists.newLinkedList(start.getOutgoingEdges());
        for (LEdge edge : outs) {
            LNode target = edge.getTarget().getNode();

            // only walk through intermediate big nodes
            if ((target.getProperty(Properties.NODE_TYPE) == NodeType.BIG_NODE)
                    && !isInitialBigNode(target)) {
                // remove the current dummy and its incoming edge
                target.getLayer().getNodes().remove(target);

                // remove the ports
                edge.getSource().setNode(null);
                edge.getTarget().setNode(null);

                // call recursively
                return removeBigNodeChain(target);
            } else {
                // this was the last node
                return start;
            }
        }

        // no final outgoing edge
        return start;
    }

    /**
     * @return true, if the {@link Properties#BIG_NODE_INITIAL} property is set and an
     *         {@link Properties#ORIGIN} is set.
     */
    private boolean isInitialBigNode(final LNode node) {
        return (node.getProperty(Properties.BIG_NODE_INITIAL))
                && (node.getProperty(Properties.ORIGIN) != null);
    }
}
