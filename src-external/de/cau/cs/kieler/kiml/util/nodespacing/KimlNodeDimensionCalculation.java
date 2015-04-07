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
     * <p>
     *   If certain functionality has to be excluded, e.g., the sizes of ports should be excluded from
     *   the calculated margins, use {@link #getNodeMarginCalculator(GraphAdapter)} to retrieve an
     *   instance of a {@link NodeMarginCalculator} that can be configured further.
     * </p>
     * 
     * @param adapter
     *            an instance of an adapter for the passed graph's type.
     * @param <T>
     *            the graphs type, e.g. a root KNode
     */
    public static <T> void calculateNodeMargins(final GraphAdapter<T> adapter) {
        NodeMarginCalculator calcu = new NodeMarginCalculator(adapter);
        calcu.process();
    }

    /**
     * <p>
     *   Returns a configurable {@link NodeMarginCalculator} that can be executed using the
     *   {@link NodeMarginCalculator#process()} method.
     * </p>
     * 
     * <p>
     *   Note that {@link #calculateNodeMargins(GraphAdapter)} can be used if no detailed
     *   configuration is required.
     * </p>
     * 
     * @param adapter
     *            an instance of an adapter for the passed graph's type.
     * @param <T>
     *            the graphs type, e.g. a root KNode
     * @return an instance of a {@link NodeMarginCalculator} that can be configured to specific
     *         needs.
     */
    public static <T> NodeMarginCalculator getNodeMarginCalculator(final GraphAdapter<T> adapter) {
        return new NodeMarginCalculator(adapter);
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
