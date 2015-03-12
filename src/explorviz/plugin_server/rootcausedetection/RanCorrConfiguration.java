package explorviz.plugin_server.rootcausedetection;

import explorviz.plugin_server.rootcausedetection.algorithm.*;

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
	public static AbstractRanCorrAlgorithm ranCorrAlgorithm = new MeshAlgorithm();

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

	/**
	 * This value contains the number of cores the root cause algorithm will
	 * use.
	 */
	public static int numberOfThreads = Runtime.getRuntime().availableProcessors();

	/**
	 * This value determines the distance intensity constant which is needed in
	 * the mesh algorithm.
	 */
	public static final double DistanceIntensityConstant = 1.0d;

	/**
	 * This value determines the power mean exponent for aggregation on class
	 * level.
	 */
	public static final double PowerMeanExponentClassLevel = 0.2d;

	/**
	 * This value determines the weight of all overload anomaly scores
	 */
	public static final double RefinedNegativeFactor = 1d;

	/**
	 * This value determines to what extent values of the other sign are taken
	 * into the RCR with RefinedMeshAlgorithm
	 */
	public static final double RefinedBuffer = 0.1d;
}
