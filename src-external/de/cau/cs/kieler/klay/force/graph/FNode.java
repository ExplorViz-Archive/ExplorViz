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

import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.core.math.KVector;

/**
 * A node in the force graph.
 * 
 * @author owo
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class FNode extends FParticle {

    /** the serial version UID. */
    private static final long serialVersionUID = 8663670492984978893L;

    // CHECKSTYLEOFF VisibilityModifier
    /** the identifier number. */
    public int id;
    // CHECKSTYLEON VisibilityModifier
    
    /** particle position displacement for each iteration. */
    private KVector displacement = new KVector();
    /** the node label. */
    private String label;
    /** The parent node. */
    private FNode parent;
    /** List of child nodes. */
    private List<FNode> children;
    
    /**
     * Create a new node.
     */
    public FNode() {
    }
    
    /**
     * Create a new node with given label.
     * 
     * @param label the label text
     */
    public FNode(final String label) {
        this.label = label;
    }

    /**
     * Create a new node with given parent node.
     * 
     * @param label the label text
     * @param theParent the parent node
     */
    public FNode(final String label, final FNode theParent) {
        this.label = label;
        this.parent = theParent;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (label == null || label.length() == 0) {
            return "n_" + id;
        } else {
            return "n_" + label;
        }
    }

    /**
     * Returns the displacement vector.
     * 
     * @return the displacement vector
     */
    public final KVector getDisplacement() {
        return displacement;
    }

    /**
     * Returns whether this node is a compound node.
     * 
     * @return true if compound
     */
    public boolean isCompound() {
        return children != null && children.size() > 0;
    }
    
    /**
     * Returns the label text of this node.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the parent node.
     * 
     * @return the parent
     */
    public FNode getParent() {
        return parent;
    }

    /**
     * Returns the list of children, creating it if necessary.
     * 
     * @return the children
     */
    public List<FNode> getChildren() {
        if (children == null) {
            children = new LinkedList<FNode>();
        }
        return children;
    }

    /**
     * Returns the depth of this node in the compound hierarchy.
     * 
     * @return the depth
     */
    public int getDepth() {
        int depth = 0;
        FNode node = parent;
        while (node != null) {
            node = node.getParent();
            depth++;
        }
        return depth;
    }
    
}
