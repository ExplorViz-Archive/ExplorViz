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
 * Definition of node types used in the layered approach.
 * 
 * @author msp
 * @author cds
 * @author ima
 * @author jjc
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public enum NodeType {
    
    /** a normal node is created from a node of the original graph. */
    NORMAL,
    /** a dummy node created to split a long edge. */
    LONG_EDGE,
    /** a node representing an external port. */
    EXTERNAL_PORT,
    /** a dummy node created to cope with ports at the northern or southern side. */
    NORTH_SOUTH_PORT,
    /** a dummy node to represent a mid-label on an edge. */
    LABEL,
    /** a dummy node originating from a node spanning multiple layers. */
    BIG_NODE;
    
    /**
     * Return the color used when writing debug output graphs. The colors are given as strings of
     * the form "#RGB", where each component is given as a two-digit hexadecimal value.
     * 
     * @return the color string
     */
    public String getColor() {
        switch (this) {
        case BIG_NODE: return "#cccccc";
        case EXTERNAL_PORT: return "#cc99cc";
        case LONG_EDGE: return "#eaed00";
        case NORTH_SOUTH_PORT: return "#0034de";
        case LABEL: return "#75c3c3";
        default: return "#000000";
        }
    }

}
