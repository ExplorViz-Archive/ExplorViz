package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
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
			clazz.setRootCauseRating(RanCorrConfiguration.RootCauseRatingFailureState);
			return;
		}
		clazz.setRootCauseRating(mapToPropabilityRange(score));
	}

	/**
	 * The correlation function described in the paper
	 *
	 * @param results
	 *            generated by {@Link getScores}
	 * @return the caluclated RCR, -1 if an important value is missing
	 */
	public double correlation(final List<Double> results) {
		final double ownMedian = results.get(0);
		final double inputMedian = results.get(1);
		final double outputMax = results.get(2);
		// If the local median can not be caluclated, return error value
		if (ownMedian == -1) {
			return -1;
		}

		// If there are no incoming or outgoing dependencies, return ownMedian.
		// Not described in Marwede et al
		if ((inputMedian == -1) || (outputMax == -1)) {
			return ownMedian;
		}

		// The regular algorithm as described in Marwede et al
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
	 * Empty list returns defined error value
	 */
	private double getOwnMedian(final List<Double> ownScores) {
		if (ownScores.size() == 0) {
			return -1;
		}
		return Maths.unweightedArithmeticMean(ownScores);
	}

	private double getMedianInputScore(final List<Double> inputScores) {
		if (inputScores.size() == 0) {
			return -1;
		}
		return Maths.unweightedArithmeticMean(inputScores);
	}

	private double getMaxOutputRating(final List<Double> outputScores) {
		if (outputScores.size() == 0) {
			return -1;
		}
		double max = -1;
		for (final double score : outputScores) {
			max = Math.max(score, max);
		}
		return max;
	}

	/**
	 * Generating the scores required for the correlation function
	 *
	 * @param clazz
	 *            The observed Class
	 * @param lscp
	 *            The observed Landscape
	 * @return The required value for the correlation function
	 */
	private List<Double> getScores(final Clazz clazz, final RanCorrLandscape lscp) {
		final List<Double> inputScores = new ArrayList<>();
		final List<Double> outputScores = new ArrayList<>();
		final List<Double> ownScores = new ArrayList<>();
		for (final CommunicationClazz operation : lscp.getOperations()) {
			if (operation.getTarget() == clazz) {
				final List<AnomalyScoreRecord> input = getAnomalyScores(lscp, operation.getSource());
				inputScores.add(Maths.unweightedArithmeticMean(getValuesFromAnomalyList(input)));
				ownScores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
			}
			if (operation.getSource() == clazz) {
				final List<AnomalyScoreRecord> outputs = getAnomalyScores(lscp,
						operation.getTarget());
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