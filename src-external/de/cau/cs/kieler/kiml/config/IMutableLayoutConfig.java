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
package de.cau.cs.kieler.kiml.config;

import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.LayoutOptionData;

/**
 * An extension of the layout configuration interface for configurations that can be altered.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-07-01 review KI-38 by cds, uru
 */
public interface IMutableLayoutConfig extends ILayoutConfig {
    
    /** option for layout context: whether changes should be applied also for all child elements. */
    IProperty<Boolean> OPT_RECURSIVE = new Property<Boolean>("context.recursive", false);
    
    /**
     * Set a new value for a layout option in the given context.
     * 
     * @param optionData a layout option descriptor
     * @param context a context for layout configuration
     * @param value the new layout option value, or {@code null} if the current value shall be removed
     */
    void setValue(LayoutOptionData optionData, LayoutContext context, Object value);
    
    /**
     * Clear all layout option values that have been set for the given context.
     * 
     * @param context a context for layout configuration
     */
    void clearValues(LayoutContext context);
    
    /**
     * Determine whether the given layout option is set, not considering any default values.
     * 
     * @param optionData a layout option descriptor
     * @param context a context for layout configuration
     * @return true if the option is set
     */
    boolean isSet(LayoutOptionData optionData, LayoutContext context);

}
