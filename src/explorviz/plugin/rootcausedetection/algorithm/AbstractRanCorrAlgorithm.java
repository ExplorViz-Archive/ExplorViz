package explorviz.plugin.rootcausedetection.algorithm;

import explorviz.plugin.rootcausedetection.model.RanCorrLandscape;

/**
 * This abstract class represents algorithms concerning the calculation of
 * RootCauseRatings in a RanCorrLandscape.
 *
 * @author Christian Claus Wiechmann
 *
 */
public abstract class AbstractRanCorrAlgorithm {

	/**
	 * Calculate RootCauseRatings in a RanCorrLandscape and uses Anomaly Scores
	 * in the ExplorViz landscape.
	 *
	 * @param lscp
	 *            specified landscape
	 */
	public abstract void calculate(RanCorrLandscape lscp);

}