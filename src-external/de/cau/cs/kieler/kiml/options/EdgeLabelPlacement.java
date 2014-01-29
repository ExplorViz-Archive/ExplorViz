/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2009 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.kiml.options;

/**
 * Definition of placement positions for edge labels. To be accessed using
 * {@link LayoutOptions#EDGE_LABEL_PLACEMENT}.
 * 
 * @kieler.design 2011-03-14 reviewed by cmot, cds
 * @kieler.rating yellow 2013-01-09 review KI-32 by ckru, chsch
 * @author msp
 */
public enum EdgeLabelPlacement {

    /** undefined label placement. */
    UNDEFINED,
    /** label is centered on the edge. */
    CENTER,
    /** label is at the head (target) of the edge. */
    HEAD,
    /** label is at the tail (source) of the edge. */
    TAIL;
    
}
