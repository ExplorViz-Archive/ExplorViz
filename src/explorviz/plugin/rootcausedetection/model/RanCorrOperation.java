package explorviz.plugin.rootcausedetection.model;

import explorviz.plugin.rootcausedetection.exception.InvalidRootCauseRatingException;
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
			this.rootCauseRating = rootCauseRating;
		} else {
			throw new InvalidRootCauseRatingException("explorviz.plugin.rootcausedetection.model."
					+ "RanCorrOperation#setRootCauseRating(double): RootCauseRating \""
					+ rootCauseRating + "\" is not in [0, 1]!");
		}
	}

}
