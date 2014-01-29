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
package de.cau.cs.kieler.klay.layered.graph;

import de.cau.cs.kieler.core.math.KVector;

/**
 * Abstract superclass for {@link LGraphElement}s that can have a position and a size.
 * 
 * @author cds
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-03-22 review KI-35 by chsch, grh
 */
public abstract class LShape extends LGraphElement {
    
    /** the serial version UID. */
    private static final long serialVersionUID = 5111245934175354687L;
    
    /** the current position of the element. */
    private final KVector pos = new KVector();
    /** the size of the element. */
    private final KVector size = new KVector();
    
    /**
     * Creates a shape in the context of the given graph.
     * 
     * @param graph the graph for which the shape is created
     */
    public LShape(final LGraph graph) {
        super(graph.hashCodeCounter());
    }

    /**
     * Returns the element's current position. This is the coordinate of the element's upper
     * left corner. May be modified.
     * 
     * @return the position
     */
    public KVector getPosition() {
        return pos;
    }

    /**
     * Returns the element's current size. May be modified.
     * 
     * @return the size
     */
    public KVector getSize() {
        return size;
    }
    
}
