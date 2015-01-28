package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_server.rootcausedetection.model.*;
import explorviz.plugin_server.rootcausedetection.util.Maths;

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
	public void calculate(final RanCorrClass clazz, final RanCorrLandscape lscp) {
		clazz.setRootCauseRating(correlation(getScores(clazz, lscp)));
	}

	/*
	 * The correlation function as described in the paper
	 */
	public double correlation(final List<Double> results) {
		final double ownMedian = results.get(0);
		final double inputMedian = results.get(1);
		final double outputMax = results.get(2);

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
	private List<Double> getScores(final RanCorrClass clazz, final RanCorrLandscape lscp) {
		final List<Double> inputScores = new ArrayList<>();
		final List<Double> outputScores = new ArrayList<>();
		final List<Double> ownScores = new ArrayList<>();
		for (final RanCorrOperation operation : lscp.getOperations()) {
			if (operation.getTarget() == clazz) {
				inputScores.addAll(getValuesFromAnomalyList(((RanCorrClass) operation.getSource())
						.getAnomalyScores(lscp)));
				ownScores.addAll(getValuesFromAnomalyList(operation.getAnomalyScores()));
			}
			if (operation.getSource() == clazz) {
				final List<AnomalyScoreRecord> outputs = ((RanCorrClass) operation.getTarget())
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
