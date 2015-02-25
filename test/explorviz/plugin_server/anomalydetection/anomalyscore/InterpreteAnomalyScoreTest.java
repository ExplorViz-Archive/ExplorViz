package explorviz.plugin_server.anomalydetection.anomalyscore;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import explorviz.plugin_server.anomalydetection.Configuration;

public class InterpreteAnomalyScoreTest {
	static InterpreteAnomalyScore interpreteAnomalyScore;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		interpreteAnomalyScore = new InterpreteAnomalyScore();
	}

	@Test
	public void testInterprete() {
		boolean[] result1 = interpreteAnomalyScore.interprete(0.5);
		if (Configuration.WARNING_ANOMALY > 0.5) {
			assertFalse(result1[0]);
		} else {
			assertTrue(result1[0]);
		}
		if (Configuration.ERROR_ANOMALY > 0.5) {
			assertFalse(result1[1]);
		} else {
			assertTrue(result1[1]);
		}

		boolean[] result2 = interpreteAnomalyScore.interprete(-0.5);
		if (Configuration.WARNING_ANOMALY > 0.5) {
			assertFalse(result2[0]);
		} else {
			assertTrue(result2[0]);
		}
		if (Configuration.ERROR_ANOMALY > 0.5) {
			assertFalse(result2[1]);
		} else {
			assertTrue(result2[1]);
		}

		boolean[] result3 = interpreteAnomalyScore.interprete(1);
		if (Configuration.WARNING_ANOMALY > 1) {
			assertFalse(result3[0]);
		} else {
			assertTrue(result3[0]);
		}
		if (Configuration.ERROR_ANOMALY > 1) {
			assertFalse(result3[1]);
		} else {
			assertTrue(result3[1]);
		}

		boolean[] result4 = interpreteAnomalyScore.interprete(-1);
		if (Configuration.WARNING_ANOMALY > 1) {
			assertFalse(result4[0]);
		} else {
			assertTrue(result4[0]);
		}
		if (Configuration.ERROR_ANOMALY > 1) {
			assertFalse(result4[1]);
		} else {
			assertTrue(result4[1]);
		}

		boolean[] result5 = interpreteAnomalyScore.interprete(0);
		if (Configuration.WARNING_ANOMALY > 0) {
			assertFalse(result5[0]);
		} else {
			assertTrue(result5[0]);
		}
		if (Configuration.ERROR_ANOMALY > 0) {
			assertFalse(result5[1]);
		} else {
			assertTrue(result5[1]);
		}

		boolean[] result6 = interpreteAnomalyScore.interprete(0.2);
		if (Configuration.WARNING_ANOMALY > 0.2) {
			assertFalse(result6[0]);
		} else {
			assertTrue(result6[0]);
		}
		if (Configuration.ERROR_ANOMALY > 0.2) {
			assertFalse(result6[1]);
		} else {
			assertTrue(result6[1]);
		}

		boolean[] result7 = interpreteAnomalyScore.interprete(-0.2);
		if (Configuration.WARNING_ANOMALY > 0.2) {
			assertFalse(result7[0]);
		} else {
			assertTrue(result7[0]);
		}
		if (Configuration.ERROR_ANOMALY > 0.2) {
			assertFalse(result7[1]);
		} else {
			assertTrue(result7[1]);
		}

	}
	
	@Test
	public void throwsCorruptedParametersException() {
		thrown.expect(CorruptedParametersException.class);
		thrown.expectMessage("Anomaly Score must be a value between -1 and 1.");
		interpreteAnomalyScore.interprete(1.2);
	}

}
