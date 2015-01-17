package explorviz.plugin_server.rootcausedetection.model;

import java.util.*;
import java.util.Map.Entry;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.exception.InvalidRootCauseRatingException;
import explorviz.shared.model.CommunicationClazz;

/**
 * This class extends a {@link CommunicationClazz} with functionality needed by
 * the RanCorr algorithms. It represents an operation in the landscape.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class RanCorrOperation extends CommunicationClazz {

	private double rootCauseRating;

	public double getRootCauseRating() {
		return rootCauseRating;
	}

	/**
	 * Sets a RootCauseRating for this element in [0, 1]. Throws an
	 * {@link InvalidRootCauseRatingException} if not in range.
	 *
	 * @param rootCauseRating
	 *            RootCauseRating in [0, 1]
	 */
	public void setRootCauseRating(final double rootCauseRating) {
		if ((rootCauseRating < 0) || (rootCauseRating > 1)) {
			throw new InvalidRootCauseRatingException("explorviz.plugin.rootcausedetection.model."
					+ "RanCorrApplication#setRootCauseRating(double): RootCauseRating \""
					+ rootCauseRating + "\" is not in [0, 1]!");
		} else {
			this.rootCauseRating = rootCauseRating;
		}
	}

	/**
	 * Sets the root cause rating of this element to a failure state.
	 */
	public void setRootCauseRatingToFailure() {
		rootCauseRating = RanCorrConfiguration.RootCauseRatingFailureState;
	}

	/**
	 * Returns a list of all available timestamp-anomalyScore pairs for this
	 * operation. All anomaly scores are in [0, 1].
	 *
	 * @return List of {@link AnomalyScoreRecord}s. If there are no anomaly
	 *         scores available, the method will return null.
	 */
	public List<AnomalyScoreRecord> getAnomalyScores() {
		// return null if there are no anomaly scores
		if (!isGenericDataPresent(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE)) {
			return null;
		}

		// otherwise create list of timestamp-anomalyscore pairs
		// (AnomalyScoreRecord)
		final TreeMapLongDoubleIValue anomalyScores = (TreeMapLongDoubleIValue) getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
		final List<Entry<Long, Double>> mapEntries = new ArrayList<>(anomalyScores.entrySet());
		final List<AnomalyScoreRecord> outputScores = new ArrayList<>();

		for (final Entry<Long, Double> entry : mapEntries) {
			outputScores.add(new AnomalyScoreRecord(entry.getKey(), entry.getValue()));
		}

		return outputScores;
	}
}
