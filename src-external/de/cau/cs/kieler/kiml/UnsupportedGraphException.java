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
package de.cau.cs.kieler.kiml;

/**
 * Thrown when a layout algorithm is executed on a graph that is not supported.
 *
 * @author msp
 * @kieler.design 2011-03-14 reviewed by cmot, cds
 * @kieler.rating proposed yellow 2012-07-10 msp
 */
public class UnsupportedGraphException extends RuntimeException {

    /** the serial version UID. */
    private static final long serialVersionUID = 669762537737088914L;
    
    /**
     * Create an unsupported graph exception with no parameters.
     */
    public UnsupportedGraphException() {
        super();
    }
    
    /**
     * Create an unsupported graph exception with a message.
     * 
     * @param message a message
     */
    public UnsupportedGraphException(final String message) {
        super(message);
    }
    
    /**
     * Create an unsupported graph exception with a message and a cause.
     * 
     * @param message a message
     * @param cause a cause
     */
    public UnsupportedGraphException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
