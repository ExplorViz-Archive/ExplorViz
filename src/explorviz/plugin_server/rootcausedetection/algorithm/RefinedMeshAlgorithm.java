package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.exception.RootCauseThreadingException;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.plugin_server.rootcausedetection.util.Maths;
import explorviz.plugin_server.rootcausedetection.util.RCDThreadPool;
import explorviz.shared.model.*;

/**
 * This class contains an elaborated algorithm to calculate RootCauseRatings. It
 * uses the data of all directly and indirectly connected classes and advanced
 * power means for aggregation.
 *
 * @author Jens Michaelis, Christian Wiechmann
 *
 */
public class RefinedMeshAlgorithm extends AbstractRanCorrAlgorithm {

	// Maps used in the landscape, required for adapting the ExplorViz Landscape
	// to a RanCorr Landscape
	private Map<Integer, ArrayList<Double>> anomalyScores;
	private Map<Integer, ArrayList<Double>> positiveAnomalyScores;
	private Map<Integer, ArrayList<Double>> negativeAnomalyScores;
	private Map<Integer, Double> positiveRCRs;
	private Map<Integer, Double> negativeRCRs;
	private Map<Integer, Double> weightedRCRs;
	private Map<Integer, ArrayList<Integer>> sources;
	private Map<Integer, ArrayList<Integer>> targets;
	private Map<String, Integer> weights;

	// Defined as in Marwede et al
	private double p = RanCorrConfiguration.PowerMeanExponentClassLevel;
	private double z = RanCorrConfiguration.DistanceIntensityConstant;

	private double posWeight = RanCorrConfiguration.RefinedNegativeFactor;
	private double buffer = RanCorrConfiguration.RefinedBuffer;

	// Internal error state
	private double errorState = -2.0d;

	// Record used to store the upper Call Relations:
	private class Record {
		int distance = 0;
		int weight = 0;
		double rcr = errorState;
	}

	/**
	 * Calculate RootCauseRatings in a RanCorrLandscape and uses Anomaly Scores
	 * in the ExplorViz landscape.
	 *
	 * @param lscp
	 *            specified landscape
	 */
	public void calculate(final RanCorrLandscape lscp) {
		// Reinitialize the landscape data
		anomalyScores = new ConcurrentHashMap<Integer, ArrayList<Double>>();
		positiveAnomalyScores = new ConcurrentHashMap<Integer, ArrayList<Double>>();
		negativeAnomalyScores = new ConcurrentHashMap<Integer, ArrayList<Double>>();
		weightedRCRs = new ConcurrentHashMap<Integer, Double>();
		positiveRCRs = new ConcurrentHashMap<Integer, Double>();
		negativeRCRs = new ConcurrentHashMap<Integer, Double>();
		sources = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
		targets = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
		weights = new ConcurrentHashMap<String, Integer>();

		// Generate the landscape data
		generateMaps(lscp);
		separateAnomalyScores();
		generateWeightedRCRs();
		generatePositiveRCRs();
		generateNegativeRCRs();

		// Start the final calculation with Threads
		final RCDThreadPool<Clazz> pool = new RCDThreadPool<>(this,
				RanCorrConfiguration.numberOfThreads);

		for (final Clazz clazz : lscp.getClasses()) {
			pool.addData(clazz);
		}

		try {
			pool.startThreads();
		} catch (final InterruptedException e) {
			throw new RootCauseThreadingException(
					"MeshRanCorrAlgorithm#calculate(...): Threading interrupted, broken output.");
		}
	}

	/**
	 * The calculation method on class level started by the Thread Pool and
	 * setting the root cause rating in the observed class
	 */
	@Override
	public void calculate(Clazz clazz) {

		List<Double> results = getScores(clazz.hashCode());

		if (results == null) {
			clazz.setRootCauseRating(RanCorrConfiguration.RootCauseRatingFailureState);
			return;
		}

		final double result = correlation(results);

		if ((result < 0) || (result > 1)) {
			clazz.setRootCauseRating(RanCorrConfiguration.RootCauseRatingFailureState);
			return;
		}

		clazz.setRootCauseRating(mapToPropabilityRange(result));
	}

	/**
	 * This method walks trough all operations and generates the maps required
	 * by the algorithm.
	 *
	 * @param lscp
	 */
	private void generateMaps(final RanCorrLandscape lscp) {
		if (lscp.getCommunications() != null) {
			for (Communication comm : lscp.getCommunications()) {
				Integer target = comm.getTargetClazz().hashCode();
				Integer source = comm.getSourceClazz().hashCode();

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

				// This part writes the weight of the connection to the weights
				// list
				Integer weight = weights.get(source + ";" + target);
				if (weight == null) {
					weight = 1;
				}
				weight = weight + comm.getRequests();
				weights.put(source + ";" + target, weight);
			}
		}

		if (lscp.getOperations() != null) {
			for (CommunicationClazz operation : lscp.getOperations()) {

				Integer target = operation.getTarget().hashCode();
				ArrayList<Integer> TargetList = targets.get(target);
				// Integer source = operation.getSource().hashCode();
				if (TargetList != null) {
					for (Integer targetClass : TargetList) {
						ArrayList<Double> scores = anomalyScores.get(targetClass);
						if (scores != null) {
							scores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
						} else {
							scores = new ArrayList<Double>();
							scores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
						}
						anomalyScores.put(targetClass, scores);
					}
					// //
					// // Integer target = operation.getTarget().hashCode();
					// // ArrayList<Double> scores = anomalyScores.get(target);
					// // if (scores != null) {
					// //
					// scores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
					// // } else {
					// // scores = new ArrayList<Double>();
					// //
					// scores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
					// // }
					// // anomalyScores.put(target, scores);
				}
			}
		}
	}

	/**
	 * Split the anomaly scores into positive and negative values
	 */
	public void separateAnomalyScores() {
		for (Integer key : anomalyScores.keySet()) {
			ArrayList<Double> scores = anomalyScores.get(key);
			ArrayList<Double> positiveScores = new ArrayList<>();
			ArrayList<Double> negativeScores = new ArrayList<>();
			for (Double score : scores) {
				if (score >= 0) {
					positiveScores.add(score);
				} else {
					negativeScores.add(score);
				}
			}
			positiveAnomalyScores.put(key, positiveScores);
			negativeAnomalyScores.put(key, negativeScores);
		}
	}

	/**
	 * Calculate the Root Cause Ratings of each class with weightedPowerMeans to
	 * save time in the final correlation phase using all anomaly scores
	 */
	public void generateWeightedRCRs() {
		for (Integer key : anomalyScores.keySet()) {
			ArrayList<Double> scores = new ArrayList<>();
			ArrayList<Double> weights = new ArrayList<>();

			scores.addAll(negativeAnomalyScores.get(key));
			int size = scores.size();
			scores.addAll(positiveAnomalyScores.get(key));

			// Generate weights as defined in the config for all positive scores
			for (int i = 0; i < scores.size(); i++) {
				if (i < size) {
					weights.add(1.0d);
				} else {
					weights.add(posWeight);
				}
			}
			weightedRCRs.put(key, Maths.weightedPowerMean(scores, weights, p));
		}
	}

	/**
	 * Calculate the Root Cause Ratings of each class with unweightedPowerMeans
	 * to save time in the final correlation phase using positive anomaly scores
	 */
	public void generatePositiveRCRs() {
		for (Integer key : anomalyScores.keySet()) {
			ArrayList<Double> scores = positiveAnomalyScores.get(key);
			if (scores.size() > 0) {
				for (int i = 0; i < scores.size(); i++) {
					scores.set(i, (scores.get(i) * 2) - 1);
				}
			}
			ArrayList<Double> negativeScores = negativeAnomalyScores.get(key);
			if (negativeScores.size() > 0) {
				for (int i = 0; i < negativeScores.size(); i++) {
					if (negativeScores.get(i) >= -buffer) {
						scores.add((Math.abs(negativeScores.get(i)) * 2) - 1);
					}
				}
			}
			if (scores.size() == 0) {
				positiveRCRs.put(key, errorState);
			} else {
				positiveRCRs.put(key, Maths.unweightedPowerMean(scores, p));
			}
		}
	}

	/**
	 * Calculate the Root Cause Ratings of each class with unweightedPowerMeans
	 * to save time in the final correlation phase using negative anomaly scores
	 */
	public void generateNegativeRCRs() {
		for (Integer key : anomalyScores.keySet()) {
			ArrayList<Double> scores = negativeAnomalyScores.get(key);
			if (scores.size() > 0) {
				for (int i = 0; i < scores.size(); i++) {
					scores.set(i, (Math.abs(scores.get(i)) * 2) - 1);
				}
			}
			ArrayList<Double> positiveScores = positiveAnomalyScores.get(key);
			if (positiveScores.size() > 0) {
				for (int i = 0; i < positiveScores.size(); i++) {
					if (positiveScores.get(i) <= buffer) {
						scores.add((positiveScores.get(i) * 2) - 1);
					}
				}
			}
			if (scores.size() == 0) {
				negativeRCRs.put(key, errorState);
			} else {
				negativeRCRs.put(key, Maths.unweightedPowerMean(scores, p));
			}
		}
	}

	/**
	 * Returns the Root Cause Rating as described in Marwede et al. Added a
	 * return of the own median if there are no upper or lower dependencies.
	 *
	 * @param results
	 *            List of results generated in {@Link getScores}
	 * @return calculated Root Cause Rating
	 */
	public double correlation(final List<Double> results) {
		if ((results == null) || (results.size() != 3)) {
			return errorState;
		}

		final Double ownMedian = results.get(0);
		final Double inputMedian = results.get(1);
		final Double outputMax = results.get(2);

		if ((inputMedian == errorState) || (outputMax == errorState) || (ownMedian == errorState)) {
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
	 *
	 * @return List of all required scores to calculate the Root Cause Rating
	 *         First is the own median, second the Input Median, third the Max
	 *         Output Score
	 */
	private List<Double> getScores(Integer clazz) {

		final List<Double> results = new ArrayList<>();

		Double ownMedian = errorState;

		final Double sign = weightedRCRs.get(clazz);

		if (sign != null) {
			if (sign >= 0) {
				ownMedian = positiveRCRs.get(clazz);
			} else {
				ownMedian = negativeRCRs.get(clazz);
			}
		}

		if ((ownMedian != null) && (sign != null)) {
			results.add(ownMedian);
		} else {
			results.add(errorState);
			results.add(errorState);
			results.add(errorState);
			return results;
		}

		// Map used to store the upper Call Relations
		Map<Integer, Record> distanceData = new ConcurrentHashMap<Integer, Record>();

		// List of all checked sources classes
		ArrayList<Integer> finishedCallerClasses = new ArrayList<>();

		ArrayList<Integer> sourcesList = sources.get(clazz);
		if (sourcesList != null) {
			for (Integer source : sourcesList) {
				getInputClasses(source, clazz, 1, 0, distanceData, finishedCallerClasses, sign);
			}
		}
		results.add(getMedianInputScore(distanceData));

		// Default value, kept if no target classes are found
		Double outputScore = errorState;

		// List of all checked target classes
		ArrayList<Integer> finishedCalleeClasses = new ArrayList<>();

		// Run trough all Callees of the observed classes and get the maximum
		// rating
		ArrayList<Integer> targetList = targets.get(clazz);
		if (targetList != null) {
			for (Integer target : targetList) {
				outputScore = getMaxOutputRating(target, outputScore, finishedCalleeClasses, sign);
			}
		}
		results.add(outputScore);

		return results;
	}

	/**
	 * Adds all Callee Classes of the currently observed clazz to the database
	 * trough {@Link addInputClasses}
	 *
	 * @param source
	 *            hash value of the source class
	 * @param target
	 *            hash value of the target class
	 * @param distance
	 *            the current distance
	 * @param weight
	 *            the current weight
	 */
	private void getInputClasses(Integer source, Integer target, Integer distance, Integer weight,
			Map<Integer, Record> distanceData, ArrayList<Integer> finishedCallerClasses, Double sign) {
		if (!finishedCallerClasses.contains(source)) {
			finishedCallerClasses.add(source);

			Integer addWeight = weights.get(source + ";" + target);
			if (addWeight == null) {
				addWeight = 0;
			}

			weight = weight + addWeight;

			Double ownSign = weightedRCRs.get(source);
			Double RCR = errorState;

			if (ownSign != null) {
				if ((sign >= 0) && (ownSign >= 0)) {
					RCR = positiveRCRs.get(source);
				} else if ((sign < 0) && (ownSign < 0)) {
					RCR = negativeRCRs.get(source);
				} else {
					return;
				}
			}

			if (RCR == null) {
				RCR = errorState;
			}
			addInputClasses(source, weight, RCR, distance, distanceData);
			ArrayList<Integer> sourcesList = sources.get(source);
			if (sourcesList != null) {
				for (Integer nextSource : sourcesList) {
					getInputClasses(nextSource, source, distance + 1, weight, distanceData,
							finishedCallerClasses, sign);
				}
			}
		}
	}

	/**
	 * Adds the given values to the weight/distance data
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
			final int distance, Map<Integer, Record> distanceData) {
		Record rec = distanceData.get(source);
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
		distanceData.put(source, rec);
	}

	/**
	 * Calculating the Callee-related scores as defined in Marwede et al The
	 * values are provided by the Distance Graph generated trough {@Link
	 * getScores}
	 *
	 * @return calculated Median of the input scores
	 */
	private double getMedianInputScore(Map<Integer, Record> distanceData) {
		List<Double> scores = new ArrayList<Double>();
		List<Integer> weights = new ArrayList<Integer>();
		List<Integer> distances = new ArrayList<Integer>();
		for (Integer key : distanceData.keySet()) {
			Record rec = distanceData.get(key);
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
				return errorState;
			}
			return result;
		}
	}

	/**
	 * Calculates the maxixum called Root Cause Rating as described in Marwede
	 * et al. It compares the current value to all connected Callees and returns
	 * the highest value of all classes with the same sign.
	 *
	 * @param target
	 *            Hash value of the observed target class
	 * @param max
	 *            The current maximum, errorState if none is found
	 *
	 * @return Maximum Root Cause Rating of all Callees of the observed class or
	 *         the observed class
	 */
	private double getMaxOutputRating(final Integer target, double max,
			ArrayList<Integer> finishedCalleeClasses, Double sign) {
		if (finishedCalleeClasses.contains(target)) {
			return max;
		} else {
			finishedCalleeClasses.add(target);

			Double newValue = errorState;
			Double ownSign = weightedRCRs.get(target);

			if (ownSign != null) {
				if ((sign >= 0) && (ownSign >= 0)) {
					newValue = positiveRCRs.get(target);
				} else if ((sign < 0) && (ownSign < 0)) {
					newValue = negativeRCRs.get(target);
				} else {
					return max;
				}
			} else {
				return max;
			}

			if (newValue == null) {
				return max;
			}

			max = Math.max(max, newValue);
			ArrayList<Integer> targetList = targets.get(target);
			if (targetList != null) {
				for (Integer key : targetList) {
					max = Math.max(max, getMaxOutputRating(key, max, finishedCalleeClasses, sign));
				}
			}
			return max;
		}
	}
}
