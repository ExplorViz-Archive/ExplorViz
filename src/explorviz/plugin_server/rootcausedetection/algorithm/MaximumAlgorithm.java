package explorviz.plugin_server.rootcausedetection.algorithm;

import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.*;

/**
 * This class represents an algorithm using maxima to aggregate RootCauseRatings
 * from class level.
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
	 * This method takes all ratings on class level and copies them to all
	 * elements directly above them. The (temporary) rating of the higher
	 * element is decided by a maximum function (either the rating of the
	 * underlying element or the rating the element already has, if higher).
	 * This method really generates temporary ratings which are not necessarily
	 * in [0, 1] as required. It will set the algebraic signs of the rating
	 * correctly, though.
	 *
	 * @param lscp
	 *            Landscape we want to work with
	 */
	protected void raiseRatingsToHigherLevels(final RanCorrLandscape lscp) {
		for (final Clazz clazz : lscp.getClasses()) {
			// raise RCR and sign to package level
			Component component = clazz.getParent();
			component.setTemporaryRating(Math.max(component.getTemporaryRating(),
					clazz.getRootCauseRating()));
			component.setIsRankingPositive(component.getTemporaryRating() > clazz
					.getRootCauseRating() ? component.isIsRankingPositive() : isRankingPositive(
					lscp, clazz));
			double lastRating = component.getTemporaryRating();
			boolean lastIsRankingPositive = component.isIsRankingPositive();

			// also give ratings and sign to parent packages
			while (component.getParentComponent() != null) {
				component = component.getParentComponent();
				component.setTemporaryRating(Math.max(component.getTemporaryRating(), lastRating));
				component
				.setIsRankingPositive(component.getTemporaryRating() > lastRating ? component
						.isIsRankingPositive() : lastIsRankingPositive);
				lastRating = component.getTemporaryRating();
				lastIsRankingPositive = component.isIsRankingPositive();
			}

			// raise RCR and sign to application level
			final Application application = component.getBelongingApplication();
			application.setTemporaryRating(Math.max(application.getTemporaryRating(), lastRating));
			application
					.setIsRankingPositive(application.getTemporaryRating() > lastRating ? application
							.isIsRankingPositive() : lastIsRankingPositive);
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
		for (final Clazz clazz : lscp.getClasses()) {
			if (clazz.getTemporaryRating() < 0) {
				continue;
			}
			sumClasses += clazz.getTemporaryRating();
		}
		// packages
		double sumPackages = 0;
		for (final Component component : lscp.getPackages()) {
			if (component.getTemporaryRating() < 0) {
				continue;
			}
			sumPackages += component.getTemporaryRating();
		}
		// applications
		double sumApplications = 0;
		for (final Application application : lscp.getApplications()) {
			if (application.getTemporaryRating() < 0) {
				continue;
			}
			sumApplications += application.getTemporaryRating();
		}

		// Now we can actually normalize the values. There are two exceptions
		// though:
		// - The calculated sum might be null. In that case we actually have no
		// underlying operations at all and this cannot be a root cause.
		// - There was no temporary rating set for the element. In that case the
		// element itself has no underlying operations and it cannot be a root
		// cause either.

		// normalize classes
		for (final Clazz clazz : lscp.getClasses()) {
			if ((clazz.getTemporaryRating() < 0) || (sumClasses <= 0)) {
				clazz.setRootCauseRating(0);
			} else {
				clazz.setRootCauseRating(clazz.getTemporaryRating() / sumClasses);
			}
		}

		// normalize packages
		for (final Component component : lscp.getPackages()) {
			if ((component.getTemporaryRating() < 0) || (sumPackages <= 0)) {
				component.setRootCauseRating(0);
			} else {
				component.setRootCauseRating(component.getTemporaryRating() / sumPackages);
			}
		}

		// normalize applications
		for (final Application application : lscp.getApplications()) {
			if ((application.getTemporaryRating() < 0) || (sumApplications <= 0)) {
				application.setRootCauseRating(0);
			} else {
				application.setRootCauseRating(application.getTemporaryRating() / sumApplications);
			}
		}
	}

}
