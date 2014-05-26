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
 * Layout option for the choice of candidates in the Brandes & Köpf node placement.
 *
 * @author jjc
 * @kieler.design proposed by cds
 * @kieler.rating proposed yellow by cds
 */
public enum FixedAlignment {
    
    /** Chooses the smallest layout from the four possible candidates. */
    NONE,
    /** Chooses the left-up candidate from the four possible candidates. */
    LEFTUP,
    /** Chooses the right-up candidate from the four possible candidates. */
    RIGHTUP,
    /** Chooses the left-down candidate from the four possible candidates. */
    LEFTDOWN,
    /** Chooses the right-down candidate from the four possible candidates. */
    RIGHTDOWN,
    /** Creates a balanced layout from the four possible candidates. */
    BALANCED;

}
