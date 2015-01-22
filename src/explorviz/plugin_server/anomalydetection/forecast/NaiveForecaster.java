package explorviz.plugin_server.anomalydetection.forecast;

import java.util.Collections;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;

public class NaiveForecaster extends AbstractForecaster {

	public static double forecast(final TreeMapLongDoubleIValue historyResponseTimes) {
		final long key = Collections.max(historyResponseTimes.keySet());
		return historyResponseTimes.get(key);
	}
}
