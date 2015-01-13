package explorviz.plugin.rootcausedetection.model;

import explorviz.plugin.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin.rootcausedetection.exception.InvalidRootCauseRatingException;
import explorviz.shared.model.Component;

/**
 * This class extends a {@link Component} with functionality needed by the
 * RanCorr algorithms. It represents a package in the landscape.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class RanCorrPackage extends Component {

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

}
