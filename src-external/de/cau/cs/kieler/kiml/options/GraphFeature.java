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
 * Graph features used for automatic recognition of the suitability of layout algorithms.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-01-09 review KI-32 by ckru, chsch
 */
public enum GraphFeature {

    /**
     * Edges connecting a node with itself.
     */
    SELF_LOOPS,
    /**
     * Multiple edges with the same source and target node.
     */
    MULTI_EDGES,
    /**
     * Labels that are associated with edges.
     */
    EDGE_LABELS,
    /**
     * Edges are connected to nodes over ports.
     */
    PORTS,
    /**
     * Edges that connect nodes from different hierarchy levels and are incident to compound nodes.
     * @see LayoutOptions#LAYOUT_HIERARCHY
     */
    COMPOUND,
    /**
     * Edges that connect nodes from different clusters, but not the cluster parent nodes.
     */
    CLUSTERS,
    /**
     * Multiple connected components.
     */
    DISCONNECTED;

}
