package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.List;

import explorviz.plugin_server.rootcausedetection.model.*;
import explorviz.plugin_server.rootcausedetection.util.Maths;

/**
 * This class contains a very simple algorithm to calculate RootCauseRatings. It
 * only uses data of the element the RootCauseRating is calculated for.
 *
 * @author Christian Claus Wiechmann, Dominik Olp, Yannic Noller
 *
 */
public class LocalAlgorithm extends AbstractRanCorrAlgorithm {

	@Override
	public void calculate(final RanCorrClass clazz, final RanCorrLandscape lscp) {
		final List<AnomalyScoreRecord> anomalyScores = clazz.getAnomalyScores(lscp);

		// If there are no anomaly scores for a operation, set the
		// corresponding root cause rating to a failure state
		if (anomalyScores == null) {
			clazz.setRootCauseRatingToFailure();
			return;
		}

		// In trivial algorithm anomalyRank = locally aggregated anomaly
		// scores using unweighted arithmetic mean.
		final double anomalyRank = Maths
				.unweightedArithmeticMean(getValuesFromAnomalyList(anomalyScores));

		clazz.setRootCauseRating(mapToPropabilityRange(anomalyRank));
	}
}
