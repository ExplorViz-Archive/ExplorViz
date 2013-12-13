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

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.klay.force.properties.Properties;

/**
 * A label in the force graph.
 * 
 * @author tmn
 * @author owo
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class FLabel extends FParticle {
    
    /** the serial version UID. */
    private static final long serialVersionUID = 9047772256368142239L;
    
    /** the edge this label is associated to. */
    private FEdge edge;
    /** label text. */
    private String text;
    
    /**
     * Create a new label. The label is also put into the edge's list of labels.
     * 
     * @param text the text of the new label
     * @param fedge edge corresponding to this label
     */
    public FLabel(final FEdge fedge, final String text) {
        this.edge = fedge;
        this.text = text;
        edge.getLabels().add(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (text == null || text.length() == 0) {
            return "l[" + edge.toString() + "]";
        } else {
            return "l_" + text;
        }
    }
        
    /**
     * Returns the text of this label.
     * 
     * @return text of this label
     */
    public String getText() {
        return text;
    }
    
    /**
     * Returns the associated edge.
     * 
     * @return edge this label is associated to.
     */
    public FEdge getEdge() {
        return edge;
    }
    
    /**
     * Refresh the label position, that is place it in the center of the edge.
     */
    public void refreshPosition() { 
        double spacing = edge.getProperty(Properties.LABEL_SPACING);

        KVector src = edge.getSource().getPosition();
        KVector tgt = edge.getTarget().getPosition();

        // TODO add support for head and tail labels
        KVector pos = getPosition();
        if (src.x >= tgt.x) {
            if (src.y >= tgt.y) {
                // CASE1, src top left, tgt bottom right
                pos.x = tgt.x + ((src.x - tgt.x) / 2) + spacing;
                pos.y = tgt.y + ((src.y - tgt.y) / 2) - spacing;
            } else {
                // CASE2, src bottom left, tgt top right
                pos.x = tgt.x + ((src.x - tgt.x) / 2) + spacing;
                pos.y = src.y + ((tgt.y - src.y) / 2) + spacing;
            }
        } else {
            if (src.y >= tgt.y) {
                // CASE2, src top right, tgt bottom left
                pos.x = src.x + ((tgt.x - src.x) / 2) + spacing;
                pos.y = tgt.y + ((src.y - tgt.y) / 2) + spacing;
            } else {
                // CASE1, src bottom right, tgt top left
                pos.x = src.x + ((tgt.x - src.x) / 2) + spacing;
                pos.y = src.y + ((tgt.y - src.y) / 2) - spacing;
            }
        }
    }

}
