package explorviz.plugin_server.rootcausedetection.algorithm;

import static org.junit.Assert.*;

import java.util.List;
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
		AbstractAggregationAlgorithm alg = new MaximumAlgorithm();
		Entry<Long, Double> entry = alg.getLatestAnomalyScorePair(comClazz);
		assertTrue(entry.getKey() == 2l);
		assertTrue(entry.getValue() == 0.05d);

		// Test of getAnomalyScores()
		AbstractRanCorrAlgorithm alg2 = new LocalAlgorithm();
		List<AnomalyScoreRecord> ass = alg2.getAnomalyScores(comClazz);
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

		AbstractAggregationAlgorithm alg = new MaximumAlgorithm();
		assertTrue(!alg.isRankingPositive(lscp, clazz));
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

		AbstractAggregationAlgorithm alg = new MaximumAlgorithm();
		assertTrue(alg.isRankingPositive(lscp, clazz));
	}

	private boolean withEpsilon(double is, double should, double epsilon) {
		return (Math.abs(is - should) - epsilon) <= 0.0d;
	}
}
