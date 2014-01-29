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
package de.cau.cs.kieler.klay.layered.properties;

/**
 * Definition of edge label placement strategies. The chosen strategy determines on which side
 * of an edge labels are placed.
 *
 * @author jjc
 */
public enum EdgeLabelSideSelection {
    
    /** Labels are always placed above their edges. */
    ALWAYS_UP,
    /** Labels are always placed below their edges. */
    ALWAYS_DOWN,
    /** Labels are always placed above their edges, with respect to the edge's direction. */
    DIRECTION_UP,
    /** Labels are always placed below their edges, with respect to the edge's direction. */
    DIRECTION_DOWN,
    /** A heuristic is used to determine the side. */
    SMART;
    
}
