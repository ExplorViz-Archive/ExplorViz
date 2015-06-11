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
 * Enumeration of in-layer constraint types. In-layer constraints divide a layer into three
 * parts: the normal part, a top part and a bottom part. This constraint can be set on nodes
 * to define in which part they may appear.
 * 
 * @see de.cau.cs.kieler.klay.layered.intermediate.InLayerConstraintProcessor
 *        InLayerConstraintProcessor
 * @author cds
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public enum InLayerConstraint {
    
    /** no constraint on in-layer placement. */
    NONE,
    /** float node to the top of the layer, along with other nodes posessing this constraint. */
    TOP,
    /** float node to the bottom of the layer, along with other nodes posessing this constraint. */
    BOTTOM;
    
}