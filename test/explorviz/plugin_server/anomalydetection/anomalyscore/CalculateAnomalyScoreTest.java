package explorviz.plugin_server.anomalydetection.anomalyscore;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

public class CalculateAnomalyScoreTest {
	static CalculateAnomalyScore calculateAnomalyScore;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		calculateAnomalyScore = new CalculateAnomalyScore();
	}

	@Test
	public void testGetAnomalyScore() {
		assertEquals(-0.333333, calculateAnomalyScore.getAnomalyScore(10, 20), 0.01);
		assertEquals(0.25, calculateAnomalyScore.getAnomalyScore(50, 30), 0.01);
		assertEquals(0, calculateAnomalyScore.getAnomalyScore(20, 20), 0.01);
		assertEquals(0, calculateAnomalyScore.getAnomalyScore(0, 0), 0.01);
		assertEquals(0.2, calculateAnomalyScore.getAnomalyScore(30, 20), 0.01);
		assertEquals(0.666667, calculateAnomalyScore.getAnomalyScore(1000, 200), 0.01);
		assertEquals(-0.904761, calculateAnomalyScore.getAnomalyScore(10, 200), 0.01);
	}

	@Test
	public void throwsCorruptedParametersException() {
		thrown.expect(CorruptedParametersException.class);
		thrown.expectMessage("Response time or forecast response time is less than 0. That is not possible.");
		calculateAnomalyScore.getAnomalyScore(-10, -30);
	}

}
