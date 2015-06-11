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

import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;

/**
 * Abstract super class for counting all between-layer edge crossings. Subclasses must implement
 * {@link #countCrossings(LNode[], LNode[])} which counts crossings between two layers. They can use
 * the {@code port.id} fields and the {@code portPos} array (accessible through {@link #getPortPos()}.
 * 
 * @author alan
 */
abstract class BetweenLayerEdgeAllCrossingsCounter {

    /**
     * Port position array used for counting the number of edge crossings.
     */
    private int[] portPos;

    public BetweenLayerEdgeAllCrossingsCounter(final LNode[][] graph) {
        initialize(graph);
    }

    /**
     * Calculate the number of crossings between the two given layers.
     *
     * @param leftLayer
     *            the left layer
     * @param rightLayer
     *            the right layer
     * @return the number of edge crossings
     */
    public abstract int countCrossings(LNode[] leftLayer, LNode[] rightLayer);

    private void initialize(final LNode[][] graph) {
        int portCount = 0;
        for (LNode[] layer : graph) {
            for (LNode node : layer) {
                for (LPort port : node.getPorts()) {
                    port.id = portCount++;
                }
            }
        }

        // Initialize the port positions and ranks arrays
        portPos = new int[portCount];
    }

    /**
     * @return the portPos
     */
    protected final int[] getPortPos() {
        return portPos;
    }
}
