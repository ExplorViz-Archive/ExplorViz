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
package de.cau.cs.kieler.kiml.util.nodespacing;

import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;

/**
 * Enumeration for the definition of a side of the edge to place the (edge) label to. Currently
 * supported in orthogonal edge routing.
 * 
 * @author jjc
 */
public enum LabelSide {
    /** The label's placement side hasn't been decided yet. */
    UNKNOWN,
    /** The label is placed above the edge. */
    ABOVE,
    /** The label is placed below the edge. */
    BELOW;
    

    /**
     * Property set on edge and port labels by layout algorithms depending on which side they decide is
     * appropriate for any given label.
     */
    public static final IProperty<LabelSide> LABEL_SIDE = new Property<LabelSide>(
            "de.cau.cs.kieler.labelSide", LabelSide.UNKNOWN);
}
