package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.*;
import java.util.Map.Entry;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.Clazz;
import explorviz.shared.model.CommunicationClazz;

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

	/**
	 * This method returns if the Root Cause Rating of this class is positive.
	 * This information is directly derived from Anomaly Scores. This is done as
	 * follows: We get from every method in this class the latest anomaly score.
	 * From these we choose the AS which has the most recent timestamp and of
	 * these the highest absolute value. Then we check if this AS is >= 0.
	 *
	 * @param lscp
	 *            Landscape we want to look for operations in
	 * @param clazz
	 *            given clazz
	 * @return Is the Root Cause Ranking of this class positive?
	 */
	protected boolean isRankingPositive(RanCorrLandscape lscp, Clazz clazz) {
		long latest = 0;
		double valueOfLatest = 0;

		for (CommunicationClazz operation : lscp.getOperations()) {
			if (operation.getTarget() == clazz) {
				Entry<Long, Double> entry = getLatestAnomalyScorePair(operation);
				if (entry != null) {
					if (entry.getKey() > latest) {

						// more recent value has been found
						latest = entry.getKey();
						valueOfLatest = entry.getValue();
					} else if ((entry.getKey() == latest)
							&& (Math.abs(entry.getValue()) > Math.abs(valueOfLatest))) {

						// higher absolute value has been found
						// new value has the same timestamp as the one from
						// before
						valueOfLatest = entry.getValue();
					}
				}
			}
		}

		return valueOfLatest >= 0;
	}

	/**
	 * This method returns the latest timestamp-anomalyscore-pair for a given
	 * method.
	 *
	 * @param op
	 *            given operation
	 * @return Pair of (Timestamp, Anomaly Score), null if no scores are present
	 */
	protected Entry<Long, Double> getLatestAnomalyScorePair(CommunicationClazz op) {
		TreeMapLongDoubleIValue anomalyScores = (TreeMapLongDoubleIValue) op
				.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
		if (anomalyScores == null) {
			return null;
		}
		final List<Entry<Long, Double>> mapEntries = new ArrayList<Entry<Long, Double>>(
				anomalyScores.entrySet());

		Entry<Long, Double> current = null;
		for (Entry<Long, Double> entry : mapEntries) {
			if ((current == null) || (entry.getKey() > current.getKey())) {
				current = entry;
			}
		}

		return current;
	}
}
