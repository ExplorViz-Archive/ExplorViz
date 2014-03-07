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
     * Calculates label sizes and node sizes also considering ports.
     * 
     * @see LabelAndNodeSizeProcessor
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

}
