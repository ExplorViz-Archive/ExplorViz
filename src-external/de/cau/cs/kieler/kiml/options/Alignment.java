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
package de.cau.cs.kieler.kiml.options;

/**
 * Enumeration of node alignment options. To be accessed using {@link LayoutOptions#ALIGNMENT}.
 *
 * @author msp
 * @kieler.design 2011-03-14 reviewed by cmot, cds
 * @kieler.rating yellow 2013-01-09 review KI-32 by ckru, chsch
 */
public enum Alignment {
    
    /** automatic alignment (default). */
    AUTOMATIC,
    /** left alignment. */
    LEFT,
    /** right alignment. */
    RIGHT,
    /** top alignment. */
    TOP,
    /** bottom alignment. */
    BOTTOM,
    /** center alignment. */
    CENTER;

}
