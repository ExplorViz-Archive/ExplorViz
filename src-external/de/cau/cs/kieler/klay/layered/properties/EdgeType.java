/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2011 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.properties;

/**
 * Definition of edge types used in the layered approach.
 * 
 * @author ima
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public enum EdgeType {
    
    /**
     * a normal edge is created from an edge of the original graph.
     */
    NORMAL,
    /**
     * a dummy edge created for the layering phase of compound graphs.
     */
    COMPOUND_DUMMY,
    /**
     * a dummy edge created to connect COMPOUND_SIDE dummy nodes for the drawing of compound node
     * side borders.
     */
    COMPOUND_SIDE;
    
}
