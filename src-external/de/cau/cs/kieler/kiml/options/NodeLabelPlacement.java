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

import java.util.EnumSet;

/**
 * Options for controlling how node labels are placed by layout algorithms. The corresponding layout
 * option will usually accept an {@link EnumSet} over this enumeration, theoretically allowing
 * arbitrary and even contradictory subsets of options to be set. Note that you are restricted to
 * the following combinations if you want your choice to make sense:
 * <ul>
 *   <li>Exactly one of the {@link #INSIDE} and {@link #OUTSIDE} options.</li>
 *   <li>Exactly one of the {@link #H_LEFT}, {@link #H_CENTER}, and {@link #H_RIGHT} options.</li>
 *   <li>Exactly one of the {@link #V_TOP}, {@link #V_CENTER}, and {@link #V_BOTTOM} options.</li>
 * </ul>
 * <p><b>This enumeration is not set directly on {@link LayoutOptions#NODE_LABEL_PLACEMENT}; instead,
 * an {@code EnumSet} over this enumeration is used there.</b></p>
 * 
 * <p><i>Note:</i> Layout algorithms may only support a subset of these options.</p>
 *
 * @author msp
 * @author cds
 */
public enum NodeLabelPlacement {
    
    /**
     * Horizontal left placement.
     */
    H_LEFT,
    
    /**
     * Horizontal center placement.
     */
    H_CENTER,
    
    /**
     * Horizontal right placement.
     */
    H_RIGHT,
    
    /**
     * Vertical top placement.
     */
    V_TOP,
    
    /**
     * Vertical center placement.
     */
    V_CENTER,
    
    /**
     * Vertical bottom placement.
     */
    V_BOTTOM,
    
    /**
     * Place node labels on the inside of the node. This should usually be combined with size
     * constraints to ensure the node is big enough to accomodate its labels.
     */
    INSIDE,
    
    /**
     * Place node labels on the outside of the node.
     */
    OUTSIDE,
    
    /**
     * If set, the default behaviour is changed to give the horizontal placement priority over the
     * vertical one. This option is only relevant for {@link #OUTSIDE} placement.
     * 
     * <p>The default behaviour is to first choose the northern or southern node side for placement of
     * the label, according to the {@link #V_TOP} and {@link #V_BOTTOM} options. The horizontal placement
     * is then constricted to the width of the node, thus resulting in labels that are placed above or
     * below the node, but never to its left or right side.</p>
     * 
     * <p>If this option is set, the default behaviour is overridden and the horizontal placement options
     * are evaluated first, thus causing the eastern or western node side to be chosen for placement of
     * the label. The vertical placement options are then constricted to the height of the node, thus
     * resulting in labels that are placed to the left or right of the node.</p>
     */
    H_PRIORITY;
    
    
    /**
     * Returns the enumeration value related to the given ordinal.
     * 
     * @param i ordinal value
     * @return the related enumeration value
     */
    public static NodeLabelPlacement valueOf(final int i) {
        return values()[i];
    }
    

    /**
     * Returns an empty enum set over this enumeration, which prevents the layout algorithm from
     * changing the label's coordinates.
     * 
     * @return set over this enumeration representing fixed node label placement constraints.
     */
    public static EnumSet<NodeLabelPlacement> fixed() {
        return EnumSet.noneOf(NodeLabelPlacement.class);
    }
    
    /**
     * Returns a node label placement to place the node label inside the node, left-aligned on top.
     * 
     * @return node label placement for inside top left placement.
     */
    public static EnumSet<NodeLabelPlacement> insideTopLeft() {
        return EnumSet.of(INSIDE, V_TOP, H_LEFT);
    }
    
    /**
     * Returns a node label placement to place the node label inside the node, centered at its top.
     * 
     * @return node label placement for inside top center placement.
     */
    public static EnumSet<NodeLabelPlacement> insideTopCenter() {
        return EnumSet.of(INSIDE, V_TOP, H_CENTER);
    }
    
    /**
     * Returns a node label placement to place the node label inside the node, right-aligned on top.
     * 
     * @return node label placement for inside top right placement.
     */
    public static EnumSet<NodeLabelPlacement> insideTopRight() {
        return EnumSet.of(INSIDE, V_TOP, H_RIGHT);
    }
    
    /**
     * Returns a node label placement to place the node label centered inside the node.
     * 
     * @return node label placement for inside centered placement.
     */
    public static EnumSet<NodeLabelPlacement> insideCenter() {
        return EnumSet.of(INSIDE, V_CENTER, H_CENTER);
    }
    
    /**
     * Returns a node label placement to place the node label inside the node, left-aligned on bottom.
     * 
     * @return node label placement for inside top bottom placement.
     */
    public static EnumSet<NodeLabelPlacement> insideBottomLeft() {
        return EnumSet.of(INSIDE, V_BOTTOM, H_LEFT);
    }
    
    /**
     * Returns a node label placement to place the node label inside the node, centered at its bottom.
     * 
     * @return node label placement for inside bottom center placement.
     */
    public static EnumSet<NodeLabelPlacement> insideBottomCenter() {
        return EnumSet.of(INSIDE, V_BOTTOM, H_CENTER);
    }
    
    /**
     * Returns a node label placement to place the node label inside the node, right-aligned on bottom.
     * 
     * @return node label placement for inside top right placement.
     */
    public static EnumSet<NodeLabelPlacement> insideBottomRight() {
        return EnumSet.of(INSIDE, V_BOTTOM, H_RIGHT);
    }
    
    /**
     * Returns a node label placement to place the node label outside the node, left-aligned on top.
     * 
     * @return node label placement for outside top left placement.
     */
    public static EnumSet<NodeLabelPlacement> outsideTopLeft() {
        return EnumSet.of(OUTSIDE, V_TOP, H_LEFT);
    }
    
    /**
     * Returns a node label placement to place the node label outside the node, centered on top.
     * 
     * @return node label placement for outside top center placement.
     */
    public static EnumSet<NodeLabelPlacement> outsideTopCenter() {
        return EnumSet.of(OUTSIDE, V_TOP, H_CENTER);
    }
    
    /**
     * Returns a node label placement to place the node label outside the node, right-aligned on top.
     * 
     * @return node label placement for outside top left placement.
     */
    public static EnumSet<NodeLabelPlacement> outsideTopRight() {
        return EnumSet.of(OUTSIDE, V_TOP, H_RIGHT);
    }
    
    /**
     * Returns a node label placement to place the node label outside the node, left-aligned on bottom.
     * 
     * @return node label placement for outside top left placement.
     */
    public static EnumSet<NodeLabelPlacement> outsideBottomLeft() {
        return EnumSet.of(OUTSIDE, V_BOTTOM, H_LEFT);
    }

    /**
     * Returns a node label placement to place the node label outside the node, centered below it.
     * 
     * @return node label placement for outside bottom center placement.
     */
    public static EnumSet<NodeLabelPlacement> outsideBottomCenter() {
        return EnumSet.of(OUTSIDE, V_BOTTOM, H_CENTER);
    }
    
    /**
     * Returns a node label placement to place the node label outside the node, right-aligned on bottom.
     * 
     * @return node label placement for outside top left placement.
     */
    public static EnumSet<NodeLabelPlacement> outsideBottomRight() {
        return EnumSet.of(OUTSIDE, V_BOTTOM, H_RIGHT);
    }
}
