/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2015 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.properties;

import java.util.EnumSet;

/**
 * Determines how the content of compound nodes is to be aligned if the compound node's size exceeds
 * the bounding box of the content (i.e. child nodes). This might be the case if for a compound node
 * the {@link de.cau.cs.kieler.kiml.options.SizeConstraint SizeConstraint} of
 * {@link de.cau.cs.kieler.kiml.options.SizeConstraint#MINIMUM_SIZE MINIMUM_SIZE} is set and
 * {@link de.cau.cs.kieler.kiml.options.LayoutOptions#MIN_WIDTH MIN_WIDTH} and
 * {@link de.cau.cs.kieler.kiml.options.LayoutOptions#MIN_HEIGHT MIN_HEIGHT} are set large enough.
 * 
 * This property is to be used as an {@link java.util.EnumSet EnumSet}; it should be comprised of
 * exactly one value prefixed with {@code V_} and one prefixed with {@code H_}.
 * 
 * Default values are {@link ContentAlignment#V_TOP V_TOP} and {@link ContentAlignment#H_LEFT
 * H_LEFT}.
 * 
 * @author uru
 */
public enum ContentAlignment {

    /** Content should be vertically aligned to the top. */
    V_TOP,
    /** Content should be vertically centered. */
    V_CENTER,
    /** Content should be vertically aligned to the bottom. */
    V_BOTTOM,
    /** Content should be horizontally aligned to the left. */
    H_LEFT,
    /** Content should be horizontally centered. */
    H_CENTER,
    /** Content should be horizontally aligned to the right. */
    H_RIGHT;

    /**
     * @return a set containing {@link #V_CENTER} and {@link #H_CENTER}.
     */
    public static EnumSet<ContentAlignment> centerCenter() {
        return EnumSet.of(V_CENTER, H_CENTER);
    }

    /**
     * @return a set containing {@link #V_TOP} and {@link #H_LEFT}.
     */
    public static EnumSet<ContentAlignment> topLeft() {
        return EnumSet.of(V_TOP, H_LEFT);
    }

    /**
     * @return a set containing {@link #V_BOTTOM} and {@link #H_RIGHT}.
     */
    public static EnumSet<ContentAlignment> bottomRight() {
        return EnumSet.of(V_BOTTOM, H_RIGHT);
    }
}
