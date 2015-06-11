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
package de.cau.cs.kieler.kiml.util.labelspacing;

import de.cau.cs.kieler.kiml.util.nodespacing.Rectangle;

/**
 * Information wrapper for size and position of a group of labels. Basically a {@link Rectangle}
 * with an additional field for the current y-offset inside the group, used while placing the
 * labels.
 * 
 * @author csp
 */
public final class LabelGroup extends Rectangle {

    /** Next free y position. */
    // SUPPRESS CHECKSTYLE NEXT 1 |
    public double nextLabelYPos = 0;
}