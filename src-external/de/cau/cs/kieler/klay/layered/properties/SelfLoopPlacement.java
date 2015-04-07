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
 * Enumeration to distinguish the possible ways to distribute the selfLoops around a node.
 * 
 * @author tit
 */
public enum SelfLoopPlacement {
    /** Distributes the loops equally around the node. */
    EQUALLY_DISTRIBUTED,
    /** Stacks all loops to the north side of the node. */
    NORTH_STACKED,
    /** Loops are placed sequentially (next to each other) to the north side of the node. */
    NORTH_SEQUENCE;
}
