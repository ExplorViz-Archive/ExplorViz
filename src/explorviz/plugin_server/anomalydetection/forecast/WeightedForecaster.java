package explorviz.plugin_server.anomalydetection.forecast;

import java.util.ArrayList;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;

public class WeightedForecaster extends AbstractForecaster {

	public static double forecast(TreeMapLongDoubleIValue historyResponseTimes) {
		int size = historyResponseTimes.size();
		ArrayList<Double> historyResponseTimesValues = new ArrayList<>(
				historyResponseTimes.values());
		double forecastResponseTime = 0;
		double weight = 1;
		double weightValues = 0;
		// low weighting
		if (Configuration.WEIGHTING_FORECASTER_WEIGHT.equals("LOW")) {
			for (int i = 0; i < size; i++) {
				weight = 1 + (i * 0.1);
				forecastResponseTime += historyResponseTimesValues.get(i) * weight;
				weightValues += weight;
			}
			return forecastResponseTime / weightValues;
		}
		// mean weighting
		else if (Configuration.WEIGHTING_FORECASTER_WEIGHT.equals("MEAN")) {
			for (int i = 0; i < size; i++) {
				weight = 1 + i;
				forecastResponseTime += historyResponseTimesValues.get(i) * weight;
				weightValues += weight;
			}
			return forecastResponseTime / weightValues;
		}
		// strong weighting
		else if (Configuration.WEIGHTING_FORECASTER_WEIGHT.equals("STRONG")) {
			for (int i = 0; i < size; i++) {
				weight = Math.pow(2, i);
				forecastResponseTime += historyResponseTimesValues.get(i) * weight;
				weightValues += weight;
			}
			return forecastResponseTime / weightValues;
		}
		// false weighting
		else {
			throw new FalseWeightInConfigurationException(
					"False weight for WeightedForecaster. Check Configuration!");
		}
	}
}
