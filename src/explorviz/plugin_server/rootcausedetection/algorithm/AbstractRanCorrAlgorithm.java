package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.exception.RootCauseThreadingException;
import explorviz.plugin_server.rootcausedetection.model.AnomalyScoreRecord;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.plugin_server.rootcausedetection.util.IThreadable;
import explorviz.plugin_server.rootcausedetection.util.RCDThreadPool;
import explorviz.shared.model.Clazz;

/**
 * This abstract class represents algorithms concerning the calculation of
 * RootCauseRatings in a RanCorrLandscape. It will automatically introduce
 * concurrency for its implemented algorithms.
 *
 * @author Christian Claus Wiechmann, Dominik Olp, Yannic Noller
 *
 */
public abstract class AbstractRanCorrAlgorithm implements IThreadable<Clazz, RanCorrLandscape> {

	/**
	 * Calculate RootCauseRatings in a RanCorrLandscape and uses Anomaly Scores
	 * in the ExplorViz landscape.
	 *
	 * @param lscp
	 *            specified landscape
	 */
	public void calculate(final RanCorrLandscape lscp) {
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
	public abstract void calculate(Clazz clazz, RanCorrLandscape lscp);

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

}