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
	@Override
	public <T> void setProperty(final IProperty<? super T> property, final T value) {
		if (propertyMap == null) {
			propertyMap = new HashMap<IProperty<?>, Object>();
		}
		if (value == null) {
			propertyMap.remove(property);
		} else {
			propertyMap.put(property, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * Retrieves a property value for a given class. If the property holder
	 * contains multiple instances of the class, the returned instance is
	 * selected arbitrarily. This method is less efficient than
	 * {@link #getProperty(IProperty)}, so use it with caution.
	 * 
	 * @param <T>
	 *            type of property
	 * @param clazz
	 *            a class
	 * @return a contained instance of the class, or {@code null} if there is
	 *         none
	 */
	// public <T> T getProperty(final Class<T> clazz) {
	// if (propertyMap != null) {
	// for (Object value : propertyMap.values()) {
	// if (clazz.isInstance(value)) {
	// return clazz.cast(value);
	// }
	// }
	// }
	// return null;
	// }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void copyProperties(final IPropertyHolder other) {
		if (other == null) {
			return;
		}
		final Map<IProperty<?>, Object> otherMap = other.getAllProperties();
		if (propertyMap == null) {
			propertyMap = new HashMap<IProperty<?>, Object>(otherMap);
		} else {
			propertyMap.putAll(otherMap);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<IProperty<?>, Object> getAllProperties() {
		if (propertyMap == null) {
			return Collections.emptyMap();
		} else {
			return propertyMap;
		}
	}

	/**
	 * Check for upper and lower bounds. If a property value does not fit into
	 * the bounds, it is reset to the default value.
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
