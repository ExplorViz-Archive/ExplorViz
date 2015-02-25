package explorviz.plugin_server.anomalydetection.aggregation;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import explorviz.shared.model.RuntimeInformation;

public class TraceAggregatorTest {

	static HashMap<Long, RuntimeInformation> traceIdToRuntimeMap1;
	static HashMap<Long, RuntimeInformation> traceIdToRuntimeMap2;
	static HashMap<Long, RuntimeInformation> traceIdToRuntimeMap3;
	static HashMap<Long, RuntimeInformation> traceIdToRuntimeMap4;
	static TraceAggregator traceAggregator;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		traceAggregator = new TraceAggregator();

		traceIdToRuntimeMap1 = new HashMap<Long, RuntimeInformation>();
		traceIdToRuntimeMap2 = new HashMap<Long, RuntimeInformation>();
		traceIdToRuntimeMap3 = new HashMap<Long, RuntimeInformation>();
		traceIdToRuntimeMap4 = new HashMap<Long, RuntimeInformation>();

		RuntimeInformation runtimeInformation1 = new RuntimeInformation();
		runtimeInformation1.setAverageResponseTimeInNanoSec(1000);

		RuntimeInformation runtimeInformation2 = new RuntimeInformation();
		runtimeInformation2.setAverageResponseTimeInNanoSec(2000);

		RuntimeInformation runtimeInformation3 = new RuntimeInformation();
		runtimeInformation3.setAverageResponseTimeInNanoSec(1000);

		RuntimeInformation runtimeInformation4 = new RuntimeInformation();
		runtimeInformation4.setAverageResponseTimeInNanoSec(4000);

		traceIdToRuntimeMap1.put(1L, runtimeInformation1);
		traceIdToRuntimeMap1.put(2L, runtimeInformation2);
		traceIdToRuntimeMap1.put(3L, runtimeInformation3);
		traceIdToRuntimeMap1.put(4L, runtimeInformation4);

		traceIdToRuntimeMap2.put(10L, runtimeInformation2);
		traceIdToRuntimeMap2.put(11L, runtimeInformation3);
		traceIdToRuntimeMap2.put(15L, runtimeInformation4);

		traceIdToRuntimeMap3.put(100L, runtimeInformation4);
		traceIdToRuntimeMap3.put(200L, runtimeInformation1);
		traceIdToRuntimeMap3.put(300L, runtimeInformation3);

		traceIdToRuntimeMap4.put(0L, runtimeInformation4);
		traceIdToRuntimeMap4.put(10L, runtimeInformation1);
	}

	@Test
	public void testAggregateTraces() {
		assertEquals(2000, traceAggregator.aggregateTraces(traceIdToRuntimeMap1), 1);
		assertEquals(2333.333, traceAggregator.aggregateTraces(traceIdToRuntimeMap2), 1);
		assertEquals(2000, traceAggregator.aggregateTraces(traceIdToRuntimeMap3), 1);
		assertEquals(2500, traceAggregator.aggregateTraces(traceIdToRuntimeMap4), 1);
	}

}
