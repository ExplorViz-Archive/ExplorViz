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
package de.cau.cs.kieler.core.properties;

import java.io.Serializable;
import java.util.*;

/**
 * An abstract holder class for properties that uses a hash map.
 *
 * @kieler.design 2011-01-17 reviewed by haf, cmot, soh
 * @kieler.rating proposed yellow 2012-07-10 msp
 * @author msp
 */
public class MapPropertyHolder implements IPropertyHolder, Serializable {

	/** the serial version UID. */
	private static final long serialVersionUID = 4507851447415709893L;

	/** map of property identifiers to their values. */
	private HashMap<IProperty<?>, Object> propertyMap;

	/**
	 * {@inheritDoc}
	 */
	public <T> MapPropertyHolder setProperty(final IProperty<? super T> property, final T value) {
		if (propertyMap == null) {
			propertyMap = new HashMap<IProperty<?>, Object>();
		}
		if (value == null) {
			propertyMap.remove(property);
		} else {
			propertyMap.put(property, value);
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T getProperty(final IProperty<T> property) {
		if (propertyMap != null) {
			@SuppressWarnings("unchecked")
			final T value = (T) propertyMap.get(property);
			if (value != null) {
				return value;
			}
		}

		// Retrieve the default value and memorize it for our property
		final T defaultValue = property.getDefault();
		if (defaultValue instanceof Cloneable) {
			setProperty(property, defaultValue);
		}
		return defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public MapPropertyHolder copyProperties(final IPropertyHolder other) {
		if (other == null) {
			return this;
		}

		final Map<IProperty<?>, Object> otherMap = other.getAllProperties();
		if (!otherMap.isEmpty()) {
			if (propertyMap == null) {
				propertyMap = new HashMap<IProperty<?>, Object>(otherMap);
			} else {
				propertyMap.putAll(otherMap);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<IProperty<?>, Object> getAllProperties() {
		if (propertyMap == null) {
			return Collections.emptyMap();
		} else {
			return propertyMap;
		}
	}

	/**
	 * Check for upper and lower bounds. If a property value does not fit into
	 * the bounds, it is reset to the respective bound or to the default value.
	 * 
	 * @param newProperties
	 *            the properties that shall be checked
	 */
	public void checkProperties(final IProperty<?>... newProperties) {
		for (final IProperty<?> property : newProperties) {
			final Object value = propertyMap.get(property);
			if (value != null) {
				@SuppressWarnings("unchecked")
				final Comparable<Object> lowbo = (Comparable<Object>) property.getLowerBound();
				@SuppressWarnings("unchecked")
				final Comparable<Object> uppbo = (Comparable<Object>) property.getUpperBound();
				if ((lowbo.compareTo(value) > 0) || (uppbo.compareTo(value) < 0)) {
					propertyMap.remove(property);
				}
			}
		}
	}

}
