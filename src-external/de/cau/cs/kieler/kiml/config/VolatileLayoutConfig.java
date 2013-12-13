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
package de.cau.cs.kieler.kiml.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.kiml.LayoutContext;
import de.cau.cs.kieler.kiml.LayoutOptionData;
import de.cau.cs.kieler.kiml.klayoutdata.KLayoutData;

/**
 * A layout configurator that can be used to generate on-the-fly layout options.
 * Use {@link #setValue(IProperty, Object, IProperty, Object)} to set a layout option value for a
 * particular context. All values configured this way are held in a hash map that is queried when
 * the configurator is applied to the layout graph.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-07-01 review KI-38 by cds, uru
 */
public class VolatileLayoutConfig implements ILayoutConfig {
    
    /** the default priority for volatile layout configurators. */
    public static final int DEFAULT_PRIORITY = 100;

    /** map of focus objects and property identifiers to their values. */
    private final Map<Object, Map<IProperty<?>, Object>> optionMap
            = new HashMap<Object, Map<IProperty<?>, Object>>();
    /** the layout context keys managed by this configurator. */
    private final Set<IProperty<?>> contextKeys = new HashSet<IProperty<?>>();
    /** the priority of this configurator. */
    private int priority;
    
    /**
     * Creates a volatile layout configurator with default priority.
     */
    public VolatileLayoutConfig() {
        priority = DEFAULT_PRIORITY;
    }
    
    /**
     * Creates a volatile layout configurator with given priority.
     * 
     * @param prio the priority to apply for this configurator
     */
    public VolatileLayoutConfig(final int prio) {
        this.priority = prio;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "VolatileLayoutConfig:" + optionMap.toString();
    }

    /**
     * {@inheritDoc}
     */
    public int getPriority() {
        return priority;
    }

    /**
     * {@inheritDoc}
     */
    public void enrich(final LayoutContext context) {
        // no properties to enrich for this layout configuration
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(final LayoutOptionData<?> optionData, final LayoutContext context) {
        for (IProperty<?> contextKey : contextKeys) {
            // retrieve the object stored under this key from the context
            Object object = context.getProperty(contextKey);
            // retrieve the map of options that have been set for that object
            Map<IProperty<?>, Object> contextOptions = optionMap.get(object);
            if (contextOptions != null) {
                // get the value set for the given layout option, if any
                Object value = contextOptions.get(optionData);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }
    
    /**
     * Set a new value for a layout option in the context of the given object.
     * 
     * @param option a layout option property
     * @param contextObj the object to which the option value is attached,
     *          e.g. a domain model element
     * @param contextKey the layout context key related to the context object,
     *          e.g. {@link LayoutContext#DOMAIN_MODEL}
     * @param value the new layout option value
     */
    public void setValue(final IProperty<?> option, final Object contextObj,
            final IProperty<?> contextKey, final Object value) {
        contextKeys.add(contextKey);
        
        Map<IProperty<?>, Object> contextOptions = optionMap.get(contextObj);
        if (contextOptions == null) {
            contextOptions = new HashMap<IProperty<?>, Object>();
            optionMap.put(contextObj, contextOptions);
        }
        if (value == null) {
            contextOptions.remove(option);
        } else {
            contextOptions.put(option, value);
        }
    }
    
    /**
     * Copy all values from the given layout configurator into this one.
     * 
     * @param other another volatile layout configurator
     */
    public void copyValues(final VolatileLayoutConfig other) {
        this.contextKeys.addAll(other.contextKeys);
        this.optionMap.putAll(other.optionMap);
    }

    /**
     * {@inheritDoc}
     */
    public void transferValues(final KLayoutData graphData, final LayoutContext context) {
        for (IProperty<?> contextKey : contextKeys) {
            Object object = context.getProperty(contextKey);
            Map<IProperty<?>, Object> contextOptions = optionMap.get(object);
            if (contextOptions != null) {
                for (Map.Entry<IProperty<?>, Object> option : contextOptions.entrySet()) {
                    graphData.setProperty(option.getKey(), option.getValue());
                }
            }
        }
    }

}
