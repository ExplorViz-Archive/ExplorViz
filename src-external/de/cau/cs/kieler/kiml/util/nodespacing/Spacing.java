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

import de.cau.cs.kieler.core.util.IDataObject;

/**
 * Stores the spacing of an object in {@code double} precision.
 * 
 * @author cds
 * @author uru
 */
public abstract class Spacing implements IDataObject, Cloneable {
	// Allow public fields in these utility classes.
	// CHECKSTYLEOFF VisibilityModifier

	/** The serial version UID. */
	private static final long serialVersionUID = 4358555478195088364L;

	/**
	 * The inset from the top.
	 */
	public double top = 0.0;

	/**
	 * The inset from the bottom.
	 */
	public double bottom = 0.0;

	/**
	 * The inset from the left.
	 */
	public double left = 0.0;

	/**
	 * The inset from the right.
	 */
	public double right = 0.0;

	// CHECKSTYLEON VisibilityModifier

	/**
	 * Creates a new instance with all fields set to {@code 0.0}.
	 */
	protected Spacing() {

	}

	/**
	 * Creates a new instance initialized with the given values.
	 * 
	 * @param top
	 *            the inset from the top.
	 * @param left
	 *            the inset from the left.
	 * @param bottom
	 *            the inset from the bottom.
	 * @param right
	 *            the inset from the right.
	 */
	protected Spacing(final double top, final double left, final double bottom, final double right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	/**
	 * Sets all four insets at once.
	 * 
	 * @param newTop
	 *            the inset from the top.
	 * @param newLeft
	 *            the inset from the left.
	 * @param newBottom
	 *            the inset from the bottom.
	 * @param newRight
	 *            the inset from the right.
	 */
	public void set(final double newTop, final double newLeft, final double newBottom,
			final double newRight) {
		top = newTop;
		left = newLeft;
		bottom = newBottom;
		right = newRight;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Spacing) {
			final Spacing other = (Spacing) obj;

			return (top == other.top) && (bottom == other.bottom) && (left == other.left)
					&& (right == other.right);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		// I don't care very much to define explicit constants for this
		// calculation.
		// CHECKSTYLEOFF MagicNumber

		int code1 = java.lang.Double.valueOf(left).hashCode() << 16;
		code1 |= java.lang.Double.valueOf(bottom).hashCode() & 0xffff;

		int code2 = java.lang.Double.valueOf(right).hashCode() << 16;
		code2 |= java.lang.Double.valueOf(top).hashCode() & 0xffff;

		return code1 ^ code2;

		// CHECKSTYLEON MagicNumber
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "[top=" + top + ",left=" + left + ",bottom=" + bottom + ",right=" + right + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	public void parse(final String string) {
		int start = 0;
		while ((start < string.length()) && isdelim(string.charAt(start), "([{\"' \t\r\n")) {
			start++;
		}
		int end = string.length();
		while ((end > 0) && isdelim(string.charAt(end - 1), ")]}\"' \t\r\n")) {
			end--;
		}
		if (start < end) {
			final String[] tokens = string.substring(start, end).split(",|;");
			try {
				for (final String token : tokens) {
					final String[] keyandvalue = token.split("=");
					if (keyandvalue.length != 2) {
						throw new IllegalArgumentException("Expecting a list of key-value pairs.");
					}
					final String key = keyandvalue[0].trim();
					final double value = Double.parseDouble(keyandvalue[1].trim());
					if (key.equals("top")) {
						top = value;
					} else if (key.equals("left")) {
						left = value;
					} else if (key.equals("bottom")) {
						bottom = value;
					} else if (key.equals("right")) {
						right = value;
					}
				}
			} catch (final NumberFormatException exception) {
				throw new IllegalArgumentException(
						"The given string contains parts that cannot be parsed as numbers."
								+ exception);
			}
		}
	}

	/**
	 * Determine whether the given character is a delimiter.
	 * 
	 * @param c
	 *            a character
	 * @param delims
	 *            a string of possible delimiters
	 * @return true if {@code c} is one of the characters in {@code delims}
	 */
	private static boolean isdelim(final char c, final String delims) {
		for (int i = 0; i < delims.length(); i++) {
			if (c == delims.charAt(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Copy insets values from another double valued insets.
	 * 
	 * @param other
	 *            another insets
	 * @return this instance.
	 */
	public Spacing copy(final Spacing other) {
		left = other.left;
		right = other.right;
		top = other.top;
		bottom = other.bottom;
		return this;
	}

	// GWTExcludeStart
	// Object.clone() is not available in GWT

	// GWTExcludeEnd

	/**
	 * Stores the insets of an element. The insets are spacings from the border
	 * of an element to other internal elements.
	 * 
	 * @author uru
	 */
	public static final class Insets extends Spacing {

		/** The serial version UID. */
		private static final long serialVersionUID = -2159860709896900657L;

		/**
		 * Creates a new instance with all fields set to {@code 0.0}.
		 */
		public Insets() {
			super();
		}

		/**
		 * Creates a new instance with all fields set to the value of
		 * {@code other}.
		 * 
		 * @param other
		 *            insets object from which to copy the values.
		 */
		public Insets(final Insets other) {
			super(other.top, other.left, other.bottom, other.right);
		}

		/**
		 * Creates a new instance initialized with the given values.
		 * 
		 * @param top
		 *            the inset from the top.
		 * @param left
		 *            the inset from the left.
		 * @param bottom
		 *            the inset from the bottom.
		 * @param right
		 *            the inset from the right.
		 */
		public Insets(final double top, final double left, final double bottom, final double right) {
			super(top, left, bottom, right);
		}
	}

	/**
	 * Stores the margins of an element. The margin is the area around the
	 * border of an element that has to be kept free of any other elements.
	 * 
	 * @author uru
	 */
	public static final class Margins extends Spacing {

		/** The serial version UID. */
		private static final long serialVersionUID = 7465583871643915474L;

		/**
		 * Creates a new instance with all fields set to {@code 0.0}.
		 */
		public Margins() {
			super();
		}

		/**
		 * Creates a new instance with all fields set to the value of
		 * {@code other}.
		 * 
		 * @param other
		 *            margins object from which to copy the values.
		 */
		public Margins(final Margins other) {
			super(other.top, other.left, other.bottom, other.right);
		}

		/**
		 * Creates a new instance initialized with the given values.
		 * 
		 * @param top
		 *            the margin from the top.
		 * @param left
		 *            the margin from the left.
		 * @param bottom
		 *            the margin from the bottom.
		 * @param right
		 *            the margin from the right.
		 */
		public Margins(final double top, final double left, final double bottom, final double right) {
			super(top, left, bottom, right);
		}
	}

}
