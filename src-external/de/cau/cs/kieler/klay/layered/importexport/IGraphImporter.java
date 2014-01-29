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
package de.cau.cs.kieler.klay.layered.importexport;

import de.cau.cs.kieler.klay.layered.graph.LGraph;

/**
 * Interface for importer classes for the layered graph structure. Graph importers
 * should usually subclass
 * {@link de.cau.cs.kieler.klay.layered.importexport.AbstractGraphImporter AbstractGraphImporter}
 * instead of implementing this interface directly.
 * 
 * <p>Graph importers are encouraged to set the {@link Properties#GRAPH_PROPERTIES}
 * property on imported graphs.</p>
 *
 * @param <T> the type of graph that this importer can transform into a layered graph.
 * @author msp
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public interface IGraphImporter<T> {
    
    /**
     * Create a layered graph from the given graph.
     * 
     * @param graph the graph to turn into a layered graph.
     * @return a layered graph, or {@code null} if the input was not recognized
     */
    LGraph importGraph(T graph);
    
    /**
     * Apply the computed layout of the given layered graph to the original input graph.
     * 
     * <dl>
     *   <dt>Precondition:</dt><dd>the graph has all its dummy nodes and edges removed;
     *     edges that were reversed during layout have been restored to their original
     *     orientation</dd>
     *   <dt>Postcondition:</dt><dd>none</dd>
     * </dl>
     * 
     * @param layeredGraph a graph for which layout is applied
     */
    void applyLayout(LGraph layeredGraph);

}
