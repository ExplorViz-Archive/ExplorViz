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
	public static AbstractRanCorrAlgorithm ranCorrAlgorithm = new MeshAlgorithm();

	/**
	 * Currently used algorithm for persisting RootCauseRatings.
	 */
	public static AbstractPersistAlgorithm ranCorrPersistAlgorithm = new RGBAlgorithm();

}
