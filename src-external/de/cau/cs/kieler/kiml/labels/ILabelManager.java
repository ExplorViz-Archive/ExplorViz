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
package de.cau.cs.kieler.kiml.labels;

import de.cau.cs.kieler.core.math.KVector;

/**
 * Knows how to resize a label to a given target width.
 * 
 * <p>
 * During layout, a layout algorithm may discover that the size of a label considerably increases
 * the size of a diagram. If the label (or a parent) has a label manager attached, the manager may
 * be called to try and shorten the label to a given target width. The new size is returned to the
 * layout algorithm to work with. Of course, the changes to the label's text need to actually be
 * applied after automatic layout. How that works depends on the visualization framework used.
 * </p>
 * 
 * @author cds
 */
public interface ILabelManager {
    
    /**
     * Tries to shorten the label to keep it narrower than the given target width. This may increase
     * the label's height. The new dimensions of the label are returned for the layout algorithm to
     * work with.
     * 
     * @param label
     *            the label to shorten.
     * @param targetWidth
     *            the width the label's new dimensions should try not to exceed.
     * @return the label's dimensions after shortening or {@code null} if the label has not been
     *         shortened.
     */
    KVector resizeLabelToWidth(Object label, double targetWidth);
    
}
