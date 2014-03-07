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
package de.cau.cs.kieler.klay.layered.compound;

import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * A data holder used to pass information on hierarchy crossing edges from the
 * {@link CompoundGraphPreprocessor} to the {@link CompoundGraphPostprocessor}.
 * Instances of this class are held in a multimap attached to the top-level graph via the
 * {@link de.cau.cs.kieler.klay.layered.properties.Properties#CROSS_HIERARCHY_MAP CROSS_HIERARCHY_MAP}
 * property.
 *
 * @author msp
 */
public class CrossHierarchyEdge {
    
    /** the edge used in the layered graph to compute a layout. */
    private LEdge newEdge;
    /** the layered graph in which the layout was computed. */
    private LGraph graph;
    /** the flow direction: input or output. */
    private PortType type;
    
    /**
     * Create a cross-hierarchy edge segment.
     * 
     * @param newEdge the edge used in the layered graph to compute a layout
     * @param graph the layered graph in which the layout is computed
     * @param type the flow direction: input or output
     */
    CrossHierarchyEdge(final LEdge newEdge, final LGraph graph, final PortType type) {
        this.newEdge = newEdge;
        this.graph = graph;
        this.type = type;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return type.toString() + ":" + newEdge.toString();
    }

    /**
     * Return the dummy edge used to compute a layout in one segment of the cross-hierarchy edge.
     * 
     * @return the dummy edge
     */
    public LEdge getEdge() {
        return newEdge;
    }

    /**
     * Return the graph in which the dummy edge {@link #getEdge()} is used.
     * 
     * @return the graph in which the dummy edge is used
     */
    public LGraph getGraph() {
        return graph;
    }

    /**
     * Return the type of cross-hierarchy edge segment (input or output). An input segment is one
     * that points to deeper hierarchy levels, while an output segment is one that points to
     * shallower hierarchy levels.
     * 
     * @return the edge segment type
     */
    public PortType getType() {
        return type;
    }
    
    /**
     * Return the actual source port of the edge. In case the source port of the dummy edge is
     * an external port, the corresponding port of the containing node is returned.
     * 
     * @return the actual source port
     */
    public LPort getActualSource() {
        if (newEdge.getSource().getNode().getProperty(Properties.NODE_TYPE) == NodeType.EXTERNAL_PORT) {
            return (LPort) newEdge.getSource().getNode().getProperty(Properties.ORIGIN);
        }
        return newEdge.getSource();
    }
    
    /**
     * Return the actual target port of the edge. In case the target port of the dummy edge is
     * an external port, the corresponding port of the containing node is returned.
     * 
     * @return the actual target port
     */
    public LPort getActualTarget() {
        if (newEdge.getTarget().getNode().getProperty(Properties.NODE_TYPE) == NodeType.EXTERNAL_PORT) {
            return (LPort) newEdge.getTarget().getNode().getProperty(Properties.ORIGIN);
        }
        return newEdge.getTarget();
    }
    
}
