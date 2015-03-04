package explorviz.plugin_server.anomalydetection.forecast;

import java.util.ArrayList;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;

/**
 * This algorithm is a weighted forecast algorithm.
 *
 * @author Kim Christian Mannstedt
 *
 */
public class WeightedForecaster extends AbstractForecaster {

	/**
	 *
	 * @param historyResponseTimes
	 *            window of the last history response times
	 * @return weighted forecast response time
	 */
	public static double forecast(TreeMapLongDoubleIValue historyResponseTimes) {
		int size = historyResponseTimes.size();
		ArrayList<Double> historyResponseTimesValues = new ArrayList<>(
				historyResponseTimes.values());
		double weightedResponseTime = 0;
		double weight = 1;
		double weightSum = 0;
		// low weighting
		if (Configuration.WEIGHTED_FORECASTER_WEIGHT.equals("LOW")) {
			for (int i = 0; i < size; i++) {
				weight = 1 + Math.log(i + 1);
				weightedResponseTime += historyResponseTimesValues.get(i) * weight;
				weightSum += weight;
			}
			return weightedResponseTime / weightSum;
		}
		// mean weighting
		else if (Configuration.WEIGHTED_FORECASTER_WEIGHT.equals("MEAN")) {
			for (int i = 0; i < size; i++) {
				weight = 1 + i;
				weightedResponseTime += historyResponseTimesValues.get(i) * weight;
				weightSum += weight;
			}
			return weightedResponseTime / weightSum;
		}
		// strong weighting
		else if (Configuration.WEIGHTED_FORECASTER_WEIGHT.equals("STRONG")) {
			for (int i = 0; i < size; i++) {
				weight = Math.pow(2, i);
				weightedResponseTime += historyResponseTimesValues.get(i) * weight;
				weightSum += weight;
			}
			return weightedResponseTime / weightSum;
		}
		// false weighting
		else {
			throw new FalseWeightInConfigurationException(
					"False weight for WeightedForecaster. Check Configuration!");
		}
	}
}
