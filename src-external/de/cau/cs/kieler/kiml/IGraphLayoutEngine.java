/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2008 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */

package de.cau.cs.kieler.kiml;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KNode;

/**
 * Interface for graph layout engines, which control the execution of layout algorithms
 * on a graph. This is used to execute different layout algorithms on different hierarchy
 * levels of a compound graph, or to delegate the layout execution to a remote service.
 * 
 * @author swe
 * @kieler.rating yellow 2012-08-10 review KI-23 by cds, sgu
 * @kieler.design proposed by msp
 */
public interface IGraphLayoutEngine {

    /**
     * Performs layout on the given layout graph.
     * 
     * @param layoutGraph the top-level node of the graph to be laid out
     * @param progressMonitor monitor to which progress of the layout algorithms is reported
     */
    void layout(KNode layoutGraph, IKielerProgressMonitor progressMonitor);
    
    /**
     * Determine whether the layout engine is active. Engines that delegate to remote services
     * may be inactive depending on user configuration and availability.
     * 
     * @return true if the engine is active
     */
    boolean isActive();

}
