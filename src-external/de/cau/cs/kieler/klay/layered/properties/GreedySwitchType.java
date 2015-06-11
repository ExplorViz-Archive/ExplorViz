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
 * Sets the variant of the greedy switch heuristic.
 * 
 * @author alan
 *
 */
public enum GreedySwitchType {

    /** Only consider crossings to one side of the free layer. Calculate crossing matrix on demand. */
    ONE_SIDED(true, false, false),
    /** Consider crossings to both sides of the free layer. Calculate crossing matrix on demand. */
    TWO_SIDED(false, false, false),
    /**
     * Only consider crossings to one side of the free layer. Calculate crossing matrix on demand.
     * Compare all upward and downward sweeps.
     */
    ONE_SIDED_BEST_OF_UP_OR_DOWN(true, true, false),
    /**
     * Consider crossings to both sides of the free layer. Calculate crossing matrix on demand.
     * Compare all upward and downward sweeps.
     */
    TWO_SIDED_BEST_OF_UP_OR_DOWN(false, true, false),
    /**
     * Only consider crossings to one side of the free layer. Calculate crossing matrix on demand.
     * Compare all upward and downward sweeps. Use hyperedge crossings counter for between layer
     * edges
     */
    ONE_SIDED_BEST_OF_UP_OR_DOWN_ORTHOGONAL_HYPEREDGES(true, true, true),
    /**
     * Consider crossings to both sides of the free layer. Calculate crossing matrix on demand.
     * Compare all upward and downward sweeps. Use hyperedge crossings counter for between layer
     * edges.
     */
    TWO_SIDED_BEST_OF_UP_OR_DOWN_ORTHOGONAL_HYPEREDGES(false, true, true),
    /**
     * Only consider crossings to one side of the free layer. Calculate crossing matrix on demand.
     * Use hyperedge crossings counter for between layer edges.
     */
    ONE_SIDED_ORTHOGONAL_HYPEREDGES(true, false, true),
    /** Don't use greedy switch heuristic. */
    OFF(false, false, false);

    private final boolean isOneSided;
    private final boolean useBestOfUpOrDown;
    private final boolean useHperedgeCounter;

    private GreedySwitchType(final boolean isOneSided, final boolean useBestOfUpOrDown,
            final boolean useOrthogonalCounter) {
        this.isOneSided = isOneSided;
        this.useBestOfUpOrDown = useBestOfUpOrDown;
        useHperedgeCounter = useOrthogonalCounter;
    }

    /**
     * Only considers crossings to one side of the free layer.
     * 
     * @return true if only considers two layers.
     */
    public boolean isOneSided() {
        return isOneSided;
    }

    /**
     * Compares top-bottom and bottom->top in layer sweep direction.
     * 
     * @return whether this applies.
     */
    public boolean useBestOfUpOrDown() {
        return useBestOfUpOrDown;
    }

    /**
     * Uses hyperedge crossing count approximization for between-layer edges.
     * 
     * @return whether this applies.
     */
    public boolean useHyperedgeCounter() {
        return useHperedgeCounter;
    }

}
