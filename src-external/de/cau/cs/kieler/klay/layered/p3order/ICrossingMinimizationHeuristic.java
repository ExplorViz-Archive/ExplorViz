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
package de.cau.cs.kieler.klay.layered.p3order;

import java.util.List;

/**
 * Determines the node order of a given free layer. Uses heuristic methods to find an ordering that
 * minimizes edge crossings between the given free layer and a neighboring layer with fixed node
 * order. Given constraints are to be respected, possibly by the use of an {@link IConstraintResolver}.
 * 
 * @author cds
 * @author ima
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public interface ICrossingMinimizationHeuristic {

    /**
     * Minimize the number of crossings for the edges between the given layer and either its
     * predecessor or its successor. Resolve violated constraints.
     * 
     * @param layer
     *            the free layer whose nodes are reordered.
     * @param layerIndex
     *            the free layer's index.
     * @param preOrdered
     *            whether the nodes have been ordered in a previous run.
     * @param randomize
     *            {@code true} if this layer's node order should just be randomized. In that case,
     *            {@code preOrdered} is assumed to be {@code false} and the return value is
     *            {@code 0}.
     * @param forward
     *            whether the free layer is after the fixed layer.
     */
    void minimizeCrossings(List<NodeGroup> layer, int layerIndex, boolean preOrdered, boolean randomize,
            boolean forward);

}
