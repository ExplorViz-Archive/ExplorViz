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
package de.cau.cs.kieler.klay.layered.p1cycles;

import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.ILayoutPhaseFactory;

/**
 * Enumeration of and factory for the different available cycle breaking strategies.
 * 
 * @author msp
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating yellow 2012-11-13 review KI-33 by grh, akoc
 */
public enum CycleBreakingStrategy implements ILayoutPhaseFactory {

    /**
     * Applies a greedy heuristic to minimize the number of reversed edges.
     */
    GREEDY,
    /**
     * Reacts on user interaction by respecting initial node positions. The actual positions
     * as given in the input diagram are considered here. This means that if the user moves
     * a node, that movement is reflected in the decision which edges to reverse.
     */
    INTERACTIVE;
    

    /**
     * {@inheritDoc}
     */
    public ILayoutPhase create() {
        switch (this) {
        case GREEDY:
            return new GreedyCycleBreaker();
            
        case INTERACTIVE:
            return new InteractiveCycleBreaker();
            
        default:
            throw new IllegalArgumentException(
                    "No implementation is available for the cycle breaker " + this.toString());
        }
    }

}
