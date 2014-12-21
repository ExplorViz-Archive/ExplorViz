package explorviz.plugin.rootcausedetection.model;

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
					+ "RanCorrPackage#setRootCauseRating(double): RootCauseRating \""
					+ rootCauseRating + "\" is not in [0, 1]!");
		}
	}

}
