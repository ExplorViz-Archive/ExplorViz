package explorviz.plugin.rootcausedetection.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides general, mathematical methods for the anomaly score correlation.
 *
 * @author Yannic Noller
 * @version 0.2
 */
public final class Maths {

	private Maths() {
	}

	/**
	 * Calculates the pow function without loosing the initial algebraic sign,
	 * i.e. the pow function is calculated on the norm of the base and after
	 * that the algebraic sign is readded.
	 *
	 * @param a
	 *            - base
	 * @param q
	 *            - exponent
	 * @return true value exponentation
	 */
	public static double gamma(final double a, final double q) {
		if (a >= 0) {
			return Math.pow(Math.abs(a), q);
		} else {
			return (-1) * Math.pow(Math.abs(a), q);
		}
	}

	/**
	 * Calculates the unweighted arithmetic mean of a set of double values.
	 *
	 * @param values
	 *            - set of double values
	 * @return unweighted arithmetic mean, null if all given values were null or
	 *         parameter are not correct.
	 */
	public static Double unweightedArithmeticMean(final List<Double> values) {
		if ((values == null) || values.isEmpty()) {
			return null;
		}

		// Remove null values from list. If list afterwards is empty, return
		// null.
		final List<Double> cleanedValues = cleanList(values);
		if (cleanedValues.isEmpty()) {
			return null;
		}

		// Unweighted arithmetic mean is the same as unweighted power mean with
		// exponent 1.
		return unweightedPowerMean(cleanedValues, 1);
	}

	/**
	 * Calculates the unweighted power mean of a list of double values.
	 *
	 * @param values
	 *            - set of double values
	 * @param p
	 *            - power mean exponent
	 * @return unweighted power mean, null if all given values were null.
	 */
	public static Double unweightedPowerMean(final List<Double> values, final double p) {
		if ((values == null) || values.isEmpty()) {
			return null;
		}

		// Remove null values from list. If list afterwards is empty, return
		// null.
		final List<Double> cleanedValues = cleanList(values);
		if (cleanedValues.isEmpty()) {
			return null;
		}

		// Build imaginary weights for the values. The weight does not change
		// the calculation.
		final List<Double> weights = new ArrayList<Double>();
		for (int i = 0; i < cleanedValues.size(); i++) {
			weights.add(1.0);
		}

		return weightedPowerMean(cleanedValues, weights, p);
	}

	/**
	 * Calculates the weighted power mean of the given list of values with the
	 * corresponding weights.
	 *
	 * @param values
	 *            - list of double values
	 * @param weights
	 *            - list of double values
	 * @param p
	 *            - power mean exponent
	 * @return weighted power mean, null if all given values were null or
	 *         parameter were incorrect
	 */
	public static Double weightedPowerMean(final List<Double> values, final List<Double> weights,
			final double p) {
		if ((values == null) || values.isEmpty() || (weights == null) || weights.isEmpty()) {
			return null;
		}
		if (values.size() != weights.size()) {
			return null;
		}

		// Remove null values from both list synchronously. If lists are
		// afterwards is empty, return
		// null.
		final List<Double> cleanedValues = new ArrayList<Double>();
		final List<Double> cleanedWeights = new ArrayList<Double>();
		for (int i = 0; i < values.size(); i++) {
			if ((values.get(i) != null) && (weights.get(i) != null)) {
				cleanedValues.add(values.get(i));
				cleanedWeights.add(weights.get(i));
			}
		}
		if (cleanedValues.isEmpty()) {
			return null;
		}

		double sumOfWeightedValues = 0;
		for (int i = 0; i < cleanedValues.size(); i++) {
			sumOfWeightedValues += cleanedWeights.get(i) * gamma(cleanedValues.get(i), p);
		}

		double sumOfWeights = 0;
		for (final double weight : cleanedWeights) {
			sumOfWeights += weight;
		}

		if ((sumOfWeights == 0) || (p == 0)) {
			return null;
		}

		return gamma(sumOfWeightedValues / sumOfWeights, 1 / p);
	}

	/**
	 * Removes all null values from the list.
	 *
	 * @param values
	 * @return list without null values, null if parameters incorrect
	 */
	private static List<Double> cleanList(final List<Double> values) {
		if (values == null) {
			return null;
		}
		final List<Double> cleanedList = new ArrayList<Double>();
		for (final Double obj : values) {
			if (obj != null) {
				cleanedList.add(obj);
			}
		}
		return cleanedList;
	}

}
