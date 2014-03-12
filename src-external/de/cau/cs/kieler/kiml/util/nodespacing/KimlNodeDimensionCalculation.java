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
package de.cau.cs.kieler.kiml.util.nodespacing;

import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.GraphAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.NodeAdapter;

/**
 * Entry points to apply several methods for node dimension calculation, including positioning of
 * labels, ports, etc.
 * 
 * For convenience methods when using a KGraph, see {@link KimlNodeDimensionCalculationKGraph}.
 * 
 * @author uru
 */
public final class KimlNodeDimensionCalculation {

    /**
     * Private constructor - utility class.
     */
    private KimlNodeDimensionCalculation() {
    }

    /**
     * Calculates label sizes and node sizes also considering ports. Make sure that the port lists
     * are sorted properly.
     * 
     * @see LabelAndNodeSizeProcessor
     * @see #sortPortLists(GraphAdapter)
     * 
     * @param adapter
     *            an instance of an adapter for the passed graph's type.
     * @param <T>
     *            the graphs type, e.g. a root KNode
     */
    public static <T> void calculateLabelAndNodeSizes(final GraphAdapter<T> adapter) {
        LabelAndNodeSizeProcessor processor = new LabelAndNodeSizeProcessor();
        processor.process(adapter);
    }

    /**
     * Calculates node margins for the nodes of the passed graph.
     * 
     * @param adapter
     *            an instance of an adapter for the passed graph's type.
     * @param <T>
     *            the graphs type, e.g. a root KNode
     */
    public static <T> void calculateNodeMargins(final GraphAdapter<T> adapter) {
        NodeMarginCalculator calcu = new NodeMarginCalculator();
        calcu.processNodeMargin(adapter);
    }

    /**
     * Sorts the port lists of all nodes of the graph clockwise. More precisely, ports are sorted by
     * side (north, east, south, west) in clockwise order, beginning at the top left corner.
     * 
     * @param adapter
     *            an instance of an adapter for the passed graph's type.
     * @param <T>
     *            the graphs type, e.g. a root KNode
     */
    public static <T> void sortPortLists(final GraphAdapter<T> adapter) {
        // Iterate through the nodes of all layers
        for (NodeAdapter<?> node : adapter.getNodes()) {
            node.sortPortList();
        }
    }

}
