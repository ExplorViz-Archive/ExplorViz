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

	// Defined as in Marwede et al
	double p = 0.2d;
	double z = 1.0d;
	// Internal error state
	double errorState = -2.0d;

	private DistanceGraph graph;
	private final List<Integer> finishedCalleeClasses = new ArrayList<>();
	private final List<Integer> finishedCallerClasses = new ArrayList<>();

	private Object synch;

	@Override
	public void calculate(final Clazz clazz, final RanCorrLandscape lscp) {
		synch = lscp;
		synchronized (synch) {
			graph = new DistanceGraph(clazz.hashCode());
		}
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
		List<Double> scores = null;
		List<Integer> weights = null;
		List<Integer> distances = null;
		synchronized (synch) {
			scores = graph.getRCRs();
			weights = graph.getWeights();
			distances = graph.getDistances();
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
			return Maths.weightedPowerMean(powerScores, powerWeights, 1);
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
				getInputClasses(operation.getSource(), lscp);
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
	 */
	private void getInputClasses(final Clazz clazz, final RanCorrLandscape lscp) {
		synchronized (synch) {
			int hash = clazz.hashCode();
			if (!finishedCallerClasses.contains(hash)) {
				finishedCallerClasses.add(hash);
				for (final CommunicationClazz operation : lscp.getOperations()) {
					if (operation.getTarget() == clazz) {
						addInputClasses(operation.getSource(), hash, lscp, operation.getRequests());
						getInputClasses(operation.getSource(), lscp);
					}
				}
			}
		}
	}

	/**
	 * Helper for {@Link getInputClasses}, adds the given Class to the
	 * database
	 *
	 * @param source
	 *            The Callee that needs to be added
	 * @param targetHash
	 *            The hash value of the current observed Class called by the
	 *            Callee
	 * @param lscp
	 *            The current observed landscape
	 * @param weight
	 *            The weight of the caller/callee relation
	 */
	private void addInputClasses(final Clazz source, final int targetHash,
			final RanCorrLandscape lscp, final int weight) {
		int hash = graph.addRecord(source.hashCode(), targetHash);
		if (hash != -1) {
			final List<AnomalyScoreRecord> outputs = getAnomalyScores(lscp, source);
			double rcr = errorState;
			if (outputs.size() != 0) {
				rcr = Maths.unweightedPowerMean(getValuesFromAnomalyList(outputs), p);
			}
			graph.addWeightRCR(hash, weight, rcr);
		}
	}

}