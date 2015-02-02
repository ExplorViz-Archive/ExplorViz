package explorviz.plugin_server.rootcausedetection.model;

import java.util.*;
import java.util.Map.Entry;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.exception.InvalidDistanceException;
import explorviz.plugin_server.rootcausedetection.exception.InvalidRootCauseRatingException;
import explorviz.shared.model.Clazz;

/**
 * This class extends a {@link Clazz} with functionality needed by the RanCorr
 * algorithms. It represents a class in the landscape.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class RanCorrClass extends Clazz {

	private double rootCauseRating;
	private int distance = 0;
	private double weight;

	/*
	 * This value is only used for the mesh algorithm and provides the distance
	 * to the observed class
	 */

	public int getDistance() {
		return distance;
	}

	public void setDistance(final int distance) {
		if (distance < 1) {
			throw new InvalidDistanceException("explorviz.plugin.rootcausedetection.model."
					+ "RanCorrApplication#setDistance(int): distance \"" + distance
					+ "\" is below [1]");
		} else {
			this.distance = distance;
		}
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(final double weight) {
		this.weight = weight;
	}

	/**
	 * This value is a temporary rating for this object. It may be used by any
	 * algorithm.
	 */
	public double temporaryRating = -1;

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
	 * Returns a list of all available timestamp-anomalyScore pairs for all
	 * operations in this class. All anomaly scores are in [0, 1].
	 *
	 * @param lscp
	 *            landscape we want to look for operations in
	 * @return list of timestamp-anomalyScore pairs
	 */
	public List<AnomalyScoreRecord> getAnomalyScores(final RanCorrLandscape lscp) {
		final List<AnomalyScoreRecord> outputScores = new ArrayList<>();

		// add all anomaly scores from operations that are placed inside this
		// class
		for (final RanCorrOperation operation : lscp.getOperations()) {
			if (operation.getTarget() == this) {
				outputScores.addAll(operation.getAnomalyScores());
			}
		}

		return outputScores;
	}

	/**
	 * This method returns if the Root Cause Rating of this class is positive.
	 * This information is directly derived from Anomaly Scores. This is done as
	 * follows: We get from every method in this class the latest anomaly score.
	 * From these we choose the AS which has the most recent timestamp and of
	 * these the highest absolute value. Then we check if this AS is >= 0.
	 *
	 * @param lscp
	 *            Landscape we want to look for operations in
	 * @return Is the Root Cause Ranking of this class positive?
	 */
	public boolean isRankingPositive(final RanCorrLandscape lscp) {
		long latest = 0;
		double valueOfLatest = 0;

		for (final RanCorrOperation operation : lscp.getOperations()) {
			if (operation.getTarget() == this) {
				final Entry<Long, Double> entry = operation.getLatestAnomalyScorePair();
				if (entry == null) {
					continue;
				}

				if (entry.getKey() > latest) {
					// more recent value has been found
					latest = entry.getKey();
					valueOfLatest = entry.getValue();
				} else if ((entry.getKey() == latest)
						&& (Math.abs(entry.getValue()) > Math.abs(valueOfLatest))) {
					// higher absolute value has been found
					// new value has the same timestamp as the one from before
					valueOfLatest = entry.getValue();
				}
			}
		}

		return valueOfLatest >= 0;
	}

}
