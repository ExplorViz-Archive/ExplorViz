package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.model.AnomalyScoreRecord;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.plugin_server.rootcausedetection.util.DistanceGraph;
import explorviz.plugin_server.rootcausedetection.util.Maths;
import explorviz.shared.model.Clazz;
import explorviz.shared.model.CommunicationClazz;

/**
 * This class contains an elaborated algorithm to calculate RootCauseRatings. It
 * uses the data of the entire landscape to achieve this and reaches the best
 * results while somewhat sacrificing performance.
 *
 * @author Christian Claus Wiechmann, Jens Michaelis
 *
 */
public class MeshAlgorithm extends AbstractRanCorrAlgorithm {

	double p = 0.2;
	double z = 1;

	private DistanceGraph database;
	private final List<Integer> finishedCalleeClasses = new ArrayList<>();
	private final List<Integer> finishedCallerClasses = new ArrayList<>();

	@Override
	public void calculate(final Clazz clazz, final RanCorrLandscape lscp) {
		database = new DistanceGraph(clazz.hashCode());
		final double result = correlation(getScores(clazz, lscp));
		if (result == -1.0) {
			clazz.setRootCauseRatingToFailure();
			return;
		}
		clazz.setRootCauseRating(mapToPropabilityRange(result));

	}

	public double correlation(final List<Double> results) {
		final double ownMedian = results.get(0);
		final double inputMedian = results.get(1);
		final double outputMax = results.get(2);

		if (ownMedian == -1.0) {
			return -1.0;
		}

		if ((inputMedian == -1.0) || (outputMax == -1.0)) {
			return ownMedian;
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
			return -1.0;
		}
		return Maths.unweightedPowerMean(ownScores, p);
	}

	private double getMedianInputScore() {
		final List<Double> scores = database.getRCRs();
		final List<Integer> weights = database.getWeights();
		final List<Integer> distances = database.getDistances();
		final List<Double> powerWeights = new ArrayList<Double>();
		final List<Double> powerScores = new ArrayList<Double>();

		for (int i = 0; i < scores.size(); i++) {
			if (scores.get(i) != RanCorrConfiguration.RootCauseRatingFailureState) {
				powerScores.add(scores.get(i));
				powerWeights.add(weights.get(i) / Math.pow(distances.get(i), z));
			}
		}
		if (powerScores.size() == 0) {
			return -1;
		} else {
			return Maths.weightedPowerMean(powerScores, powerWeights, 1);
		}
	}

	/**
	 * This function collects the anomaly scores of all operations calling this
	 * class (directly and indirectly), all operations directly and indirectly
	 * related to the class and the maximum score of the median of all
	 * operations called by this class
	 */
	private List<Double> getScores(final Clazz clazz, final RanCorrLandscape lscp) {
		Double outputScore = -1.0;
		final List<Double> ownScores = new ArrayList<>();
		for (final CommunicationClazz operation : lscp.getOperations()) {
			if (operation.getTarget() == clazz) {
				getInputClasses(operation.getSource(), lscp);
				ownScores.addAll(getValuesFromAnomalyList(operation.getAnomalyScores()));
			}
			if (operation.getSource() == clazz) {
				outputScore = getMaxOutputRating(operation.getTarget(), lscp, outputScore);
			}
		}
		final List<Double> results = new ArrayList<>();
		results.add(getOwnMedian(ownScores));
		results.add(getMedianInputScore());
		results.add(outputScore);
		return results;
	}

	/*
	 * If this Callee Class has not yet been visited, calculate anomaly rating
	 * of current class, compare it to the current max, recursively check all
	 * Callee Classes of the current class
	 */
	private double getMaxOutputRating(final Clazz clazz, final RanCorrLandscape lscp, double max) {
		if (finishedCalleeClasses.contains(clazz.hashCode())) {
			return max;
		} else {
			finishedCalleeClasses.add(clazz.hashCode());
			final List<AnomalyScoreRecord> outputs = clazz.getAnomalyScores(lscp);
			final double newValue = Maths.unweightedPowerMean(getValuesFromAnomalyList(outputs), p);
			max = Math.max(max, newValue);
			for (final CommunicationClazz operation : lscp.getOperations()) {
				if (operation.getSource() == clazz) {
					max = Math.max(max, getMaxOutputRating(operation.getTarget(), lscp, max));
				}
			}
			return max;
		}
	}

	/*
	 * This function collects all Caller-Classes
	 */
	private void getInputClasses(final Clazz clazz, final RanCorrLandscape lscp) {
		if (!finishedCallerClasses.contains(clazz.hashCode())) {
			finishedCallerClasses.add(clazz.hashCode());
			for (final CommunicationClazz operation : lscp.getOperations()) {
				if (operation.getTarget() == clazz) {
					addInputClasses(operation.getSource(), clazz.hashCode(), lscp,
							operation.getRequests());
					getInputClasses(operation.getSource(), lscp);
				}
			}
		}
	}

	/*
	 *
	 */
	private void addInputClasses(final Clazz source, final int targetHash,
			final RanCorrLandscape lscp, final int weight) {
		int hash = database.addRecord(source.hashCode(), targetHash);
		if (hash != -1) {
			final List<AnomalyScoreRecord> outputs = source.getAnomalyScores(lscp);
			double rcr = Maths.unweightedPowerMean(getValuesFromAnomalyList(outputs), p);
			database.addWeightRCR(hash, weight, rcr);
		}
	}

}
