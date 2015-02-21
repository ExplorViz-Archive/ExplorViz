package explorviz.plugin_server.rootcausedetection.algorithm;

import static org.junit.Assert.*;

import java.util.*;
import java.util.Map.Entry;

import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.rootcausedetection.model.AnomalyScoreRecord;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.*;

public class MaximumAlgorithmTest {

	/**
	 * This method tests if the MaximumAlgorithm correctly aggregates ratings to
	 * application level.
	 */
	@Test
	public void aggregateTest() {
		Landscape landscape = TestLandscapeBuilder.createStandardLandscape(0);
		RanCorrLandscape rcLandscape = new RanCorrLandscape(landscape);

		double current = 0.0d;
		Application maxRating = null;
		for (Clazz clazz : rcLandscape.getClasses()) {
			clazz.setRootCauseRating(current);
			current += 0.01d;
			maxRating = clazz.getParent().getBelongingApplication();
		}

		MaximumAlgorithm alg = new MaximumAlgorithm();
		alg.aggregate(rcLandscape);

		double sum = 0.0d;
		Application best = null;
		for (Application application : rcLandscape.getApplications()) {
			sum += application.getRootCauseRating();
			if ((best == null) || (best.getRootCauseRating() < application.getRootCauseRating())) {
				best = application;
			}
		}

		// ratings should be normalized
		assertTrue("expected = 1, currently = " + sum, withEpsilon(sum, 1.0d, 0.00001d));
		// ratings should be correctly raised to application level
		assertTrue(best == maxRating);
	}

	/**
	 * This method tests if CommunicationClazz can correctly handle anomaly
	 * scores.
	 */
	@Test
	public void anomalyScoresInCommunicationClazzTest() {
		CommunicationClazz comClazz = new CommunicationClazz();
		TreeMapLongDoubleIValue scores = new TreeMapLongDoubleIValue();
		scores.put(0l, 0.1d);
		scores.put(1l, -0.2d);
		scores.put(2l, 0.05d);
		comClazz.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, scores);

		// Test of getLatestAnomalyScorePair()
		Entry<Long, Double> entry = getLatestAnomalyScorePair(comClazz);
		assertTrue(entry.getKey() == 2l);
		assertTrue(entry.getValue() == 0.05d);

		// Test of getAnomalyScores()
		List<AnomalyScoreRecord> ass = getAnomalyScores(comClazz);
		assertTrue(ass.size() == 3);
		assertTrue(ass.get(0).getTimestamp() == 0l);
		assertTrue(ass.get(0).getAnomaly_score() == -0.8d);
		assertTrue(ass.get(1).getTimestamp() == 1l);
		assertTrue(ass.get(1).getAnomaly_score() == -0.6d);
		assertTrue(ass.get(2).getTimestamp() == 2l);
		assertTrue(ass.get(2).getAnomaly_score() == -0.9d);
	}

	/**
	 * This method tests if Clazz can correctly decide if a rating should be
	 * positive or negative.
	 */
	@Test
	public void isRankingPositiveTest() {
		CommunicationClazz comClazz = new CommunicationClazz();
		TreeMapLongDoubleIValue scores = new TreeMapLongDoubleIValue();
		scores.put(0l, 0.1d);
		scores.put(1l, 0.2d);
		scores.put(2l, 0.05d);
		comClazz.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, scores);

		CommunicationClazz comClazz2 = new CommunicationClazz();
		TreeMapLongDoubleIValue scores2 = new TreeMapLongDoubleIValue();
		scores2.put(0l, 0.1d);
		scores2.put(1l, 0.2d);
		scores2.put(2l, -0.5d);
		comClazz2.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, scores2);

		RanCorrLandscape lscp = new RanCorrLandscape();
		lscp.addOperation(comClazz);
		lscp.addOperation(comClazz2);

		Clazz clazz = new Clazz();
		comClazz.setSource(clazz);
		comClazz.setTarget(clazz);
		comClazz2.setSource(clazz);
		comClazz2.setTarget(clazz);

		assertTrue(!isRankingPositive(lscp, clazz));
	}

	/**
	 * This method also tests if Clazz can correctly decide if a rating should
	 * be positive or negative.
	 */
	@Test
	public void isRankingPositiveTest2() {
		CommunicationClazz comClazz = new CommunicationClazz();
		TreeMapLongDoubleIValue scores = new TreeMapLongDoubleIValue();
		scores.put(0l, 0.1d);
		scores.put(1l, 0.2d);
		scores.put(3l, 0.05d);
		comClazz.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, scores);

		CommunicationClazz comClazz2 = new CommunicationClazz();
		TreeMapLongDoubleIValue scores2 = new TreeMapLongDoubleIValue();
		scores2.put(0l, 0.1d);
		scores2.put(1l, 0.2d);
		scores2.put(2l, -0.5d);
		comClazz2.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, scores2);

		RanCorrLandscape lscp = new RanCorrLandscape();
		lscp.addOperation(comClazz);
		lscp.addOperation(comClazz2);

		Clazz clazz = new Clazz();
		comClazz.setSource(clazz);
		comClazz.setTarget(clazz);
		comClazz2.setSource(clazz);
		comClazz2.setTarget(clazz);

		assertTrue(isRankingPositive(lscp, clazz));
	}

	private boolean withEpsilon(double is, double should, double epsilon) {
		return (Math.abs(is - should) - epsilon) <= 0.0d;
	}

	/**
	 * This method returns if the Root Cause Rating of this class is positive.
	 * This information is directly derived from Anomaly Scores. This is done as
	 * follows: We get from every method in this class the latest anomaly score.
	 * From these we choose the AS which has the most recent timestamp and of
	 * these the highest absolute value. Then we check if this AS is >= 0.
	 *
	 * @param lscp
	 *            Landscape we want to look for operations in
	 * @param clazz
	 *            given clazz
	 * @return Is the Root Cause Ranking of this class positive?
	 */
	private boolean isRankingPositive(RanCorrLandscape lscp, Clazz clazz) {
		long latest = 0;
		double valueOfLatest = 0;

		for (CommunicationClazz operation : lscp.getOperations()) {
			if (operation.getTarget() == clazz) {
				Entry<Long, Double> entry = getLatestAnomalyScorePair(operation);
				if (entry != null) {
					if (entry.getKey() > latest) {

						// more recent value has been found
						latest = entry.getKey();
						valueOfLatest = entry.getValue();
					} else if ((entry.getKey() == latest)
							&& (Math.abs(entry.getValue()) > Math.abs(valueOfLatest))) {

						// higher absolute value has been found
						// new value has the same timestamp as the one from
						// before
						valueOfLatest = entry.getValue();
					}
				}
			}
		}

		return valueOfLatest >= 0;
	}

	/**
	 * This method returns the latest timestamp-anomalyscore-pair for a given
	 * method.
	 *
	 * @param op
	 *            given operation
	 * @return Pair of (Timestamp, Anomaly Score), null if no scores are present
	 */
	private Entry<Long, Double> getLatestAnomalyScorePair(CommunicationClazz op) {
		TreeMapLongDoubleIValue anomalyScores = (TreeMapLongDoubleIValue) op
				.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
		if (anomalyScores == null) {
			return null;
		}
		final List<Entry<Long, Double>> mapEntries = new ArrayList<Entry<Long, Double>>(
				anomalyScores.entrySet());

		Entry<Long, Double> current = null;
		for (Entry<Long, Double> entry : mapEntries) {
			if ((current == null) || (entry.getKey() > current.getKey())) {
				current = entry;
			}
		}

		return current;
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
	private List<AnomalyScoreRecord> getAnomalyScores(CommunicationClazz op) {
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
