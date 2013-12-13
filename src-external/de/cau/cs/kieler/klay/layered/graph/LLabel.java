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
package de.cau.cs.kieler.klay.layered.graph;

/**
 * A label in the layered graph structure.
 * 
 * @author jjc
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-03-22 review KI-35 by chsch, grh
 */
public final class LLabel extends LShape {
    
    /** Enumeration for the definition of a side of the edge to place the (edge) label to. 
     *  Currently supported in orthogonal edge routing.
     */
    public enum LabelSide {
        /** The label's placement side hasn't been decided yet. */
        UNKNOWN,
        /** The label is placed above the edge. */
        ABOVE,
        /** The label is placed below the edge. */
        BELOW;
    }

    /** the serial version UID. */
    private static final long serialVersionUID = -264988654527750053L;
    
    /** text of the label. */
    private String text;
    
    /** side of the label (if it's an edge label). */
    private LabelSide side = LabelSide.UNKNOWN;
    
    /**
     * Creates a label.
     * 
     * @param graph the graph for which the label is created
     * @param thetext text of the label
     */
    public LLabel(final LGraph graph, final String thetext) {
        super(graph);
        this.text = thetext;
    }
    
    /**
     * Creates a label with empty text.
     * 
     * @param graph the graph for which the label is created
     */
    public LLabel(final LGraph graph) {
        this(graph, "");
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        if (text == null) {
            return "l_" + id;
        } else {
            return "l_" + text;
        }
    }
    
    /**
     * Returns the text of the label.
     * 
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * If this is an edge label, this method returns the side of the edge on which the label is placed.
     * 
     * @return the label side, or {@code null}
     */
    public LabelSide getSide() {
        return side;
    }

    /**
     * Sets the side of the edge on which to place the label.
     * 
     * @param side the side to set; must not be {@code null}.
     * @throws IllegalArgumentException if {@code side == null}.
     */
    public void setSide(final LabelSide side) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        
        this.side = side;
    }
    
}
