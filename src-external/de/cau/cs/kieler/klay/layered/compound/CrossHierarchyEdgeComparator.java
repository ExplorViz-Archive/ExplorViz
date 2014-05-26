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
package de.cau.cs.kieler.klay.layered.compound;

import java.util.Comparator;

import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.PortType;

/**
 * Compares cross-hierarchy edge segments such that they can be sorted from the start to the end
 * segment.
 * 
 * @author msp
 * @kieler.design proposed by cds
 * @kieler.rating proposed yellow by cds
 */
final class CrossHierarchyEdgeComparator implements Comparator<CrossHierarchyEdge> {
    private final LGraph graph;
    
    /**
     * Creates a new comparator for sorting cross-hierarchy edge segments for the given top-level
     * compound graph.
     * 
     * @param graph the top-level compound graph.
     */
    CrossHierarchyEdgeComparator(final LGraph graph) {
        this.graph = graph;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compare(final CrossHierarchyEdge edge1, final CrossHierarchyEdge edge2) {
        if (edge1.getType() == PortType.OUTPUT
                && edge2.getType() == PortType.INPUT) {
            return -1;
        } else if (edge1.getType() == PortType.INPUT
                && edge2.getType() == PortType.OUTPUT) {
            return 1;
        }
        int level1 = hierarchyLevel(edge1.getGraph(), graph);
        int level2 = hierarchyLevel(edge2.getGraph(), graph);
        if (edge1.getType() == PortType.OUTPUT) {
            // from deeper level to higher level
            return level2 - level1;
        } else {
            // from higher level to deeper level
            return level1 - level2;
        }
    }
    
    /**
     * Compute the hierarchy level of the given nested graph.
     * 
     * @param nestedGraph a nested graph
     * @param topLevelGraph the top-level graph
     * @return the hierarchy level (higher number means the node is nested deeper)
     */
    private static int hierarchyLevel(final LGraph nestedGraph, final LGraph topLevelGraph) {
        LGraph currentGraph = nestedGraph;
        int level = 0;
        do {
            if (currentGraph == topLevelGraph) {
                return level;
            }
            LNode currentNode = currentGraph.getProperty(InternalProperties.PARENT_LNODE);
            if (currentNode == null) {
                // the given node is not an ancestor of the graph node
                throw new IllegalArgumentException();
            }
            currentGraph = currentNode.getGraph();
            level++;
        } while (true);
    }
}