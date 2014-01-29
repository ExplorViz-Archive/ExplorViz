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
package de.cau.cs.kieler.kiml.options;

/**
 * Definition of port label placement strategies. The chosen strategy determines whether port
 * labels are placed inside or outside of the respective node.
 *
 * @author jjc
 */
public enum PortLabelPlacement {
    
    /** Port labels are placed outside of the node, beside the edge. */
    OUTSIDE,
    /** Port labels are placed inside of the node, next to the port. */
    INSIDE,
    /** Port labels are left on the position the user chose. */
    FIXED;

}
