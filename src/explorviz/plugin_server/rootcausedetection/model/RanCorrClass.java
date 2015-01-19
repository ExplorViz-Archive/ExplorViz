package explorviz.plugin_server.rootcausedetection.model;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
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

}
