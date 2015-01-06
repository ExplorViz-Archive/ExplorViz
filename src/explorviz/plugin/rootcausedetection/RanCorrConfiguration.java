package explorviz.plugin.rootcausedetection;

import explorviz.plugin.rootcausedetection.algorithm.*;

/**
 * This class includes parameters for the configuration of the
 * RootCauseDetection.
 *
 * @author Christian Claus Wiechmann
 *
 */
public final class RanCorrConfiguration {

	private RanCorrConfiguration() {
	}

	/**
	 * Currently used algorithm for RootCauseRating calculation.
	 */
	public static AbstractRanCorrAlgorithm ranCorrAlgorithm = new LocalAlgorithm();

	/**
	 * Currently used algorithm for persisting RootCauseRatings.
	 */
	public static AbstractPersistAlgorithm ranCorrPersistAlgorithm = new RGBAlgorithm();

	/**
	 * Currently used algorithm for aggregating RootCauseRatings from operation
	 * level.
	 */
	public static AbstractAggregationAlgorithm ranCorrAggregationAlgorithm = new MaximumAlgorithm();

	/**
	 * This value describes an internal failure state for root cause ratings if
	 * it could not be properly calculated.
	 */
	public static Double RootCauseRatingFailureState = -1.0d;

}
