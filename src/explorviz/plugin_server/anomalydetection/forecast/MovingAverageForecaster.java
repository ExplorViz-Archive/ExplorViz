package explorviz.plugin_server.anomalydetection.forecast;

import java.util.ArrayList;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;

public class MovingAverageForecaster extends AbstractForecaster {

	public static double forecast(final TreeMapLongDoubleIValue historyResponseTimes) {

		Double forecastResponseTime = new Double(0);

		final ArrayList<Double> historyResponseTimesValues = new ArrayList<Double>(
				historyResponseTimes.values());
		for (int i = 0; i < historyResponseTimesValues.size(); i++) {
			forecastResponseTime += historyResponseTimesValues.get(i);
		}

		return forecastResponseTime / historyResponseTimesValues.size();
	}
}
