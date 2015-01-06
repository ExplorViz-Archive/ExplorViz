package explorviz.plugin.rootcausedetection.algorithm;

import explorviz.plugin.rootcausedetection.model.RanCorrLandscape;

/**
 * This abstract class represents algorithms concerning the aggregation of
 * RootCauseRatings from operation level. For these algorithms to work, any
 * {@link AbstractRanCorrAlgorithm} has to be called beforehand.
 *
 * @author Christian Claus Wiechmann
 *
 */
public abstract class AbstractAggregationAlgorithm {

	/**
	 * This method calculated all RootCauseRatings of higher levels based on
	 * those of the operation level.
	 *
	 * @param lscp
	 *            Specifies the landscape the ratings are to be calculated for.
	 */
	public abstract void aggregate(RanCorrLandscape lscp);

}
