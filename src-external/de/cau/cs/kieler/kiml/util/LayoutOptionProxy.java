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
package de.cau.cs.kieler.kiml.util;

import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.IPropertyHolder;
import de.cau.cs.kieler.core.properties.IPropertyValueProxy;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.LayoutDataService;
import de.cau.cs.kieler.kiml.LayoutOptionData;

/**
 * A proxy class for lazy resolving of layout options.
 *
 * @author msp
 */
public final class LayoutOptionProxy implements IPropertyValueProxy {
    
    /** the serialized layout option value. */
    private String value;
    
    /**
     * Create a layout option proxy for the given value.
     * 
     * @param value the serialized layout option value
     */
    public LayoutOptionProxy(final String value) {
        this.value = value;
    }
    
    /**
     * Create a layout option proxy with given key and value strings.
     * 
     * @param propertyHolder the property holder in which to store the new value
     * @param key the layout option identifier string
     * @param value the serialized value
     */
    public static void setProxyValue(final IPropertyHolder propertyHolder, final String key,
            final String value) {
        IProperty<LayoutOptionProxy> property = new Property<LayoutOptionProxy>(key);
        LayoutOptionProxy proxy = new LayoutOptionProxy(value);
        propertyHolder.setProperty(property, proxy);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T resolveValue(final IProperty<T> property) {
        LayoutOptionData<?> optionData;
        if (property instanceof LayoutOptionData<?>) {
            optionData = (LayoutOptionData<?>) property;
        } else {
            optionData = LayoutDataService.getInstance().getOptionData(property.getId());
        }
        if (optionData != null) {
            return (T) optionData.parseValue(value);
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof LayoutOptionProxy) {
            LayoutOptionProxy other = (LayoutOptionProxy) object;
            return this.value == null ? other.value == null : this.value.equals(other.value);
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (value != null) {
            return value.hashCode();
        }
        return 0;
    }

}
