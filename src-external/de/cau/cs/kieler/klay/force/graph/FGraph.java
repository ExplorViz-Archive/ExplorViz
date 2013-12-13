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
package de.cau.cs.kieler.klay.force.graph;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterables;

import de.cau.cs.kieler.core.properties.MapPropertyHolder;
import de.cau.cs.kieler.klay.force.properties.Properties;

/**
 * A graph for the force layouter.
 * 
 * @author owo
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class FGraph extends MapPropertyHolder {
    
    /** the serial version UID. */
    private static final long serialVersionUID = -2396315570561498425L;
    
    /** All nodes of this graph. */
    private LinkedList<FNode> nodes = new LinkedList<FNode>();
    /** All edges of this graph. */
    private LinkedList<FEdge> edges = new LinkedList<FEdge>();
    /** All labels of this graph. */
    private LinkedList<FLabel> labels = new LinkedList<FLabel>();
    /** All bend points of this graph. */
    private LinkedList<FBendpoint> bendPoints = new LinkedList<FBendpoint>();
    /** adjacency matrix of the graph. */
    private int[][] adjacency;

    /**
     * Returns the list of edges for this graph.
     * 
     * @return the edges
     */
    public List<FEdge> getEdges() {
        return edges;
    }

    /**
     * Returns the list of nodes for this graph.
     * 
     * @return the nodes
     */
    public List<FNode> getNodes() {
        return nodes;
    }

    /**
     * Returns the list of labels for this graph.
     * 
     * @return the labels
     */
    public List<FLabel> getLabels() {
        return labels;
    }
    
    /**
     * Returns the list of bend points for this graph.
     * 
     * @return the bend points
     */
    public List<FBendpoint> getBendpoints() {
        return bendPoints;
    }
    
    /**
     * Returns a list of all particles occurring in the graph: vertices, labels, and bend points.
     * 
     * @return iterable over all particles
     */
    public Iterable<FParticle> getParticles() {
        return Iterables.concat(nodes, labels, bendPoints);
    }
    
    /**
     * Determines the amount of connection between the two given particles, considering
     * the priority value of the respective edges.
     * 
     * @param particle1 first particle
     * @param particle2 second particle
     * @return the amount of connection
     */
    public int getConnection(final FParticle particle1, final FParticle particle2) {
        if (particle1 instanceof FNode && particle2 instanceof FNode) {
            FNode node1 = (FNode) particle1, node2 = (FNode) particle2;
            return adjacency[node1.id][node2.id] + adjacency[node2.id][node1.id];
        } else if (particle1 instanceof FBendpoint && particle2 instanceof FBendpoint) {
            FBendpoint bpoint1 = (FBendpoint) particle1, bpoint2 = (FBendpoint) particle2;
            if (bpoint1.getEdge() == bpoint2.getEdge()) {
                return bpoint2.getEdge().getProperty(Properties.PRIORITY);
            }
        }
        return 0;
    }
    
    /**
     * Calculate the adjacency matrix of the graph.
     */
    public void calcAdjacency() {
        int n = nodes.size();
        adjacency = new int[n][n];
        for (FEdge edge : edges) {
            adjacency[edge.getSource().id][edge.getTarget().id]
                                           += edge.getProperty(Properties.PRIORITY);
        }
    }

}
