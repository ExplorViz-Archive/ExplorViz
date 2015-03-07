package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.exception.RootCauseThreadingException;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.plugin_server.rootcausedetection.util.Maths;
import explorviz.plugin_server.rootcausedetection.util.RCDThreadPool;
import explorviz.shared.model.Clazz;
import explorviz.shared.model.CommunicationClazz;

/**
 * This class contains an elaborated algorithm to calculate RootCauseRatings. It
 * extends the Mesh Algorithm by a more streamlined, performant alternative.
 *
 * @author Jens Michaelis
 *
 */

public class AdvancedMeshAlgorithm extends AbstractRanCorrAlgorithm {

	// Maps used in the landscape, required for adapting the ExplorViz Landscape
	// to a RanCorr Landscape
	private Map<Integer, ArrayList<Double>> anomalyScores = new ConcurrentHashMap<Integer, ArrayList<Double>>();
	private Map<Integer, Double> RCRs = new ConcurrentHashMap<Integer, Double>();
	private Map<Integer, ArrayList<Integer>> sources = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	private Map<Integer, ArrayList<Integer>> targets = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	private Map<String, Integer> weights = new ConcurrentHashMap<String, Integer>();

	// Defined as in Marwede et al
	private double p = RanCorrConfiguration.PowerMeanExponentOperationLevel;
	private double z = RanCorrConfiguration.DistanceIntensityConstant;
	// Internal error state
	private double errorState = -2.0d;

	// Record used to store the upper Call Relations:
	private class Record {
		int distance = 0;
		int weight = 0;
		double rcr = errorState;
	}

	// Map used to store the upper Call Relations
	private Map<Integer, Record> DistanceData = new ConcurrentHashMap<Integer, Record>();

	// Lists storing which classes have been visited
	private List<Integer> finishedCalleeClasses = new ArrayList<>();
	private List<Integer> finishedCallerClasses = new ArrayList<>();

	/**
	 * Calculate RootCauseRatings in a RanCorrLandscape and uses Anomaly Scores
	 * in the ExplorViz landscape.
	 *
	 * @param lscp
	 *            specified landscape
	 */

	public void calculate(final RanCorrLandscape lscp) {
		generateMaps(lscp);
		generateRCRs();

		// Transfer the generated maps to the Landscape
		lscp.setAnomalyScores(anomalyScores);
		lscp.setRCRs(RCRs);
		lscp.setSources(sources);
		lscp.setTargets(targets);
		lscp.setWeights(weights);

		// Start the final calculation with Threads
		final RCDThreadPool<Clazz, RanCorrLandscape> pool = new RCDThreadPool<>(this,
				RanCorrConfiguration.numberOfThreads, lscp);

		for (final Clazz clazz : lscp.getClasses()) {
			pool.addData(clazz);
		}

		try {
			pool.startThreads();
		} catch (final InterruptedException e) {
			throw new RootCauseThreadingException(
					"AbstractRanCorrAlgorithm#calculate(...): Threading interrupted, broken output.");
		}
	}

	@Override
	public void calculate(Clazz clazz, RanCorrLandscape lscp) {
		final double result = correlation(getScores(clazz.hashCode(), lscp));
		if (result == errorState) {
			clazz.setRootCauseRating(RanCorrConfiguration.RootCauseRatingFailureState);
			return;
		}
		clazz.setRootCauseRating(mapToPropabilityRange(result));
	}

	/**
	 * This method walks trough all operations and generates the maps required
	 * by the algorithm. The information is then written to the landscape in the
	 * caluclate method.
	 *
	 * @param lscp
	 */
	public void generateMaps(final RanCorrLandscape lscp) {
		for (CommunicationClazz operation : lscp.getOperations()) {
			Integer target = operation.getTarget().hashCode();
			Integer source = operation.getSource().hashCode();

			// This part writes the anomalyScores to the specified target
			ArrayList<Double> scores = anomalyScores.get(target);
			if (scores != null) {
				scores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
			} else {
				scores = new ArrayList<Double>();
				scores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
			}
			anomalyScores.put(target, scores);

			// This part writes the hash value of the source class to the
			// targets class list
			ArrayList<Integer> sourcesList = sources.get(target);
			if (sourcesList != null) {
				sourcesList.add(source);
			} else {
				sourcesList = new ArrayList<Integer>();
				sourcesList.add(source);
			}
			sources.put(target, sourcesList);

			// This part writes the hash value of the target class to the
			// sources class list
			ArrayList<Integer> targetsList = targets.get(source);
			if (targetsList != null) {
				targetsList.add(target);
			} else {
				targetsList = new ArrayList<Integer>();
				targetsList.add(target);
			}
			targets.put(source, targetsList);

			// This part writes the weight of the connection to the weights list
			Integer weight = weights.get(source + ";" + target);
			if (weight == null) {
				weight = 0;
			}
			weight = weight + operation.getRequests();
			weights.put(source + ";" + target, weight);
		}
	}

	/**
	 * Calculate the Root Cause Ratings of each class with unweightedPowerMeans
	 * to save time in the final caluclation round
	 */
	public void generateRCRs() {
		for (Integer key : anomalyScores.keySet()) {
			RCRs.put(key, Maths.unweightedPowerMean(anomalyScores.get(key), p));
		}
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
	 * Generates the scores required for calculating the Root Cause Rating
	 *
	 * @param clazz
	 *            The hash of the observed Class
	 * @param lscp
	 *            The observed Landscape
	 *
	 * @return List of all required scores to calculate the Root Cause Rating
	 *         First is the own median, second the Input Median, third the Max
	 *         Output Score
	 */
	private List<Double> getScores(Integer clazz, final RanCorrLandscape lscp) {
		Double outputScore = errorState;

		ArrayList<Integer> targetList = lscp.getTargets().get(clazz);
		if (targetList != null) {
			for (Integer target : targetList) {
				outputScore = getMaxOutputRating(target, lscp, outputScore);
			}
		}

		ArrayList<Integer> sourcesList = lscp.getSources().get(clazz);
		if (sourcesList != null) {
			for (Integer source : sourcesList) {
				getInputClasses(source, clazz, lscp, 1, 0);
			}
		}

		final List<Double> results = new ArrayList<>();
		results.add(getOwnMedian(lscp.getAnomalyScores().get(clazz)));
		results.add(getMedianInputScore());
		results.add(outputScore);
		return results;
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
		if ((ownScores == null) || (ownScores.size() == 0)) {
			return errorState;
		}
		return Maths.unweightedPowerMean(ownScores, p);
	}

	/**
	 * Adds all Callee Classes of the currently observed clazz to the database
	 * trough {@Link addInputClasses}
	 *
	 * @param clazz
	 *            The current observed Class as hash value
	 * @param lscp
	 *            The current observed Landscape
	 */
	private void getInputClasses(Integer source, Integer target, final RanCorrLandscape lscp,
			Integer distance, Integer weight) {
		if (!finishedCallerClasses.contains(source)) {
			finishedCallerClasses.add(source);
			Integer addWeight = lscp.getWeights().get(source + ";" + target);
			if (addWeight == null) {
				addWeight = 0;
			}
			weight = weight + addWeight;
			Double RCR = lscp.getRCRs().get(source);
			if (RCR == null) {
				RCR = errorState;
			}
			addInputClasses(source, weight, RCR, distance);
			for (Integer nextSource : lscp.getSources().get(source)) {
				getInputClasses(nextSource, source, lscp, distance + 1, weight);
			}
		}
	}

	/**
	 * Adds the given values to the weight/distance database
	 *
	 * @param source
	 *            Hash value of the class that needs to be added
	 * @param weight
	 *            Weight that needs to be added
	 * @param rcr
	 *            RCR that needs to be added
	 * @param distance
	 *            Distance that needs to be added
	 */
	private void addInputClasses(final Integer source, final int weight, final Double rcr,
			final int distance) {
		Record rec = DistanceData.get(source);
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
		DistanceData.put(source, rec);
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
	private double getMaxOutputRating(final Integer target, final RanCorrLandscape lscp, double max) {
		if (finishedCalleeClasses.contains(target)) {
			return max;
		} else {
			finishedCalleeClasses.add(target);
			final double newValue = lscp.getRCRs().get(target);
			max = Math.max(max, newValue);
			ArrayList<Integer> targets = lscp.getTargets().get(target);
			if (targets != null) {
				for (Integer key : targets) {
					max = Math.max(max, getMaxOutputRating(key, lscp, max));
				}
			}
			return max;
		}
	}

}
