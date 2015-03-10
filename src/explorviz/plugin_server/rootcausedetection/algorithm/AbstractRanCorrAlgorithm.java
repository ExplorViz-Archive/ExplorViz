package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.*;
import java.util.Map.Entry;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.exception.RootCauseThreadingException;
import explorviz.plugin_server.rootcausedetection.model.AnomalyScoreRecord;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.plugin_server.rootcausedetection.util.IThreadable;
import explorviz.plugin_server.rootcausedetection.util.RCDThreadPool;
import explorviz.shared.model.Clazz;
import explorviz.shared.model.CommunicationClazz;

/**
 * This abstract class represents algorithms concerning the calculation of
 * RootCauseRatings in a RanCorrLandscape. It will automatically introduce
 * concurrency for its implemented algorithms.
 *
 * @author Christian Claus Wiechmann, Dominik Olp, Yannic Noller
 *
 */
public abstract class AbstractRanCorrAlgorithm implements IThreadable<Clazz> {

	/**
	 * Calculate RootCauseRatings in a RanCorrLandscape and uses Anomaly Scores
	 * in the ExplorViz landscape.
	 *
	 * @param lscp
	 *            specified landscape
	 */
	public void calculate(final RanCorrLandscape lscp) {
		final RCDThreadPool<Clazz> pool = new RCDThreadPool<>(this,
				RanCorrConfiguration.numberOfThreads);

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
	public abstract void calculate(Clazz clazz);

	/**
	 * Maps the anomaly ranking range (-1.0 to +1.0) to a probability range (0
	 * to +1.0).
	 *
	 * @param anomalyRanking
	 *            - Double value which should be in range between -1.0 and +1.0
	 * @return mapped double value, null if parameter was in wrong range
	 */
	protected Double mapToPropabilityRange(final Double anomalyRanking) {
		if ((anomalyRanking == null) || (anomalyRanking < -1.0) || (anomalyRanking > 1.0)) {
			return null;
		}
		return (anomalyRanking + 1.0) / 2.0;
	}

	/**
	 * Returns the anomaly scores of a list of {@link AnomalyScoreRecord}s.
	 *
	 * @param anomalyScores
	 *            List of {@link AnomalyScoreRecord}s
	 * @return List of anomaly scores
	 */
	protected List<Double> getValuesFromAnomalyList(final List<AnomalyScoreRecord> anomalyScores) {
		final List<Double> values = new ArrayList<>();

		for (final AnomalyScoreRecord anomalyScore : anomalyScores) {
			values.add(anomalyScore.getAnomaly_score());
		}

		return values;
	}

	/**
	 * Returns a list of all available timestamp-anomalyScore pairs for a given
	 * operation. All anomaly scores are in [-1, 1].
	 *
	 * @param op
	 *            given operation
	 * @return List of {@link AnomalyScoreRecord}s. If there are no anomaly
	 *         scores available, the method will return null.
	 */
	protected List<AnomalyScoreRecord> getAnomalyScores(CommunicationClazz op) {
		// return null if there are no anomaly scores
		if (!op.isGenericDataPresent(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE)) {
			return null;
		}

		// otherwise create list of timestamp-anomalyscore pairs
		// (AnomalyScoreRecord)
		final TreeMapLongDoubleIValue anomalyScores = (TreeMapLongDoubleIValue) op
				.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
		final List<Entry<Long, Double>> mapEntries = new ArrayList<Entry<Long, Double>>(
				anomalyScores.entrySet());
		final List<AnomalyScoreRecord> outputScores = new ArrayList<AnomalyScoreRecord>();

		for (Entry<Long, Double> entry : mapEntries) {
			// note that we use absolute values here
			outputScores.add(new AnomalyScoreRecord(entry.getKey(),
					(Math.abs(entry.getValue()) * 2) - 1));
		}

		return outputScores;
	}

}