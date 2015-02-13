package explorviz.plugin_server.anomalydetection.anomalyscore;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

public class NormalizeAnomalyScoreTest {

	static NormalizeAnomalyScore normalizeAnomalyScore;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		normalizeAnomalyScore = new NormalizeAnomalyScore();
	}

	@Test
	public void testNormalizeAnomalyScore() {
		double result1 = normalizeAnomalyScore.normalizeAnomalyScore(100, 300, 200);
		assertEquals(0.2, result1, 0.01);
		double result2 = normalizeAnomalyScore.normalizeAnomalyScore(20, 70, 50);
		assertEquals(0.166667, result2, 0.01);
		double result3 = normalizeAnomalyScore.normalizeAnomalyScore(-50000, 20000, 70000);
		assertEquals(-0.555555, result3, 0.01);
		double result4 = normalizeAnomalyScore.normalizeAnomalyScore(0, 20, 20);
		assertEquals(0, result4, 0.01);
	}

	@Test
	public void throwsCorruptedParametersException() {
		thrown.expect(CorruptedParametersException.class);
		thrown.expectMessage("The calculated anomaly score (100.0) does not fit to the given response time (10.0) and forecasted response time (50.0).");
		normalizeAnomalyScore.normalizeAnomalyScore(100, 10, 50);
	}

}
