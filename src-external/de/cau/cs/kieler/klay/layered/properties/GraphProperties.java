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
 * An enumeration of properties a graph may have. These can be used as part of an
 * {@code EnumSet} to base decisions on graph properties. For example, self-loop
 * processing may be skipped if the graph doesn't contain self-loops in the first
 * place.
 * 
 * <p>An {@code EnumSet} for this enumeration can be attached to a graph via the
 * {@link Properties#GRAPH_PROPERTIES} property.</p>
 * 
 * @author cds
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public enum GraphProperties {
    
    /** The graph contains comment boxes. */
    COMMENTS,
    /** The graph contains dummy nodes representing external ports. */
    EXTERNAL_PORTS,
    /** The graph contains hyperedges. */
    HYPEREDGES,
    /** The graph contains hypernodes (nodes that are marked as such). */
    HYPERNODES,
    /** The graph contains ports that are not free for positioning. */
    NON_FREE_PORTS,
    /** The graph contains ports on the northern or southern side. */
    NORTH_SOUTH_PORTS,
    /** The graph contains self-loops. */
    SELF_LOOPS,
    /** The graph contains node labels. */
    CENTER_LABELS,
    /** The graph contains head or tail edge labels. */
    END_LABELS;
    
}
