package explorviz.plugin.rootcausedetection.model;

import explorviz.plugin.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin.rootcausedetection.exception.InvalidRootCauseRatingException;
import explorviz.shared.model.Application;

/**
 * This class extends a {@link Application} with functionality needed by the
 * RanCorr algorithms. It represents an application in the landscape.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class RanCorrApplication extends Application {

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
			this.rootCauseRating = rootCauseRating;
		} else {
			throw new InvalidRootCauseRatingException("explorviz.plugin.rootcausedetection.model."
					+ "RanCorrApplication#setRootCauseRating(double): RootCauseRating \""
					+ rootCauseRating + "\" is not in [0, 1]!");
		}
	}

	/**
	 * Sets the root cause rating of this element to a failure state.
	 */
	public void setRootCauseRatingToFailure() {
		rootCauseRating = RanCorrConfiguration.RootCauseRatingFailureState;
	}

}
