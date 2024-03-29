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

import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.ILayoutPhaseFactory;

/**
 * Enumeration of and factory for the different available crossing minimization strategies.
 * 
 * @author msp
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public enum CrossingMinimizationStrategy implements ILayoutPhaseFactory {

    /**
     * A heuristic that sweeps through the layers trying to minimize the crossings locally.
     */
    LAYER_SWEEP,
    /**
     * Allow user interaction by considering the previous node positioning. The actual positions
     * as given in the input diagram are considered here. This means that if the user moves
     * a node, that movement is reflected in the ordering of nodes.
     */
    INTERACTIVE;
    

    /**
     * {@inheritDoc}
     */
    public ILayoutPhase create() {
        switch (this) {
        case LAYER_SWEEP:
            return new LayerSweepCrossingMinimizer();
            
        case INTERACTIVE:
            return new InteractiveCrossingMinimizer();
            
        default:
            throw new IllegalArgumentException(
                    "No implementation is available for the crossing minimizer " + this.toString());
        }
    }

}
