package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.ArrayList;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.plugin_server.rootcausedetection.util.Maths;
import explorviz.shared.model.*;

/**
 * This class represents an algorithm using the powerMean to aggregate
 * RootCauseRatings from class level.
 *
 * @author Christian Claus Wiechmann, Jens Michaelis
 *
 */
public class PowerMeanAggregationAlgorithm extends AbstractAggregationAlgorithm {

	private final double errorState = RanCorrConfiguration.RootCauseRatingFailureState;
	private final Double appP = RanCorrConfiguration.PowerMeanExponentApplicationLevel;
	private final Double compP = RanCorrConfiguration.PowerMeanExponentComponentLevel;

	@Override
	public void aggregate(final RanCorrLandscape lscp) {
		raiseRatingsToHigherLevels(lscp);
		normalizeRatingsOnAllLevels(lscp);
	}

	/**
	 * Aggregates RanCorrRatings from the component layer to the application
	 * layer
	 *
	 * @param lscp
	 *            Landscape we want to work with
	 */
	private void raiseRatingsToHigherLevels(final RanCorrLandscape lscp) {
		for (Application app : lscp.getApplications()) {
			ArrayList<Double> RCRs = new ArrayList<Double>();
			for (final Component comp : app.getComponents()) {
				componentPowerMean(comp);
				double value = comp.getRootCauseRating();
				if ((value >= 0.0d) && (value <= 1.0d)) {
					RCRs.add((value * 2.0d) - 1.0d);
				}
			}
			Double RCR = Maths.unweightedPowerMean(RCRs, appP);
			if (RCR != null) {
				app.setRootCauseRating((RCR + 1.0d) / 2.0d);
			} else {
				app.setRootCauseRating(errorState);
			}
		}
	}

	/**
	 * Aggretates values from the component (children) and clazz layer to the
	 * component layer
	 *
	 * @param comp
	 *            observed component
	 */
	private void componentPowerMean(final Component comp) {
		ArrayList<Double> RCRs = new ArrayList<Double>();
		for (final Component child : comp.getChildren()) {
			componentPowerMean(child);
			double value = child.getRootCauseRating();
			if ((value >= 0.0d) && (value <= 1.0d)) {
				RCRs.add((value * 2.0d) - 1.0d);
			}
		}

		for (final Clazz clazz : comp.getClazzes()) {
			double value = clazz.getRootCauseRating();
			if ((value >= 0.0d) && (value <= 1.0d)) {
				RCRs.add((value * 2.0d) - 1.0d);
			}
		}

		Double RCR = Maths.unweightedPowerMean(RCRs, compP);
		if (RCR != null) {
			comp.setRootCauseRating((RCR + 1.0d) / 2.0d);
		} else {
			comp.setRootCauseRating(errorState);
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

		// packages
		double sumPackages = 0;
		for (final Component component : lscp.getPackages()) {
			if (component.getRootCauseRating() < 0) {
				continue;
			}
			sumPackages += component.getRootCauseRating();
		}
		// applications
		double sumApplications = 0;
		for (final Application application : lscp.getApplications()) {
			if (application.getRootCauseRating() < 0) {
				continue;
			}
			sumApplications += application.getRootCauseRating();
		}

		// Now we can actually normalize the values. There are two exceptions
		// though:
		// - The calculated sum might be null. In that case we actually have no
		// underlying operations at all and this cannot be a root cause.
		// - There was no temporary rating set for the element. In that case the
		// element itself has no underlying operations and it cannot be a root
		// cause either.

		// normalize classes
		// for (final Clazz clazz : lscp.getClasses()) {
		// if ((clazz.getTemporaryRating() < 0) || (sumClasses <= 0)) {
		// clazz.setRootCauseRating(0);
		// } else {
		// clazz.setRootCauseRating(clazz.getTemporaryRating() / sumClasses);
		// }
		// }

		// normalize packages
		for (final Component component : lscp.getPackages()) {
			if ((component.getRootCauseRating() < 0) || (sumPackages <= 0)) {
				component.setRootCauseRating(0);
			} else {
				component.setRootCauseRating(component.getRootCauseRating() / sumPackages);
			}
		}

		// normalize applications
		for (final Application application : lscp.getApplications()) {
			if ((application.getRootCauseRating() < 0) || (sumApplications <= 0)) {
				application.setRootCauseRating(0);
			} else {
				application.setRootCauseRating(application.getRootCauseRating() / sumApplications);
			}
		}
	}
}
