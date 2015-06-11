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
package de.cau.cs.kieler.klay.layered.intermediate.greedyswitch;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;

/**
 * Counts crossings between in-layer edges incident to two nodes.
 * 
 * @author alan
 */
class InLayerEdgeTwoNodeCrossingCounter extends InLayerEdgeAllCrossingsCounter {

    private final List<ComparableEdgeAndPort> relevantEdgesAndPorts;
    private int upperLowerCrossings;
    private int lowerUpperCrossings;
    private LNode upperNode;
    private LNode lowerNode;

    /**
     * Counts crossings between in-layer edges incident to two nodes.
     * 
     * @param nodeOrder
     *            the current order of the layer to be counted in.
     */
    public InLayerEdgeTwoNodeCrossingCounter(final LNode[] nodeOrder) {
        super(nodeOrder);
        relevantEdgesAndPorts = Lists.newArrayList();
    }

    /**
     * Counts crossings between in-layer edges incident to the given nodes. Use
     * {@link #getUpperLowerCrossings()} and {@link #getLowerUpperCrossings()} to access the
     * calculated values.
     * 
     * @param upper
     * @param lower
     */
    public void countCrossingsBetweenNodes(final LNode upper, final LNode lower) {
        upperNode = upper;
        lowerNode = lower;

        upperLowerCrossings = countCrossingsOnSide(PortSide.EAST);
        upperLowerCrossings += countCrossingsOnSide(PortSide.WEST);

        notifyOfSwitch(upper, lower);

        lowerUpperCrossings = countCrossingsOnSide(PortSide.EAST);
        lowerUpperCrossings += countCrossingsOnSide(PortSide.WEST);

        notifyOfSwitch(lower, upper);
    }

    /**
     * This class simply collects all edges and ports connected to the two nodes in questions, sorts
     * them by port position and uses the superclass method countCrossingsOn(LEdge edge, LPort
     * port).
     * 
     */
    private int countCrossingsOnSide(final PortSide side) {
        relevantEdgesAndPorts.clear();

        addEdgesAndPortsConnectedToNodesAndSort(side);

        return iterateThroughRelevantEdgesAndPortsAndCountCrossings();
    }

    private void addEdgesAndPortsConnectedToNodesAndSort(final PortSide side) {
        iterateThroughEdgesAndCollectThem(upperNode, side);
        iterateThroughEdgesAndCollectThem(lowerNode, side);
        Collections.sort(relevantEdgesAndPorts);
    }

    private void iterateThroughEdgesAndCollectThem(final LNode node, final PortSide side) {
        Iterable<LPort> ports = PortIterable.inNorthSouthEastWestOrder(node, side);
        for (LPort port : ports) {
            for (LEdge edge : port.getConnectedEdges()) {
                if (!edge.isSelfLoop()) {
                    addThisEndOrBothEndsOfEdge(node, port, edge);
                }
            }
        }
    }

    private int iterateThroughRelevantEdgesAndPortsAndCountCrossings() {
        int crossings = 0;
        for (ComparableEdgeAndPort eP : relevantEdgesAndPorts) {
            crossings += super.countCrossingsOn(eP.edge, eP.port);
        }
        return crossings;
    }

    private void addThisEndOrBothEndsOfEdge(final LNode node, final LPort port, final LEdge edge) {
        relevantEdgesAndPorts.add(new ComparableEdgeAndPort(port, edge, positionOf(port)));

        if (isInLayer(edge) && notConnectedToOtherNode(edge, node)) {
            LPort otherEnd = otherEndOf(edge, port);
            relevantEdgesAndPorts.add(new ComparableEdgeAndPort(otherEnd, edge,
                    positionOf(otherEnd)));
        }
    }

    private boolean notConnectedToOtherNode(final LEdge edge, final LNode node) {
        if (node.equals(upperNode)) {
            return !edge.getTarget().getNode().equals(lowerNode)
                    && !edge.getSource().getNode().equals(lowerNode);
        } else {
            return !edge.getTarget().getNode().equals(upperNode)
                    && !edge.getSource().getNode().equals(upperNode);
        }
    }

    /**
     * This private class collects a port and a connected edge and can be sorted by portPosition.
     * 
     * @author alan
     *
     */
    private static class ComparableEdgeAndPort implements Comparable<ComparableEdgeAndPort> {
        /** The port. */
        private final LPort port;
        /** The edge connected to it. */
        private final LEdge edge;
        /** The position of the port. */
        private final int portPosition;

        public ComparableEdgeAndPort(final LPort port, final LEdge edge, final int portPosition) {
            this.port = port;
            this.edge = edge;
            this.portPosition = portPosition;
        }

        public int compareTo(final ComparableEdgeAndPort o) {
            return (portPosition < o.portPosition) ? -1 : ((portPosition == o.portPosition) ? 0 : 1);
        }

        @Override
        public String toString() {
            return "ComparableEdgeAndPort [port=" + port + ", edge=" + edge + ", portPosition="
                    + portPosition + "]";
        }

    }

    public int getUpperLowerCrossings() {
        return upperLowerCrossings;
    }

    public int getLowerUpperCrossings() {
        return lowerUpperCrossings;
    }

    private LPort otherEndOf(final LEdge edge, final LPort fromPort) {
        return fromPort == edge.getSource() ? edge.getTarget() : edge.getSource();
    }

}
