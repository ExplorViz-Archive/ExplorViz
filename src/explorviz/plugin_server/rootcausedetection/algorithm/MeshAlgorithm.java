package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.model.AnomalyScoreRecord;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
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

	// Defined as in Marwede et al
	double p = RanCorrConfiguration.PowerMeanExponentOperationLevel;
	double z = RanCorrConfiguration.DistanceIntensityConstant;
	// Internal error state
	double errorState = -2.0d;

	// Record used to store the upper Call Relations:
	private class Record {
		int distance = 0;
		int weight = 0;
		double rcr = errorState;
	}

	// Map used to store the upper Call Relations
	private Map<Integer, Record> DistanceData = new ConcurrentHashMap<Integer, Record>();

	private final List<Integer> finishedCalleeClasses = new ArrayList<>();
	private final List<Integer> finishedCallerClasses = new ArrayList<>();

	@Override
	public void calculate(final Clazz clazz, final RanCorrLandscape lscp) {
		final double result = correlation(getScores(clazz, lscp));
		if (result == errorState) {
			clazz.setRootCauseRating(RanCorrConfiguration.RootCauseRatingFailureState);
			return;
		}
		clazz.setRootCauseRating(mapToPropabilityRange(result));
	}

	/**
	 * Returns the Root Cause Rating as described in Malwede et al.
	 *
	 * @param results
	 *            List of results generated in {@Link getScores}
	 * @return calculated Root Cause Rating
	 */
	public double correlation(final List<Double> results) {
		final double ownMedian = results.get(0);
		final double inputMedian = results.get(1);
		final double outputMax = results.get(2);

		if (ownMedian == errorState) {
			return errorState;
		}

		if ((inputMedian == errorState) || (outputMax == errorState)) {
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

	/**
	 * Calculating the own root cause rating as defined in Marwede et al
	 *
	 * @param ownScores
	 *            List of anomaly scores
	 *
	 * @return calculated root cause rating, errorState if no anomaly Scores
	 *         exists
	 */
	private double getOwnMedian(final List<Double> ownScores) {
		if (ownScores.size() == 0) {
			return errorState;
		}
		return Maths.unweightedPowerMean(ownScores, p);
	}

	/**
	 * Calculating the Callee-related Scores as defined in Marwede et al The
	 * values are provided by the Distance Graph generated trough {@Link
	 * getScores}
	 *
	 * @return calculated Median of the input scores
	 */
	private double getMedianInputScore() {
		List<Double> scores = new ArrayList<Double>();
		List<Integer> weights = new ArrayList<Integer>();
		List<Integer> distances = new ArrayList<Integer>();
		for (Integer key : DistanceData.keySet()) {
			Record rec = DistanceData.get(key);
			scores.add(rec.rcr);
			weights.add(rec.weight);
			distances.add(rec.distance);
		}
		final List<Double> powerWeights = new ArrayList<Double>();
		final List<Double> powerScores = new ArrayList<Double>();

		if (scores.size() == 0) {
			return errorState;
		}

		for (int i = 0; i < scores.size(); i++) {
			if ((scores.get(i) != errorState) && (scores.size() == weights.size())
					&& (scores.size() == distances.size())) {
				powerScores.add(scores.get(i));
				powerWeights.add(weights.get(i) / Math.pow(distances.get(i), z));
			}
		}
		if ((powerScores.size() == 0) || (powerScores.size() != powerWeights.size())) {
			return errorState;
		} else {
			Double result = Maths.weightedPowerMean(powerScores, powerWeights, 1);
			if (result == null) {
				result = errorState;
			}
			return result;
		}
	}

	/**
	 * Generates the scores required for calculating the Root Cause Rating
	 *
	 * @param clazz
	 *            The observed Class
	 * @param lscp
	 *            The observed Landscape
	 *
	 * @return List of all required scores to calculate the Root Cause Rating
	 *         First is the own median, second the Input Median, third the Max
	 *         Output Score
	 */
	private List<Double> getScores(final Clazz clazz, final RanCorrLandscape lscp) {
		Double outputScore = errorState;
		final List<Double> ownScores = new ArrayList<>();

		for (final CommunicationClazz operation : lscp.getOperations()) {
			if (operation.getTarget() == clazz) {
				getInputClasses(operation.getSource(), lscp, 1, 0);
				ownScores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
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

	/**
	 * Calculates the maxixum called Root Cause Rating as described in Marwede
	 * et al
	 *
	 * @param clazz
	 *            The observed Class
	 * @param lscp
	 *            The observed Landscape
	 * @param max
	 *            The current maximum, -1 as error value
	 *
	 * @return calculated Root Cause Rating
	 */
	private double getMaxOutputRating(final Clazz clazz, final RanCorrLandscape lscp, double max) {
		if (finishedCalleeClasses.contains(clazz.hashCode())) {
			return max;
		} else {
			finishedCalleeClasses.add(clazz.hashCode());
			final List<AnomalyScoreRecord> outputs = getAnomalyScores(lscp, clazz);
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

	/**
	 * Adds all Callee Classes of the currently observed clazz to the database
	 * trough {@Link addInputClasses}
	 *
	 * @param clazz
	 *            The current observed Class
	 * @param lscp
	 *            The current observed Landscape
	 * @param distance
	 *            The current distance from the observed class
	 * @param weight
	 *            The current weight of all edges from the observed class
	 */
	private void getInputClasses(final Clazz clazz, final RanCorrLandscape lscp, Integer distance,
			Integer weight) {
		int hash = clazz.hashCode();
		if (!finishedCallerClasses.contains(hash)) {
			finishedCallerClasses.add(hash);
			for (final CommunicationClazz operation : lscp.getOperations()) {
				if (operation.getTarget() == clazz) {
					addInputClasses(operation.getSource(), lscp, weight + operation.getRequests(),
							distance);
					getInputClasses(operation.getSource(), lscp, weight + operation.getRequests(),
							distance + 1);
				}
			}
		}
	}

	/**
	 * Helper for {@Link getInputClasses}, adds the given Class to the
	 * database if distance is shorter or distance is equal and weight is lower
	 * compared to allready inserted path
	 *
	 * @param source
	 *            The Callee that needs to be added
	 * @param lscp
	 *            The current observed landscape
	 * @param weight
	 *            The weight of the caller/callee relation
	 * @param distance
	 *            The distance of the caller/callee relation
	 */
	private void addInputClasses(final Clazz source, final RanCorrLandscape lscp,
			final Integer weight, final Integer distance) {
		Record rec = DistanceData.get(source.hashCode());
		final List<AnomalyScoreRecord> outputs = getAnomalyScores(lscp, source);
		double rcr = errorState;
		if (outputs.size() != 0) {
			rcr = Maths.unweightedPowerMean(getValuesFromAnomalyList(outputs), p);
		}
		if (rec != null) {
			if (rec.distance > distance) {
				rec.distance = distance;
				rec.weight = weight;
				rec.rcr = rcr;
			} else if ((rec.distance == distance) && (rec.weight < weight)) {
				rec.distance = distance;
				rec.weight = weight;
				rec.rcr = rcr;
			}
		} else {
			rec = new Record();
			rec.distance = distance;
			rec.weight = weight;
			rec.rcr = rcr;
		}
		DistanceData.put(source.hashCode(), rec);
	}

}