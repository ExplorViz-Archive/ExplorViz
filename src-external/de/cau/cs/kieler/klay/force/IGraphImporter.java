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
package de.cau.cs.kieler.klay.force;

import de.cau.cs.kieler.klay.force.graph.FGraph;

/**
 * Interface for importer classes for the force graph structure.
 *
 * @param <T> the type of graph that this importer can transform into a force graph.
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public interface IGraphImporter<T> {
    
    /**
     * Create a force graph from the given graph.
     * 
     * @param graph the graph to turn into a force graph
     * @return a force graph, or {@code null} if the input was not recognized
     */
    FGraph importGraph(T graph);
    
    /**
     * Apply the computed layout of a force graph to the original graph.
     * 
     * @param forceGraph the graph for which layout is applied
     */
    void applyLayout(FGraph forceGraph);

}
