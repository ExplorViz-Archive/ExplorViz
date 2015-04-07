/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2015 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.kiml.options;

/**
 * Defines the distribution of ports.
 *
 * @author csp
 */
public enum PortAlignment {

    /** The alignment is not set. */
    UNDEFINED,

    /** Ports are evenly distributed. */
    JUSTIFIED,

    /** Ports are placed at the most top respectively left position with minimal spacing. */
    BEGIN,

    /** Ports are centered with minimal spacing. */
    CENTER,

    /** Ports are placed at the most top respectively left position with minimal spacing. */
    END;

}
