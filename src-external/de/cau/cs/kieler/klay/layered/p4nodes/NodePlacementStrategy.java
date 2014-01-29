/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2012 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.p4nodes;

/**
 * Definition of the available node placement strategies for the layered layout approach.
 *
 * @author jjc
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public enum NodePlacementStrategy {
    
    /**
     * Very simple and very fast node placement that centers all nodes vertically.
     */
    SIMPLE,
    /**
     * Node placement algorithm that aligns long edges using linear segments.
     * Nodes are aligned according to the <em>pendulum</em> method, which is similar to
     * the barycenter method for node ordering.
     */
    LINEAR_SEGMENTS,
    /**
     * Node placement which groups nodes to blocks which result in straight edges.
     */
    BRANDES_KOEPF;

}
