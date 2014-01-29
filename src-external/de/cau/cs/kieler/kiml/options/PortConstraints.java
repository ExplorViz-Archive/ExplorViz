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
 * Definition of port constraints. To be accessed using {@link LayoutOptions#PORT_CONSTRAINTS}.
 * 
 * @kieler.design 2011-03-14 reviewed by cmot, cds
 * @kieler.rating yellow 2013-01-09 review KI-32 by ckru, chsch
 * @author msp
 */
public enum PortConstraints {

    /** Undefined constraints. */
    UNDEFINED,
    /** All ports are free. */
    FREE,
    /** The side is fixed for each port. */
    FIXED_SIDE,
    /** The side is fixed for each port, and the order of ports is fixed for each side. */
    FIXED_ORDER,
    /**
     * The side is fixed for each port, the order or ports is fixed for each side and
     * the relative position of each port must be preserved. That means if the node is
     * resized by factor x, the port's position must also be scaled by x.
     */
    FIXED_RATIO,
    /** The exact position is fixed for each port. */
    FIXED_POS;
    
    
    /**
     * Returns whether the position of the ports is fixed. Note that this is not true
     * if port ratios are fixed.
     * 
     * @return true if the position is fixed
     */
    public boolean isPosFixed() {
        return this == FIXED_POS;
    }
    
    /**
     * Returns whether the ratio of port positions is fixed. Note that this is not true
     * if the port positions are fixed.
     * 
     * @return true if the ratio is fixed
     */
    public boolean isRatioFixed() {
        return this == FIXED_RATIO;
    }
    
    /**
     * Returns whether the order of ports is fixed.
     * 
     * @return true if the order of ports is fixed
     */
    public boolean isOrderFixed() {
        return this == FIXED_ORDER || this == FIXED_RATIO || this == FIXED_POS;
    }
    
    /**
     * Returns whether the sides of ports are fixed.
     * 
     * @see PortSide
     * @return true if the port sides are fixed
     */
    public boolean isSideFixed() {
        return this != FREE && this != UNDEFINED;
    }
    
}
