package explorviz.plugin_server.anomalydetection;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.shared.model.*;

public class TestOPADx {

	private static Landscape landscape;
	private static CommunicationClazz commClazz;

	@BeforeClass
	public static void beforeClass() {
		landscape = TestLandscapeBuilder.createStandardLandscape(30);
		commClazz = landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0)
				.getApplications().get(0).getCommunications().get(0);
		HashMap<Long, RuntimeInformation> traceIdToRuntimeMap = new HashMap<Long, RuntimeInformation>();
		for (int i = 1; i <= 5; i++) {
			RuntimeInformation runtime = new RuntimeInformation();
			runtime.setAverageResponseTimeInNanoSec(2);
			traceIdToRuntimeMap.put(new Long(i), runtime);
		}
		TreeMapLongDoubleIValue responseTimes = new TreeMapLongDoubleIValue();
		for (int i = 1; i <= 29; i++) {
			responseTimes.put(new Long(i), 2.0);
		}
		TreeMapLongDoubleIValue predictedResponseTimes = new TreeMapLongDoubleIValue();
		for (int i = 1; i <= 29; i++) {
			predictedResponseTimes.put(new Long(i), 2.0);
		}
		TreeMapLongDoubleIValue anomalyScore = new TreeMapLongDoubleIValue();
		for (int i = 1; i <= 29; i++) {
			anomalyScore.put(new Long(i), 0.0);
		}
		commClazz.setTraceIdToRuntimeMap(traceIdToRuntimeMap);
		commClazz.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, responseTimes);
		commClazz.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
				predictedResponseTimes);
		commClazz.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, anomalyScore);
	}

	@Test
	public void testDoAnomalyDetection() {
		OPADx opadx = new OPADx();
		opadx.doAnomalyDetection(landscape);
		boolean anomaly = commClazz.getGenericBooleanData(IPluginKeys.WARNING_ANOMALY);
		assertFalse(anomaly);
		boolean parentAnomaly = commClazz.getTarget().getGenericBooleanData(
				IPluginKeys.WARNING_ANOMALY);
		assertFalse(parentAnomaly);
	}

}
