package explorviz.plugin_server.anomalydetection.aggregation;

import java.util.HashMap;

import explorviz.shared.model.RuntimeInformation;

/**
 *
 * This class aggregates the average response times and calculate the average
 * method reponse time
 *
 * @author Kim Christian Mannstedt
 * @author Enno Schwanke
 *
 */
public class TraceAggregator {
	/**
	 *
	 * @param traceIdToRuntimeMap
	 *            map with the runtime informations and traceIds
	 * @return average response time for method
	 */
	public double aggregateTraces(final HashMap<Long, RuntimeInformation> traceIdToRuntimeMap) {
		double methodResponsetime = 0;
		for (final RuntimeInformation runtimeInformation : traceIdToRuntimeMap.values()) {
			methodResponsetime += runtimeInformation.getAverageResponseTimeInNanoSec();
		}
		methodResponsetime = methodResponsetime / traceIdToRuntimeMap.size();
		return methodResponsetime;
	}
}
