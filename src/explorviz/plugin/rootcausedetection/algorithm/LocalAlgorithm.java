package explorviz.plugin.rootcausedetection.algorithm;

import java.util.List;

import explorviz.plugin.rootcausedetection.model.*;
import explorviz.plugin.rootcausedetection.util.Maths;

/**
 * This class contains a very simple algorithm to calculate RootCauseRatings. It
 * only uses data of the element the RootCauseRating is calculated for.
 *
 * @author Christian Claus Wiechmann, Dominik Olp, Yannic Noller
 *
 */
public class LocalAlgorithm extends AbstractRanCorrAlgorithm {

	@Override
	public void calculate(final RanCorrLandscape lscp) {
		for (final RanCorrOperation operation : lscp.getOperations()) {

			final List<AnomalyScoreRecord> anomalyScores = operation.getAnomalyScores();

			// If there are no anomaly scores for a operation, set the
			// corresponding root cause rating to a failure state
			if (anomalyScores == null) {
				operation.setRootCauseRatingToFailure();
				continue;
			}

			// In trivial algorithm anomalyRank = locally aggregated anomaly
			// scores using unweighted arithmetic mean.
			final double anomalyRank = Maths
					.unweightedArithmeticMean(getValuesFromAnomalyList(operation.getAnomalyScores()));

			operation.setRootCauseRating(mapToPropabilityRange(anomalyRank));
		}
	}

}
