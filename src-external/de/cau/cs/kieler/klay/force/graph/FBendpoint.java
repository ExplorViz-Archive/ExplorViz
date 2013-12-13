/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2010 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.force.graph;

/**
 * A bend point in the force graph.
 * 
 * @author tmn
 * @author owo
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class FBendpoint extends FParticle {
    
    /** the serial version UID. */
    private static final long serialVersionUID = -7146373072650467350L;
    
    /** The edge this bend point belongs to. */
    private FEdge edge;
    
    /**
     * Construct a new bend point on the given edge. The bend point is also put
     * into the edge's list of bend points.
     * 
     * @param edge the edge this bend point belongs to
     */
    public FBendpoint(final FEdge edge) {
        this.edge = edge;
        edge.getBendpoints().add(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (edge != null) {
            int index = edge.getBendpoints().indexOf(this);
            if (index >= 0) {
                return "b" + index + "[" + edge.toString() + "]";
            } else {
                return "b[" + edge.toString() + "]";
            }
        }
        return "b_" + hashCode();
    }
    
    /**
     * Returns the edge this bend point belongs to.
     * 
     * @return the corresponding edge
     */
    public FEdge getEdge() {
        return edge;
    }
    
}
