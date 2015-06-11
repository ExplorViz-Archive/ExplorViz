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
package de.cau.cs.kieler.klay.layered.intermediate.greedyswitch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LNode.NodeType;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;

/**
 * Counts the crossings caused by the order of north south port dummies when their respective normal
 * node in the same layer has a fixed port order. Also counts crossings between north south edges
 * and long edge dummies.
 * 
 * @author alan
 */
public class NorthSouthEdgeNeighbouringNodeCrossingsCounter {

    private int upperLowerCrossings;
    private int lowerUpperCrossings;
    private final Map<LPort, Integer> portPositions;
    private final LNode[] layer;

    /**
     * Creates a counter for north south port crossings.
     * 
     * @param nodes
     *            the order of nodes in the layer in question.
     */
    public NorthSouthEdgeNeighbouringNodeCrossingsCounter(final LNode[] nodes) {
        layer = nodes;
        portPositions = new HashMap<LPort, Integer>();
        initializePortPositions();
    }

    /**
     * Since accessing the index of a port is linear, the positions are saved. To prevent dependency
     * problems, they are saved in a field of this Object. Ports are numbered as they are in the
     * list returned by getPorts().
     */
    private void initializePortPositions() {
        for (LNode node : layer) {
            setPortIdsOn(node, PortSide.SOUTH);
            setPortIdsOn(node, PortSide.NORTH);
        }
    }

    private void setPortIdsOn(final LNode node, final PortSide side) {
        Iterable<LPort> ports = PortIterable.inNorthSouthEastWestOrder(node, side);
        int portId = 0;
        for (LPort port : ports) {
            portPositions.put(port, portId++);
        }
    }

    /**
     * Counts north south port crossings and crossings between north south ports and dummy nodes,
     * for uppperNode and lowerNode.
     * 
     * @param upperNode
     *            The first node.
     * @param lowerNode
     *            The second node.
     */
    public void countCrossings(final LNode upperNode, final LNode lowerNode) {
        upperLowerCrossings = 0;
        lowerUpperCrossings = 0;

        processIfTwoNorthSouthNodes(upperNode, lowerNode);

        processIfNorthSouthLongEdgeDummyCrossing(upperNode, lowerNode);

        processIfNormalNodeWithNSPortsAndLongEdgeDummy(upperNode, lowerNode);
    }

    private void processIfTwoNorthSouthNodes(final LNode upperNode, final LNode lowerNode) {
        if (isNorthSouth(upperNode) && isNorthSouth(lowerNode)) {
            if (noFixedPortOrderOn(originOf(upperNode))
                    || haveDifferentOrigins(upperNode, lowerNode)) {
                return;
            }
            PortSide upperNodePortSide = getPortDirectionFromNorthSouthNode(upperNode);
            PortSide lowerNodePortSide = getPortDirectionFromNorthSouthNode(lowerNode);
            if (isNorthOfNormalNode(upperNode)) {
                countCrossingsOfTwoNorthSouthDummies(upperNode, lowerNode, upperNodePortSide,
                        lowerNodePortSide);
            } else {
                countCrossingsOfTwoNorthSouthDummies(lowerNode, upperNode, lowerNodePortSide,
                        upperNodePortSide);
            }
        }
    }

    private void countCrossingsOfTwoNorthSouthDummies(final LNode furtherFromNormalNode,
            final LNode closerToNormalNode, final PortSide furtherNodePortSide,
            final PortSide closerNodePortSide) {
        
        if (furtherNodePortSide == PortSide.EAST && closerNodePortSide == PortSide.EAST) {
            if (originPortPositionOf(furtherFromNormalNode) > originPortPositionOf(closerToNormalNode)) {
                upperLowerCrossings = 1;
            } else {
                lowerUpperCrossings = 1;
            }
        } else if (furtherNodePortSide == PortSide.WEST && closerNodePortSide == PortSide.WEST) {
            if (originPortPositionOf(furtherFromNormalNode) < originPortPositionOf(closerToNormalNode)) {
                upperLowerCrossings = 1;
            } else {
                lowerUpperCrossings = 1;
            }
        } else if (furtherNodePortSide == PortSide.WEST && closerNodePortSide == PortSide.EAST) {
            if (originPortPositionOf(furtherFromNormalNode) > originPortPositionOf(closerToNormalNode)) {
                upperLowerCrossings = 1;
                lowerUpperCrossings = 1;
            }
        } else {
            if (originPortPositionOf(furtherFromNormalNode) < originPortPositionOf(closerToNormalNode)) {
                upperLowerCrossings = 1;
                lowerUpperCrossings = 1;
            }
        }
    }

    private void processIfNorthSouthLongEdgeDummyCrossing(final LNode upperNode,
            final LNode lowerNode) {
        
        if (isNorthSouth(upperNode) && isLongEdgeDummy(lowerNode)) {
            if (isNorthOfNormalNode(upperNode)) {
                upperLowerCrossings = 1;
            } else {
                lowerUpperCrossings = 1;
            }
        } else if (isNorthSouth(lowerNode) && isLongEdgeDummy(upperNode)) {
            if (isNorthOfNormalNode(lowerNode)) {
                lowerUpperCrossings = 1;
            } else {
                upperLowerCrossings = 1;
            }
        }
    }

    private void processIfNormalNodeWithNSPortsAndLongEdgeDummy(final LNode upperNode,
            final LNode lowerNode) {
        
        if (isNormal(upperNode) && isLongEdgeDummy(lowerNode)) {
            upperLowerCrossings =
                    numberOfDummyEdgeCrossingsWithNSEdgesOnSide(upperNode, PortSide.SOUTH);
            lowerUpperCrossings =
                    numberOfDummyEdgeCrossingsWithNSEdgesOnSide(upperNode, PortSide.NORTH);
        }
        if (isNormal(lowerNode) && isLongEdgeDummy(upperNode)) {
            upperLowerCrossings =
                    numberOfDummyEdgeCrossingsWithNSEdgesOnSide(lowerNode, PortSide.NORTH);
            lowerUpperCrossings =
                    numberOfDummyEdgeCrossingsWithNSEdgesOnSide(lowerNode, PortSide.SOUTH);
        }
    }

    private int numberOfDummyEdgeCrossingsWithNSEdgesOnSide(final LNode node, final PortSide side) {
        int numberOfPorts = 0;
        for (LPort port : node.getPorts(side)) {
            List<LPort> nsPorts =
                    port.getProperty(InternalProperties.CONNECTED_NORTH_SOUTH_PORT_DUMMIES);
            numberOfPorts += nsPorts.size();
        }
        return numberOfPorts;
    }

    private boolean haveDifferentOrigins(final LNode upperNode, final LNode lowerNode) {
        return originOf(upperNode) != originOf(lowerNode);
    }

    private PortSide getPortDirectionFromNorthSouthNode(final LNode node) {
        assert isNorthSouth(node);
        boolean northSouthNodeOnlyHasOneInBetweenLayerEdge = node.getPorts().size() == 1;
        assert northSouthNodeOnlyHasOneInBetweenLayerEdge;
        return node.getPorts().get(0).getSide();
    }

    private int originPortPositionOf(final LNode node) {
        LPort origin = originPortOf(node);
        final LPort port = origin;
        return portPositions.get(port);
    }

    private LPort originPortOf(final LNode node) {
        LPort port = node.getPorts().get(0);
        LPort origin = (LPort) port.getProperty(InternalProperties.ORIGIN);
        return origin;
    }

    private boolean isNorthOfNormalNode(final LNode upperNode) {
        return originPortOf(upperNode).getSide() == PortSide.NORTH;
    }

    private LNode originOf(final LNode node) {
        return (LNode) node.getProperty(InternalProperties.ORIGIN);
    }

    private boolean noFixedPortOrderOn(final LNode node) {
        return !node.getProperty(LayoutOptions.PORT_CONSTRAINTS).isOrderFixed();
    }

    private boolean isLongEdgeDummy(final LNode node) {
        return node.getNodeType() == NodeType.LONG_EDGE;
    }

    private boolean isNorthSouth(final LNode node) {
        return node.getNodeType() == NodeType.NORTH_SOUTH_PORT;
    }

    private boolean isNormal(final LNode node) {
        return node.getNodeType() == NodeType.NORMAL;
    }

    /**
     * Get crossing count.
     * 
     * @return the crossings between when ordered upper - lower.
     */
    public int getUpperLowerCrossings() {
        return upperLowerCrossings;
    }

    /**
     * 
     * Get crossing count.
     * 
     * @return the crossings between when ordered lower - upper.
     */
    public int getLowerUpperCrossings() {
        return lowerUpperCrossings;
    }

}
