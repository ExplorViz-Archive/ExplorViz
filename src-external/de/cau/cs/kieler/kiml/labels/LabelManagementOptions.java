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

import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;

/**
 * Layout options specifically concerned with label management.
 * 
 * @author cds
 */
public final class LabelManagementOptions {
    
    /**
     * The label manager responsible for a given part of the graph. A label manager can either be
     * attached to a compound node (in which case it is responsible for all labels inside) or to specific
     * labels. The label manager can then be called by layout algorithms to modify labels that are too
     * wide to try and shorten them to a given target width.
     */
    public static final IProperty<ILabelManager> LABEL_MANAGER =
            new Property<ILabelManager>("de.cau.cs.kieler.labels.labelManager", null);
    
    
    /**
     * Not supposed to be instantiated.
     */
    private LabelManagementOptions() {
        throw new IllegalStateException("not supposed to be instantiated.");
    }
    
}
