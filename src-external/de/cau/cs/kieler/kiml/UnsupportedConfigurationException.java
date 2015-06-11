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
 * Thrown when a layout algorithm is executed on a graph that has properties set on it that are not
 * supported by the algorithm.
 *
 * @author cds
 * @kieler.design 2013-05-22 proposed by cds
 * @kieler.rating proposed yellow 2013-05-22 cds
 */
public class UnsupportedConfigurationException extends RuntimeException {

    /** the serial version UID. */
    private static final long serialVersionUID = -3617468773969103109L;

    
    /**
     * Create an unsupported graph configuration exception with no parameters.
     */
    public UnsupportedConfigurationException() {
        super();
    }
    
    /**
     * Create an unsupported graph configuration exception with a message.
     * 
     * @param message a message
     */
    public UnsupportedConfigurationException(final String message) {
        super(message);
    }
    
    /**
     * Create an unsupported graph configuration exception with a message and a cause.
     * 
     * @param message a message
     * @param cause a cause
     */
    public UnsupportedConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
