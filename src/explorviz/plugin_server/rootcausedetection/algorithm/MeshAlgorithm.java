package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_server.rootcausedetection.model.*;
import explorviz.plugin_server.rootcausedetection.util.Maths;

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

	private final List<RanCorrClass> finishedCalleeClasses = new ArrayList<>();
	private final List<RanCorrClass> finishedCallerClasses = new ArrayList<>();

	@Override
	public void calculate(final RanCorrClass clazz, final RanCorrLandscape lscp) {
		// TODO Auto-generated method stub

	}

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
		return Maths.unweightedPowerMean(ownScores, p);
	}

	private double getMedianInputScore(final List<RanCorrClass> inputScores) {
		// TODO RCD: insert correct functionality here...
		return 0.0d;
	}

	/**
	 * This function collects the anomaly scores of all operations calling this
	 * class (directly and indirectly), all operations directly and indirectly
	 * related to the class and the maximum score of the median of all
	 * operations called by this class
	 */
	private List<Double> getScores(final RanCorrClass clazz, final RanCorrLandscape lscp) {
		final List<RanCorrClass> inputScores = new ArrayList<>();
		Double outputScore = 0.0;
		final List<Double> ownScores = new ArrayList<>();
		getDistanceAndWeights(clazz, lscp, 1, 1);
		for (final RanCorrOperation operation : lscp.getOperations()) {
			if (operation.getTarget() == clazz) {
				getInputClasses((RanCorrClass) operation.getSource(), lscp);
				ownScores.addAll(getValuesFromAnomalyList(operation.getAnomalyScores()));
			}
			if (operation.getSource() == clazz) {
				outputScore = getMaxOutputRating((RanCorrClass) operation.getTarget(), lscp,
						outputScore);
			}
		}
		final List<Double> results = new ArrayList<>();
		results.add(getOwnMedian(ownScores));
		results.add(getMedianInputScore(inputScores));
		results.add(outputScore);
		return results;
	}

	/*
	 * If this Callee Class has not yet been visited, calculate anomaly rating
	 * of current class, compare it to the current max, recursively check all
	 * Callee Classes of the current class
	 */
	private double getMaxOutputRating(final RanCorrClass clazz, final RanCorrLandscape lscp,
			double max) {
		if (finishedCalleeClasses.contains(clazz)) {
			return max;
		} else {
			finishedCalleeClasses.add(clazz);
			final List<AnomalyScoreRecord> outputs = clazz.getAnomalyScores(lscp);
			final double newValue = Maths.unweightedPowerMean(getValuesFromAnomalyList(outputs),
					0.2);
			max = Math.max(max, newValue);
			for (final RanCorrOperation operation : lscp.getOperations()) {
				if (operation.getSource() == clazz) {
					max = Math.max(max,
							getMaxOutputRating((RanCorrClass) operation.getTarget(), lscp, max));
				}
			}
			return max;
		}
	}

	/*
	 * This function goes up in the call relationship class and updates all
	 * distances and weights if a shorter path is found. %TODO Weight
	 * calculation
	 */
	private void getDistanceAndWeights(final RanCorrClass clazz, final RanCorrLandscape lscp,
			final int distance, final float weight) {
		clazz.setDistance(distance);
		clazz.setWeight(weight);
		for (final RanCorrOperation operation : lscp.getOperations()) {
			if (operation.getTarget() == clazz) {
				final int sourceDist = ((RanCorrClass) operation.getSource()).getDistance();
				if ((sourceDist == 0) || (distance < (sourceDist - 1))) {
					getDistanceAndWeights((RanCorrClass) operation.getSource(), lscp, distance + 1,
							weight);
				}
			}
		}
	}

	private void getInputClasses(final RanCorrClass clazz, final RanCorrLandscape lscp) {
		final RanCorrClass result = clazz;
		result.setRootCauseRating(Maths.unweightedPowerMean(
				getValuesFromAnomalyList(result.getAnomalyScores(lscp)), p));
		finishedCallerClasses.add(result);
		clazz.setDistance(0);
		clazz.setWeight(0.0);
		for (final RanCorrOperation operation : lscp.getOperations()) {
			if ((operation.getTarget() == clazz)
					&& !(finishedCallerClasses.contains(operation.getSource()))) {
				getInputClasses((RanCorrClass) operation.getSource(), lscp);
			}
		}
	}
}
