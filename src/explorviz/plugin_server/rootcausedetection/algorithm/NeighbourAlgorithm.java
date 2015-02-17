package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_server.rootcausedetection.model.AnomalyScoreRecord;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.plugin_server.rootcausedetection.util.Maths;
import explorviz.shared.model.Clazz;
import explorviz.shared.model.CommunicationClazz;

/**
 * This class contains a simple algorithm to calculate RootCauseRatings. It uses
 * the data of all directly adjacent elements of the element the RootCauseRating
 * is calculated for.
 *
 * @author Christian Claus Wiechmann, Jens Michaelis
 *
 */

public class NeighbourAlgorithm extends AbstractRanCorrAlgorithm {

	@Override
	public void calculate(final Clazz clazz, final RanCorrLandscape lscp) {
		double score = correlation(getScores(clazz, lscp));
		if (score == -1) {
			clazz.setRootCauseRatingToFailure();
			return;
		}
		clazz.setRootCauseRating(mapToPropabilityRange(score));
	}

	/*
	 * The correlation function as described in the paper
	 */
	public double correlation(final List<Double> results) {
		final double ownMedian = results.get(0);
		final double inputMedian = results.get(1);
		final double outputMax = results.get(2);

		if (ownMedian == -1) {
			return -1;
		}

		if ((inputMedian > ownMedian) && (outputMax <= ownMedian)) {
			return ((ownMedian + 1) / 2.0);
		} else if ((inputMedian <= ownMedian) && (outputMax > ownMedian)) {
			return ((ownMedian - 1) / 2.0);
		} else {
			return ownMedian;
		}
	}

	/*
	 * The three methods generating the values used in the correlation function
	 */
	private double getOwnMedian(final List<Double> ownScores) {
		if (ownScores.size() == 0) {
			return -1;
		}
		return Maths.unweightedArithmeticMean(ownScores);
	}

	private double getMedianInputScore(final List<Double> inputScores) {
		return Maths.unweightedArithmeticMean(inputScores);
	}

	private double getMaxOutputRating(final List<Double> outputScores) {
		double max = 0;
		for (final double score : outputScores) {
			max = Math.max(score, max);
		}
		return max;
	}

	/**
	 * This function collects the anomaly scores of all operations calling this
	 * class, all operations directly related to the class and the maximum score
	 * of the median of all operations called by this class
	 */
	private List<Double> getScores(final Clazz clazz, final RanCorrLandscape lscp) {
		final List<Double> inputScores = new ArrayList<>();
		final List<Double> outputScores = new ArrayList<>();
		final List<Double> ownScores = new ArrayList<>();
		for (final CommunicationClazz operation : lscp.getOperations()) {
			if (operation.getTarget() == clazz) {
				final List<AnomalyScoreRecord> input = (operation.getSource())
						.getAnomalyScores(lscp);
				inputScores.add(Maths.unweightedArithmeticMean(getValuesFromAnomalyList(input)));
				ownScores.addAll(getValuesFromAnomalyList(operation.getAnomalyScores()));
			}
			if (operation.getSource() == clazz) {
				final List<AnomalyScoreRecord> outputs = (operation.getTarget())
						.getAnomalyScores(lscp);
				outputScores.add(Maths.unweightedArithmeticMean(getValuesFromAnomalyList(outputs)));
			}
		}
		final List<Double> results = new ArrayList<>();
		results.add(getOwnMedian(ownScores));
		results.add(getMedianInputScore(inputScores));
		results.add(getMaxOutputRating(outputScores));
		return results;
	}
}
