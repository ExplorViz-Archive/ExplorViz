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
package de.cau.cs.kieler.klay.layered;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.graph.LGraph;

/**
 * A layout processor processes a {@link de.cau.cs.kieler.klay.layered.graph.LGraph},
 * performing layout related tasks on it.
 *
 * @see LayeredLayoutProvider
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating yellow 2014-11-09 review KI-56 by chsch, als
 */
public interface ILayoutProcessor {
    
    /**
     * Performs the phase's work on the given graph.
     * 
     * @param layeredGraph a layered graph
     * @param progressMonitor a progress monitor to track algorithm execution
     */
    void process(LGraph layeredGraph, IKielerProgressMonitor progressMonitor);
    
}
