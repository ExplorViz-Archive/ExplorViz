package explorviz.plugin_server.anomalydetection.forecast;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;

public class NaiveForecaster extends AbstractForecaster {

	// TODO: To be implemented
	public static double forecast(final TreeMapLongDoubleIValue historyResponseTime) {
		final long key = 15;
		return historyResponseTime.get(key);
	}
}
