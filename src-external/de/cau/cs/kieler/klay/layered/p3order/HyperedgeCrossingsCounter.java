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
package de.cau.cs.kieler.klay.layered.p3order;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;

/**
 * Crossings counter implementation specialized for hyperedges. It also works for normal edges,
 * but is considerably slower compared to other implementations. For normal edges the computed
 * number of crossings is exact, while for hyperedges it is an estimation. In fact, it is
 * impossible to reliably count the number of crossings at this stage of the layer-based approach.
 * See the following publication for details.
 * <ul>
 *   <li>M. Sp&ouml;nemann, C. D. Schulze, U. R&uuml;egg, R. von Hanxleden.
 *     Counting crossings for layered hypergraphs, In <i>DIAGRAMS 2014</i>,
 *     volume 8578 of LNAI, Springer, 2014.</li>
 * </ul>
 * 
 * @author msp
 */
public class HyperedgeCrossingsCounter extends AbstractCrossingsCounter {

    /**
     * Port position array used for counting the number of edge crossings.
     */
    private final int[] portPos;

    /**
     * Create a hyperedge crossings counter.
     * 
     * @param inLayerEdgeCount
     *          The number of in-layer edges for each layer, including virtual connections to
     *          north/south dummies
     * @param hasNorthSouthPorts
     *          Whether the layers contain north / south port dummies or not
     * @param portPos
     *          Port position array used for counting the number of edge crossings
     */
    public HyperedgeCrossingsCounter(final int[] inLayerEdgeCount,
            final boolean[] hasNorthSouthPorts, final int[] portPos) {
        super(inLayerEdgeCount, hasNorthSouthPorts);
        this.portPos = portPos;
    }
    
    /**
     * Hyperedge representation.
     */
    private static class Hyperedge implements Comparable<Hyperedge> {
        private List<LEdge> edges = Lists.newArrayList();
        private List<LPort> ports = Lists.newArrayList();
        private int upperLeft;
        private int lowerLeft;
        private int upperRight;
        private int lowerRight;
        
        /**
         * {@inheritDoc}
         */
        public int compareTo(final Hyperedge other) {
            if (this.upperLeft < other.upperLeft) {
                return -1;
            } else if (this.upperLeft > other.upperLeft) {
                return 1;
            } else if (this.upperRight < other.upperRight) {
                return -1;
            } else if (this.upperRight > other.upperRight) {
                return 1;
            }
            return this.hashCode() - other.hashCode();
        }
    }
    
    /**
     * The upper left, lower left, upper right, or lower right corner of a hyperedge.
     */
    private static class HyperedgeCorner implements Comparable<HyperedgeCorner> {
        private Hyperedge hyperedge;
        private int position;
        private int oppositePosition;
        private Type type;
        
        HyperedgeCorner(final Hyperedge hyperedge, final int position,
                final int oppositePosition, final Type type) {
            this.hyperedge = hyperedge;
            this.position = position;
            this.oppositePosition = oppositePosition;
            this.type = type;
        }

        /** The corner type. */
        enum Type {
            UPPER, LOWER;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final HyperedgeCorner other) {
            if (this.position < other.position) {
                return -1;
            } else if (this.position > other.position) {
                return 1;
            } else if (this.oppositePosition < other.oppositePosition) {
                return -1;
            } else if (this.oppositePosition > other.oppositePosition) {
                return 1;
            } else if (this.hyperedge != other.hyperedge) {
                return this.hyperedge.hashCode() - other.hyperedge.hashCode();
            } else if (this.type == Type.UPPER && other.type == Type.LOWER) {
                return -1;
            } else if (this.type == Type.LOWER && other.type == Type.UPPER) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * Special crossings counting method for hyperedges. See
     * <ul>
     *   <li>M. Sp&ouml;nemann, C. D. Schulze, U. R&uuml;egg, R. von Hanxleden.
     *     Counting crossings for layered hypergraphs, In <i>DIAGRAMS 2014</i>,
     *     volume 8578 of LNAI, Springer, 2014.</li>
     * </ul>
     * 
     * @param leftLayer
     *            the left layer
     * @param rightLayer
     *            the right layer
     * @return the number of edge crossings
     */
    // SUPPRESS CHECKSTYLE NEXT 1 MethodLength
    @Override
    public int countCrossings(final NodeGroup[] leftLayer, final NodeGroup[] rightLayer) {
        // Assign index values to the ports of the left layer
        int sourceCount = 0;
        for (NodeGroup nodeGroup : leftLayer) {
            LNode node = nodeGroup.getNode();
            if (node.getProperty(LayoutOptions.PORT_CONSTRAINTS).isOrderFixed()) {
                // Assign index values in the order north - east - south - west
                for (LPort port : node.getPorts()) {
                    int portEdges = 0;
                    for (LEdge edge : port.getOutgoingEdges()) {
                        if (node.getLayer() != edge.getTarget().getNode().getLayer()) {
                            portEdges++;
                        }
                    }
                    if (portEdges > 0) {
                        portPos[port.id] = sourceCount++;
                    }
                }
                
            } else {
                // All ports are assigned the same index value, since their order does not matter
                int nodeEdges = 0;
                for (LPort port : node.getPorts()) {
                    for (LEdge edge : port.getOutgoingEdges()) {
                        if (node.getLayer() != edge.getTarget().getNode().getLayer()) {
                            nodeEdges++;
                        }
                    }
                    portPos[port.id] = sourceCount;
                }
                if (nodeEdges > 0) {
                    sourceCount++;
                }
            }
        }
        
        // Assign index values to the ports of the right layer
        int targetCount = 0;
        for (NodeGroup nodeGroup : rightLayer) {
            LNode node = nodeGroup.getNode();
            if (node.getProperty(LayoutOptions.PORT_CONSTRAINTS).isOrderFixed()) {
                // Determine how many input ports there are on the north side
                // (note that the standard port order is north - east - south - west)
                int northInputPorts = 0;
                for (LPort port : node.getPorts()) {
                    if (port.getSide() == PortSide.NORTH) {
                        for (LEdge edge : port.getIncomingEdges()) {
                            if (node.getLayer() != edge.getSource().getNode().getLayer()) {
                                northInputPorts++;
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                // Assign index values in the order north - west - south - east
                int otherInputPorts = 0;
                ListIterator<LPort> portIter = node.getPorts().listIterator(node.getPorts().size());
                while (portIter.hasPrevious()) {
                    LPort port = portIter.previous();
                    int portEdges = 0;
                    for (LEdge edge : port.getIncomingEdges()) {
                        if (node.getLayer() != edge.getSource().getNode().getLayer()) {
                            portEdges++;
                        }
                    }
                    if (portEdges > 0) {
                        if (port.getSide() == PortSide.NORTH) {
                            portPos[port.id] = targetCount;
                            targetCount++;
                        } else {
                            portPos[port.id] = targetCount + northInputPorts + otherInputPorts;
                            otherInputPorts++;
                        }
                    }
                }
                targetCount += otherInputPorts;
                
            } else {
                // All ports are assigned the same index value, since their order does not matter
                int nodeEdges = 0;
                for (LPort port : node.getPorts()) {
                    for (LEdge edge : port.getIncomingEdges()) {
                        if (node.getLayer() != edge.getSource().getNode().getLayer()) {
                            nodeEdges++;
                        }
                    }
                    portPos[port.id] = targetCount;
                }
                if (nodeEdges > 0) {
                    targetCount++;
                }
            }
        }
        
        // Gather hyperedges
        Map<LPort, Hyperedge> port2HyperedgeMap = Maps.newHashMap();
        Set<Hyperedge> hyperedgeSet = Sets.newLinkedHashSet();
        for (NodeGroup nodeGroup : leftLayer) {
            LNode node = nodeGroup.getNode();
            for (LPort sourcePort : node.getPorts()) {
                for (LEdge edge : sourcePort.getOutgoingEdges()) {
                    LPort targetPort = edge.getTarget();
                    if (node.getLayer() != targetPort.getNode().getLayer()) {
                        Hyperedge sourceHE = port2HyperedgeMap.get(sourcePort);
                        Hyperedge targetHE = port2HyperedgeMap.get(targetPort);
                        if (sourceHE == null && targetHE == null) {
                            Hyperedge hyperedge = new Hyperedge();
                            hyperedgeSet.add(hyperedge);
                            hyperedge.edges.add(edge);
                            hyperedge.ports.add(sourcePort);
                            port2HyperedgeMap.put(sourcePort, hyperedge);
                            hyperedge.ports.add(targetPort);
                            port2HyperedgeMap.put(targetPort, hyperedge);
                        } else if (sourceHE == null) {
                            targetHE.edges.add(edge);
                            targetHE.ports.add(sourcePort);
                            port2HyperedgeMap.put(sourcePort, targetHE);
                        } else if (targetHE == null) {
                            sourceHE.edges.add(edge);
                            sourceHE.ports.add(targetPort);
                            port2HyperedgeMap.put(targetPort, sourceHE);
                        } else if (sourceHE == targetHE) {
                            sourceHE.edges.add(edge);
                        } else {
                            sourceHE.edges.add(edge);
                            for (LPort p : targetHE.ports) {
                                port2HyperedgeMap.put(p, sourceHE);
                            }
                            sourceHE.edges.addAll(targetHE.edges);
                            sourceHE.ports.addAll(targetHE.ports);
                            hyperedgeSet.remove(targetHE);
                        }
                    }
                }
            }
        }
        
        // Determine top and bottom positions for each hyperedge
        Hyperedge[] hyperedges = hyperedgeSet.toArray(new Hyperedge[hyperedgeSet.size()]);
        Layer leftLayerRef = leftLayer[0].getNode().getLayer();
        Layer rightLayerRef = rightLayer[0].getNode().getLayer();
        for (Hyperedge he : hyperedges) {
            he.upperLeft = sourceCount;
            he.upperRight = targetCount;
            for (LPort port : he.ports) {
                int pos = portPos[port.id];
                if (port.getNode().getLayer() == leftLayerRef) {
                    if (pos < he.upperLeft) {
                        he.upperLeft = pos;
                    }
                    if (pos > he.lowerLeft) {
                        he.lowerLeft = pos;
                    }
                } else if (port.getNode().getLayer() == rightLayerRef) {
                    if (pos < he.upperRight) {
                        he.upperRight = pos;
                    }
                    if (pos > he.lowerRight) {
                        he.lowerRight = pos;
                    }
                }
            }
        }
        
        
        // Determine the sequence of edge target positions sorted by source and target index
        Arrays.sort(hyperedges);
        int[] southSequence = new int[hyperedges.length];
        int[] compressDeltas = new int[targetCount + 1];
        for (int i = 0; i < hyperedges.length; i++) {
            southSequence[i] = hyperedges[i].upperRight;
            compressDeltas[southSequence[i]] = 1;
        }
        int delta = 0;
        for (int i = 0; i < compressDeltas.length; i++) {
            if (compressDeltas[i] == 1) {
                compressDeltas[i] = delta;
            } else {
                delta--;
            }
        }
        int q = 0;
        for (int i = 0; i < southSequence.length; i++) {
            southSequence[i] += compressDeltas[southSequence[i]];
            q = Math.max(q, southSequence[i] + 1);
        }
        
        // Build the accumulator tree
        int firstIndex = 1;
        while (firstIndex < q) {
            firstIndex *= 2;
        }
        int treeSize = 2 * firstIndex - 1;
        firstIndex -= 1;
        int[] tree = new int[treeSize];

        // Count the straight-line crossings of the topmost edges
        int crossings = 0;
        for (int k = 0; k < southSequence.length; k++) {
            int index = southSequence[k] + firstIndex;
            tree[index]++;
            while (index > 0) {
                if (index % 2 > 0) {
                    crossings += tree[index + 1];
                }
                index = (index - 1) / 2;
                tree[index]++;
            }
        }
        
        
        // Create corners for the left side
        HyperedgeCorner[] leftCorners = new HyperedgeCorner[hyperedges.length * 2];
        for (int i = 0; i < hyperedges.length; i++) {
            leftCorners[2 * i] = new HyperedgeCorner(hyperedges[i], hyperedges[i].upperLeft,
                    hyperedges[i].lowerLeft, HyperedgeCorner.Type.UPPER);
            leftCorners[2 * i + 1] = new HyperedgeCorner(hyperedges[i], hyperedges[i].lowerLeft,
                    hyperedges[i].upperLeft, HyperedgeCorner.Type.LOWER);
        }
        Arrays.sort(leftCorners);
        
        // Count crossings caused by overlapping hyperedge areas on the left side
        int openHyperedges = 0;
        for (int i = 0; i < leftCorners.length; i++) {
            switch (leftCorners[i].type) {
            case UPPER:
                openHyperedges++;
                break;
            case LOWER:
                openHyperedges--;
                crossings += openHyperedges;
                break;
            }
        }
        
        
        // Create corners for the right side
        HyperedgeCorner[] rightCorners = new HyperedgeCorner[hyperedges.length * 2];
        for (int i = 0; i < hyperedges.length; i++) {
            rightCorners[2 * i] = new HyperedgeCorner(hyperedges[i], hyperedges[i].upperRight,
                    hyperedges[i].lowerRight, HyperedgeCorner.Type.UPPER);
            rightCorners[2 * i + 1] = new HyperedgeCorner(hyperedges[i], hyperedges[i].lowerRight,
                    hyperedges[i].upperRight, HyperedgeCorner.Type.LOWER);
        }
        Arrays.sort(rightCorners);
        
        // Count crossings caused by overlapping hyperedge areas on the right side
        openHyperedges = 0;
        for (int i = 0; i < rightCorners.length; i++) {
            switch (rightCorners[i].type) {
            case UPPER:
                openHyperedges++;
                break;
            case LOWER:
                openHyperedges--;
                crossings += openHyperedges;
                break;
            }
        }
        
        return crossings;
    }

}
