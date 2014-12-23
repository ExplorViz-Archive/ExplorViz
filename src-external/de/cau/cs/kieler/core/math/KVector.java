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
package de.cau.cs.kieler.core.math;

import java.util.Random;

import de.cau.cs.kieler.core.util.IDataObject;

/**
 * A simple 2D vector class which supports translation, scaling, normalization
 * etc.
 *
 * @kieler.design 2014-04-17 reviewed by cds, chsch, tit, uru
 * @kieler.rating 2011-01-13 proposed yellow msp
 * @author uru
 * @author owo
 */
public final class KVector implements IDataObject, Cloneable {

	/** the serial version UID. */
	private static final long serialVersionUID = -4780985519832787684L;

	// CHECKSTYLEOFF VisibilityModifier
	/** x coordinate. */
	public double x;
	/** y coordinate. */
	public double y;

	// CHECKSTYLEON VisibilityModifier

	/**
	 * Create vector with default coordinates (0,0).
	 */
	public KVector() {
		x = 0.0;
		y = 0.0;
	}

	/**
	 * Constructs a new vector from given values.
	 * 
	 * @param thex
	 *            x value
	 * @param they
	 *            y value
	 */
	public KVector(final double thex, final double they) {
		x = thex;
		y = they;
	}

	/**
	 * Creates an exact copy of a given vector v.
	 * 
	 * @param v
	 *            existing vector
	 */
	public KVector(final KVector v) {
		x = v.x;
		y = v.y;
	}

	/**
	 * Creates a normalized vector for the passed angle in radians.
	 * 
	 * @param angle
	 *            angle in radians.
	 */
	public KVector(final double angle) {
		x = Math.cos(angle);
		y = Math.sin(angle);
	}

	/**
	 * Returns an exact copy of this vector.
	 * 
	 * @return identical vector
	 */
	public KVector clone() {
		return new KVector(x, y);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof KVector) {
			final KVector other = (KVector) obj;
			return (x == other.x) && (y == other.y);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Double.valueOf(x).hashCode() + Integer.reverse(Double.valueOf(y).hashCode());
	}

	/**
	 * returns this vector's length.
	 * 
	 * @return Math.sqrt(x*x + y*y)
	 */
	public double length() {
		return Math.sqrt((x * x) + (y * y));
	}

	/**
	 * returns square length of this vector.
	 * 
	 * @return x*x + y*y
	 */
	public double squareLength() {
		return (x * x) + (y * y);
	}

	/**
	 * Set vector to (0,0).
	 * 
	 * @return {@code this}
	 */
	public KVector reset() {
		x = 0.0;
		y = 0.0;
		return this;
	}

	/**
	 * Vector addition.
	 * 
	 * @param v
	 *            vector to add
	 * @return <code>this + v</code>
	 */
	public KVector add(final KVector v) {
		x += v.x;
		y += v.y;
		return this;
	}

	/**
	 * Translate the vector by adding the given amount.
	 * 
	 * @param dx
	 *            the x offset
	 * @param dy
	 *            the y offset
	 * @return {@code this}
	 */
	public KVector add(final double dx, final double dy) {
		x += dx;
		y += dy;
		return this;
	}

	/**
	 * Returns the sum of arbitrarily many vectors as a new vector instance.
	 * 
	 * @param vs
	 *            vectors to be added
	 * @return a new vector containing the sum of given vectors
	 */
	public static KVector sum(final KVector... vs) {
		final KVector sum = new KVector();
		for (final KVector v : vs) {
			sum.x += v.x;
			sum.y += v.y;
		}
		return sum;
	}

	/**
	 * Vector subtraction.
	 * 
	 * @param v
	 *            vector to subtract
	 * @return {@code this}
	 */
	public KVector sub(final KVector v) {
		x -= v.x;
		y -= v.y;
		return this;
	}

	/**
	 * Translate the vector by subtracting the given amount.
	 * 
	 * @param dx
	 *            the x offset
	 * @param dy
	 *            the y offset
	 * @return {@code this}
	 */
	public KVector sub(final double dx, final double dy) {
		x -= dx;
		y -= dy;
		return this;
	}

	/**
	 * Scale the vector.
	 * 
	 * @param scale
	 *            scaling factor
	 * @return {@code this}
	 */
	public KVector scale(final double scale) {
		x *= scale;
		y *= scale;
		return this;
	}

	/**
	 * Scale the vector with different values for X and Y coordinate.
	 * 
	 * @param scalex
	 *            the x scaling factor
	 * @param scaley
	 *            the y scaling factor
	 * @return {@code this}
	 */
	public KVector scale(final double scalex, final double scaley) {
		x *= scalex;
		y *= scaley;
		return this;
	}

	/**
	 * Normalize the vector.
	 * 
	 * @return {@code this}
	 */
	public KVector normalize() {
		final double length = length();
		if (length > 0) {
			x /= length;
			y /= length;
		}
		return this;
	}

	/**
	 * scales this vector to the passed length.
	 * 
	 * @param length
	 *            length to scale to
	 * @return {@code this}
	 */
	public KVector scaleToLength(final double length) {
		normalize();
		this.scale(length);
		return this;
	}

	/**
	 * Negate the vector.
	 * 
	 * @return {@code this}
	 */
	public KVector negate() {
		x = -x;
		y = -y;
		return this;
	}

	/**
	 * Returns angle representation of this vector in degree. The length of the
	 * vector must not be 0.
	 * 
	 * @return value within [0,360)
	 */
	public double toDegrees() {
		return Math.toDegrees(toRadians());
	}

	/**
	 * Returns angle representation of this vector in radians. The length of the
	 * vector must not be 0.
	 * 
	 * @return value within [0,2*pi)
	 */
	public double toRadians() {
		final double length = length();
		assert length > 0;

		if ((x >= 0) && (y >= 0)) { // 1st quadrant
			return Math.asin(y / length);
		} else if (x < 0) { // 2nd or 3rd quadrant
			return Math.PI - Math.asin(y / length);
		} else { // 4th quadrant
			return (2 * Math.PI) + Math.asin(y / length);
		}
	}

	/**
	 * Add some "noise" to this vector.
	 * 
	 * @param random
	 *            the random number generator
	 * @param amount
	 *            the amount of noise to add
	 */
	public void wiggle(final Random random, final double amount) {
		x += (random.nextDouble() * amount) - (amount / 2);
		y += (random.nextDouble() * amount) - (amount / 2);
	}

	/**
	 * Returns the distance between two vectors.
	 * 
	 * @param v2
	 *            second vector
	 * @return distance between this and second vector
	 */
	public double distance(final KVector v2) {
		final double dx = x - v2.x;
		final double dy = y - v2.y;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	/**
	 * Returns the dot product of the two given vectors.
	 * 
	 * @param v2
	 *            second vector
	 * @return (this.x * this.x) + (v1.y * v2.y)
	 */
	public double dotProduct(final KVector v2) {
		return ((x * v2.x) + (y * v2.y));
	}

	/**
	 * Apply the given bounds to this vector.
	 * 
	 * @param lowx
	 *            the lower bound for x coordinate
	 * @param lowy
	 *            the lower bound for y coordinate
	 * @param highx
	 *            the upper bound for x coordinate
	 * @param highy
	 *            the upper bound for y coordinate
	 * @return {@code this}
	 * @throws IllegalArgumentException
	 *             if highx < lowx or highy < lowy
	 */
	public KVector bound(final double lowx, final double lowy, final double highx,
			final double highy) {
		if ((highx < lowx) || (highy < lowy)) {
			throw new IllegalArgumentException(
					"The highx must be bigger then lowx and the highy must be bigger then lowy");
		}
		if (x < lowx) {
			x = lowx;
		} else if (x > highx) {
			x = highx;
		}
		if (y < lowy) {
			y = lowy;
		} else if (y > highy) {
			y = highy;
		}
		return this;
	}

	/**
	 * Determine whether any of the two values are NaN.
	 * 
	 * @return true if x is NaN or y is NaN
	 */
	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(y);
	}

	/**
	 * Determine whether any of the two values are infinite.
	 * 
	 * @return true if x is infinite or y is infinite
	 */
	public boolean isInfinite() {
		return Double.isInfinite(x) || Double.isInfinite(y);
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
		if (start >= end) {
			throw new IllegalArgumentException("The given string does not contain any numbers.");
		}
		final String[] tokens = string.substring(start, end).split(",|;|\r|\n");
		if (tokens.length != 2) {
			throw new IllegalArgumentException("Exactly two numbers are expected, " + tokens.length
					+ " were found.");
		}
		try {
			x = Double.parseDouble(tokens[0].trim());
			y = Double.parseDouble(tokens[1].trim());
		} catch (final NumberFormatException exception) {
			throw new IllegalArgumentException(
					"The given string contains parts that cannot be parsed as numbers." + exception);
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

}
