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
package de.cau.cs.kieler.klay.layered.p4nodes.bk;

/**
 * For documentation see {@link BKNodePlacer}.
 * 
 * @author uru
 */
public interface ICompactor {
    
    /**
     * In this step, actual coordinates are calculated for blocks and its nodes.
     * 
     * <p>First, all blocks are placed, trying to avoid any crossing of the blocks. Then, the blocks are
     * shifted towards each other if there is any space for compaction.</p>
     * 
     * @param bal One of the four layouts which shall be used in this step
     */
    void horizontalCompaction(final BKAlignedLayout bal);
    
    /**
     *  Specifies how the compaction step of 
     *  the {@link BKNodePlacer} should be executed.
     */
    public enum CompactionStrategy {
        /** As specified in the original paper. */
        CLASSIC,
        /** An integrated method trying to increase the number of straight edges. */
        IMPROVE_STRAIGHTNESS,
    }
}
