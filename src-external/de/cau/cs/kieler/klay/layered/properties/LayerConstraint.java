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
 * Enumeration of layer constraint types. May be set on nodes to constrain in which layer
 * they may appear.
 *
 * @see de.cau.cs.kieler.klay.layered.intermediate.EdgeAndLayerConstraintEdgeReverser
 *        EdgeAndLayerConstraintEdgeReverser
 * @see de.cau.cs.kieler.klay.layered.intermediate.LayerConstraintProcessor
 *        LayerConstraintProcessor
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public enum LayerConstraint {
    
    /** no constraint on the layering. */
    NONE,
    /** put into the first layer. */
    FIRST,
    /** put into a separate first layer; used internally. */
    FIRST_SEPARATE,
    /** put into the last layer. */
    LAST,
    /** put into a separate last layer; used internally. */
    LAST_SEPARATE;
    
}