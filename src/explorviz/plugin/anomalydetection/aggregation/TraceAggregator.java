package explorviz.plugin.anomalydetection.aggregation;

import java.util.HashMap;

import explorviz.shared.model.RuntimeInformation;

public class TraceAggregator {
	// TODO: JUNIT Tests hinzufügen.
	public float aggregateTraces(final HashMap<Long, RuntimeInformation> traceIdToRuntimeMap) {
		float methodResponsetime = 0;
		for (final RuntimeInformation runtimeInformation : traceIdToRuntimeMap.values()) {
			methodResponsetime += runtimeInformation.getAverageResponseTimeInNanoSec() * 1000 * 1000;
		}
		methodResponsetime = methodResponsetime / traceIdToRuntimeMap.size();
		return methodResponsetime;
	}
}
