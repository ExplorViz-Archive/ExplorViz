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
 * Enumeration of edge constraints. Edge constraints can be set on ports to constrain the
 * type of edges incident to that port.
 * 
 * @see de.cau.cs.kieler.klay.layered.intermediate.EdgeAndLayerConstraintEdgeReverser
 *        EdgeAndLayerConstraintEdgeReverser
 * @author cds
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public enum EdgeConstraint {
    
    /** no constraint on incident edges. */
    NONE,
    /** node may have only incoming edges. */
    INCOMING_ONLY,
    /** node may have only outgoing edges. */
    OUTGOING_ONLY;
    
}