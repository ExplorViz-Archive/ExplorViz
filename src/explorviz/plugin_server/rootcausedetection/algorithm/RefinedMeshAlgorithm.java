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
	private Map<Integer, Double> RCRs;
	private Map<Integer, ArrayList<Integer>> sources;
	private Map<Integer, ArrayList<Integer>> targets;
	private Map<String, Integer> weights;
	private Map<Integer, Boolean> isPositive;

	// Defined as in Marwede et al
	private double p = RanCorrConfiguration.PowerMeanExponentClassLevel;
	private double z = RanCorrConfiguration.DistanceIntensityConstant;
	private double errorState = RanCorrConfiguration.RootCauseRatingFailureState;
	private boolean opTarget = RanCorrConfiguration.OperationsTarget;
	private double overloadWeight = RanCorrConfiguration.RefinedNegativeFactor;

	// Internal error state
	private double internalErrorState = -2.0d;

	// Record used to store the upper Call Relations:
	private class Record {
		int distance = 0;
		int weight = 0;
		double rcr = internalErrorState;
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
		RCRs = new ConcurrentHashMap<Integer, Double>();
		sources = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
		targets = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
		weights = new ConcurrentHashMap<String, Integer>();
		isPositive = new ConcurrentHashMap<Integer, Boolean>();

		// Generate the landscape data
		generateMaps(lscp);
		generateSigns();
		generateRCRs();

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

		clazz.setIsRankingPositive(isRCRPositive(clazz.hashCode()));

		if (result == internalErrorState) {
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

				int target = operation.getTarget().hashCode();
				ArrayList<Integer> targetList = new ArrayList<Integer>();
				if (opTarget) {
					targetList.add(target);
				} else {
					targetList = targets.get(target);
				}

				if (targetList != null) {
					for (Integer targetClass : targetList) {
						ArrayList<Double> scores = anomalyScores.get(targetClass);
						if (scores != null) {
							scores.addAll(getValuesFromAnomalyList(getUnchangedAnomalyScores(operation)));
						} else {
							scores = new ArrayList<Double>();
							scores.addAll(getValuesFromAnomalyList(getUnchangedAnomalyScores(operation)));
						}
						anomalyScores.put(targetClass, scores);
					}
				}
			}
		}
	}

	/**
	 * Calculate the Root Cause Ratings of each class with unweightedPowerMeans
	 * to save time in the final correlation phase
	 */
	private void generateRCRs() {
		for (Integer key : anomalyScores.keySet()) {
			ArrayList<Double> values = new ArrayList<Double>();
			boolean positive = true;
			if (isPositive.get(key) != null) {
				positive = isPositive.get(key);
			}
			for (double value : anomalyScores.get(key)) {
				if (positive) {
					if (value < 0.0d) {
						values.add(-1.0d);
					} else {
						values.add((value * 2) - 1);
					}
				} else {
					if (value < 0.0d) {
						values.add((Math.abs(value) * 2) - 1);
					} else {
						values.add((-1.0d));
					}
				}
			}
			RCRs.put(key, Maths.unweightedPowerMean(values, p));
		}
	}

	/**
	 * Calculates the kind of an anomaly (positive or negative)
	 */
	private void generateSigns() {
		for (Integer key : anomalyScores.keySet()) {
			ArrayList<Double> weight = new ArrayList<Double>();
			ArrayList<Double> values = anomalyScores.get(key);
			for (double value : values) {
				if (value < 0.0d) {
					weight.add(1.0d);
				} else {
					weight.add(overloadWeight);
				}
			}
			if (Maths.weightedPowerMean(values, weight, p) >= 0.0d) {
				isPositive.put(key, true);
			} else {
				isPositive.put(key, false);
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
	private double correlation(final List<Double> results) {
		if ((results == null) || (results.size() != 3)) {
			return internalErrorState;
		}

		final double ownMedian = results.get(0);
		final double inputMedian = results.get(1);
		final double outputMax = results.get(2);

		if ((inputMedian == internalErrorState) || (outputMax == internalErrorState)
				|| (ownMedian == internalErrorState)) {
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

		// The own positive sign
		final Boolean ownSign = isPositive.get(clazz);
		if (ownSign == null) {
			results.add(internalErrorState);
			results.add(internalErrorState);
			results.add(internalErrorState);
			return results;
		}

		// The own basic RCR score
		final Double ownMedian = RCRs.get(clazz);
		if (ownMedian == null) {
			results.add(internalErrorState);
		} else {
			results.add(ownMedian.doubleValue());
		}

		// Map used to store the upper Call Relations
		Map<Integer, Record> distanceData = new ConcurrentHashMap<Integer, Record>();

		// List of all checked sources classes
		ArrayList<Integer> finishedCallerClasses = new ArrayList<>();

		ArrayList<Integer> sourcesList = sources.get(clazz);
		if (sourcesList != null) {
			for (Integer source : sourcesList) {
				getInputClasses(source, clazz, 1, 0, distanceData, finishedCallerClasses, ownSign);
			}
		}
		results.add(getMedianInputScore(distanceData));

		// Default value, kept if no target classes are found
		double outputScore = internalErrorState;

		// List of all checked target classes
		ArrayList<Integer> finishedCalleeClasses = new ArrayList<>();

		// Run trough all Callees of the observed classes and get the maximum
		// rating
		ArrayList<Integer> targetList = targets.get(clazz);
		if (targetList != null) {
			for (Integer target : targetList) {
				outputScore = getMaxOutputRating(target, outputScore, finishedCalleeClasses,
						ownSign);
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
	private void getInputClasses(int source, int target, int distance, int weight,
			Map<Integer, Record> distanceData, ArrayList<Integer> finishedCallerClasses,
			boolean sign) {
		if (!finishedCallerClasses.contains(source)) {
			finishedCallerClasses.add(source);
			Integer addWeight = weights.get(source + ";" + target);
			if (addWeight == null) {
				addWeight = 0;
			}

			weight = weight + addWeight;
			Double RCR = RCRs.get(source);
			if (RCR == null) {
				RCR = internalErrorState;
			}

			Boolean ownSign = isPositive.get(source);
			if (ownSign != null) {
				if (ownSign.booleanValue() == sign) {
					addInputClasses(source, weight, RCR, distance, distanceData);
				}
			}
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
			return internalErrorState;
		}

		for (int i = 0; i < scores.size(); i++) {
			if ((scores.get(i) != internalErrorState) && (scores.size() == weights.size())
					&& (scores.size() == distances.size())) {
				powerScores.add(scores.get(i));
				powerWeights.add(weights.get(i) / Math.pow(distances.get(i), z));
			}
		}

		if ((powerScores.size() == 0) || (powerScores.size() != powerWeights.size())) {
			return internalErrorState;
		} else {
			Double result = Maths.weightedPowerMean(powerScores, powerWeights, 1);
			if (result == null) {
				return internalErrorState;
			}
			return result;
		}
	}

	/**
	 * Calculates the maxixum called Root Cause Rating as described in Marwede
	 * et al. It compares the current value to all connected Callees and returns
	 * the highest value.
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
			ArrayList<Integer> finishedCalleeCallees, boolean sign) {
		if (finishedCalleeCallees.contains(target)) {
			return max;
		} else {
			finishedCalleeCallees.add(target);
			Double newValue = RCRs.get(target);
			Boolean ownSign = isPositive.get(target);
			if ((newValue == null) || (ownSign == null) || (newValue.doubleValue() == errorState)
					|| (ownSign.booleanValue() != sign)) {
				return max;
			}
			max = Math.max(max, newValue);
			ArrayList<Integer> targetList = targets.get(target);
			if (targetList != null) {
				for (Integer key : targetList) {
					max = Math.max(max, getMaxOutputRating(key, max, finishedCalleeCallees, sign));
				}
			}
			return max;
		}
	}

	/**
	 * Determines if the class has a positive or negative root cause rating
	 *
	 * @param target
	 *            class that needs to be looked after
	 * @return positive ranking of the class
	 */
	private boolean isRCRPositive(int target) {
		Boolean positive = isPositive.get(target);
		if (positive != null) {
			return positive.booleanValue();
		} else {
			return true;
		}
	}

}
