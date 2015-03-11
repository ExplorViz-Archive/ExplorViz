package explorviz.plugin_server.anomalydetection;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.shared.model.*;

public class OPADxTest {

	private static Landscape landscape;
	private static CommunicationClazz commClazz;

	@BeforeClass
	public static void beforeClass() {
		landscape = TestLandscapeBuilder.createStandardLandscape(30);
		commClazz = landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0)
				.getApplications().get(0).getCommunications().get(0);
		// HashMap<Long, RuntimeInformation> traceIdToRuntimeMap = new
		// HashMap<Long, RuntimeInformation>();
		// for (int i = 1; i <= 5; i++) {
		// RuntimeInformation runtime = new RuntimeInformation();
		// runtime.setAverageResponseTimeInNanoSec(7);
		// traceIdToRuntimeMap.put(new Long(i), runtime);
		// }
		// TreeMapLongDoubleIValue responseTimes = new
		// TreeMapLongDoubleIValue();
		// for (int i = 1; i <= 29; i++) {
		// responseTimes.put(new Long(i), 1.0);
		// }
		// TreeMapLongDoubleIValue predictedResponseTimes = new
		// TreeMapLongDoubleIValue();
		// for (int i = 1; i <= 29; i++) {
		// predictedResponseTimes.put(new Long(i), 1.0);
		// }
		// TreeMapLongDoubleIValue anomalyScore = new TreeMapLongDoubleIValue();
		// for (int i = 1; i <= 29; i++) {
		// anomalyScore.put(new Long(i), 0.0);
		// }
		// commClazz.setTraceIdToRuntimeMap(traceIdToRuntimeMap);
		// commClazz.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME,
		// responseTimes);
		// commClazz.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
		// predictedResponseTimes);
		// commClazz.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE,
		// anomalyScore);
	}

	@Test
	public void testDoAnomalyDetection() {
		OPADx opadx = new OPADx();
		opadx.doAnomalyDetection(landscape);
		boolean warning = commClazz.getGenericBooleanData(IPluginKeys.WARNING_ANOMALY);
		assertFalse(warning);
		boolean parentWarning = commClazz.getTarget().getGenericBooleanData(
				IPluginKeys.WARNING_ANOMALY);
		CommunicationClazz anotherCommClazz = landscape.getSystems().get(0).getNodeGroups().get(0)
				.getNodes().get(0).getApplications().get(0).getCommunications().get(1);
		boolean anomalyOfAnotherClazz = anotherCommClazz
				.getGenericBooleanData(IPluginKeys.WARNING_ANOMALY);
		assertFalse(anomalyOfAnotherClazz);

		boolean anomaly = commClazz.getGenericBooleanData(IPluginKeys.ERROR_ANOMALY);
		boolean parentAnomaly = commClazz.getTarget().getGenericBooleanData(
				IPluginKeys.ERROR_ANOMALY);
		assertTrue(anomaly);
		assertTrue(parentAnomaly);
		assertFalse(parentWarning);

		assertEquals(0.75,
				((TreeMapLongDoubleIValue) commClazz
						.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE)).get(30L), 0);
	}

}
