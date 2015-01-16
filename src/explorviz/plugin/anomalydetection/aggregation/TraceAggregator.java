package explorviz.plugin.anomalydetection.aggregation;

import java.util.HashMap;

import explorviz.shared.model.RuntimeInformation;

public class TraceAggregator {
	// TODO: JUNIT Tests hinzufügen.
	public double aggregateTraces(final HashMap<Long, RuntimeInformation> traceIdToRuntimeMap) {
		double methodResponsetime = 0;
		for (final RuntimeInformation runtimeInformation : traceIdToRuntimeMap.values()) {
			methodResponsetime += runtimeInformation.getAverageResponseTimeInNanoSec();
		}
		methodResponsetime = methodResponsetime / traceIdToRuntimeMap.size();
		return methodResponsetime;
	}
}
