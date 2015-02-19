package explorviz.plugin_server.anomalydetection.forecast;

import java.util.Collections;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;

/**
 * This algorithm returns the last history response Time
 *
 * @author Kim Christian Mannstedt
 * @author Enno Schwanke
 *
 */
public class NaiveForecaster extends AbstractForecaster {

	/**
	 *
	 * @param historyResponseTimes
	 *            window with the history response times
	 * @return last history response time
	 */
	public static double forecast(final TreeMapLongDoubleIValue historyResponseTimes) {
		final long key = Collections.max(historyResponseTimes.keySet());
		return historyResponseTimes.get(key);
	}
}
