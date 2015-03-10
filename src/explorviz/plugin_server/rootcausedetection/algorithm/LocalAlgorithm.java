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
 * This class contains a very simple algorithm to calculate RootCauseRatings. It
 * only uses data of the element the RootCauseRating is calculated for.
 *
 * @author Christian Claus Wiechmann, Dominik Olp, Yannic Noller, Jens Michaelis
 *
 */
public class LocalAlgorithm extends AbstractRanCorrAlgorithm {

	private Map<Integer, ArrayList<Double>> anomalyScores = new ConcurrentHashMap<Integer, ArrayList<Double>>();

	public void calculate(final RanCorrLandscape lscp) {
		generateMaps(lscp);

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
					"LocalRancorrAlgorithm#calculate(...): Threading interrupted, broken output.");
		}
	}

	@Override
	public void calculate(final Clazz clazz) {

		List<Double> scores = anomalyScores.get(clazz.hashCode());

		// If there are no anomaly scores for a operation, set the
		// corresponding root cause rating to a failure state
		if (scores == null) {
			clazz.setRootCauseRating(RanCorrConfiguration.RootCauseRatingFailureState);
			return;
		}

		// In trivial algorithm anomalyRank = locally aggregated anomaly
		// scores using unweighted arithmetic mean.
		final double anomalyRank = Maths.unweightedArithmeticMean(scores);

		clazz.setRootCauseRating(mapToPropabilityRange(anomalyRank));
	}

	/**
	 * This method walks trough all operations and generates the maps required
	 * by the algorithm.
	 *
	 * @param lscp
	 */
	public void generateMaps(final RanCorrLandscape lscp) {
		if (lscp.getOperations() != null) {
			for (CommunicationClazz operation : lscp.getOperations()) {
				Integer target = operation.getTarget().hashCode();
				// This part writes the anomalyScores to the specified target
				ArrayList<Double> scores = anomalyScores.get(target);
				if (scores != null) {
					scores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
				} else {
					scores = new ArrayList<Double>();
					scores.addAll(getValuesFromAnomalyList(getAnomalyScores(operation)));
				}
				anomalyScores.put(target, scores);
			}
		}
	}
}
