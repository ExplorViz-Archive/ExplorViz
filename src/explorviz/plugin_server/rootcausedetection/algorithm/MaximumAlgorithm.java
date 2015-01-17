package explorviz.plugin_server.rootcausedetection.algorithm;

import explorviz.plugin_server.rootcausedetection.model.*;

/**
 * This class represents an algorithm using maxima to aggregate RootCauseRatings
 * from operation level.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class MaximumAlgorithm extends AbstractAggregationAlgorithm {

	@Override
	public void aggregate(final RanCorrLandscape lscp) {
		raiseRatingsToHigherLevels(lscp);
		normalizeRatingsOnAllLevels(lscp);
	}

	/**
	 * This method takes all ratings on operation level and copies them to all
	 * elements directly above them. The (temporary) rating of the higher
	 * element is decided by a maximum function (either the rating of the
	 * underlying element or the rating the element already has, if higher).
	 * This method really generates temporary ratings which are not necessarily
	 * in [0, 1] as required.
	 *
	 * @param lscp
	 *            Landscape we want to work with
	 */
	private void raiseRatingsToHigherLevels(final RanCorrLandscape lscp) {
		for (final RanCorrOperation operation : lscp.getOperations()) {
			// raise RCR to class level
			// assumption: target in Clazz is the class the method belongs to
			final RanCorrClass clazz = (RanCorrClass) operation.getTarget();
			clazz.temporaryRating = Math.max(clazz.temporaryRating, operation.getRootCauseRating());

			// raise RCR to package level
			RanCorrPackage component = (RanCorrPackage) clazz.getParent();
			component.temporaryRating = Math.max(component.temporaryRating, clazz.temporaryRating);
			double lastRating = component.temporaryRating;

			// also give ratings to parent packages
			while (component.getParentComponent() != null) {
				component = (RanCorrPackage) component.getParentComponent();
				component.temporaryRating = Math.max(component.temporaryRating, lastRating);
				lastRating = component.temporaryRating;
			}

			// raise RCR to application level
			final RanCorrApplication application = (RanCorrApplication) component
					.getBelongingApplication();
			application.temporaryRating = Math.max(application.temporaryRating,
					component.temporaryRating);
		}
	}

	/**
	 * This method will normalize ratings of all elements of one level by
	 * dividing the temporary ratings by the sum of all ratings. Thus, we are
	 * able to get our final ratings in [0, 1].
	 *
	 * @param lscp
	 *            Landscape we want to work with
	 */
	private void normalizeRatingsOnAllLevels(final RanCorrLandscape lscp) {
		// get total sums of all higher elements

		// classes
		double sumClasses = 0;
		for (final RanCorrClass clazz : lscp.getClasses()) {
			if (clazz.temporaryRating < 0) {
				continue;
			}
			sumClasses += clazz.temporaryRating;
		}
		// packages
		double sumPackages = 0;
		for (final RanCorrPackage component : lscp.getPackages()) {
			if (component.temporaryRating < 0) {
				continue;
			}
			sumPackages += component.temporaryRating;
		}
		// applications
		double sumApplications = 0;
		for (final RanCorrApplication application : lscp.getApplications()) {
			if (application.temporaryRating < 0) {
				continue;
			}
			sumApplications += application.temporaryRating;
		}

		// Now we can actually normalize the values. There are two exceptions
		// though:
		// - The calculated sum might be null. In that case we actually have no
		// underlying operations at all and this cannot be a root cause.
		// - There was no temporary rating set for the element. In that case the
		// element itself has no underlying operations and it cannot be a root
		// cause either.

		// normalize classes
		for (final RanCorrClass clazz : lscp.getClasses()) {
			if ((clazz.temporaryRating < 0) || (sumClasses <= 0)) {
				clazz.setRootCauseRating(0);
			} else {
				clazz.setRootCauseRating(clazz.temporaryRating / sumClasses);
			}
		}

		// normalize packages
		for (final RanCorrPackage component : lscp.getPackages()) {
			if ((component.temporaryRating < 0) || (sumPackages <= 0)) {
				component.setRootCauseRating(0);
			} else {
				component.setRootCauseRating(component.temporaryRating / sumPackages);
			}
		}

		// normalize applications
		for (final RanCorrApplication application : lscp.getApplications()) {
			if ((application.temporaryRating < 0) || (sumApplications <= 0)) {
				application.setRootCauseRating(0);
			} else {
				application.setRootCauseRating(application.temporaryRating / sumApplications);
			}
		}
	}

}
