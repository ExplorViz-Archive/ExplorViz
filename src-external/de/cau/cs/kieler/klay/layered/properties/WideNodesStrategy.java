/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2014 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.properties;

/**
 * Strategy to distribute wide nodes over multiple layers.
 * 
 * @author uru
 */
public enum WideNodesStrategy {

    /**
     * Splits wide nodes prior to the crossing minimization phase. Note that this can lead to
     * edge-node crossings.
     */
    AGGRESSIVE,
    /**
     * Splits wide nodes after the crossing minimization phase, guaranteeing that no edge-node
     * crossings are introduced.
     */
    CAREFUL,
    /** Do not handle wide nodes specially. */
    OFF

}
