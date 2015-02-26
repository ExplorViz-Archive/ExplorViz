package explorviz.plugin_server.anomalydetection.forecast;

import java.util.Collections;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;

/**
 * This class choose the forecast algorithm if there enough history response
 * times. If there are zero history response times, use the actual response
 * time. If there is one history response time, use the naive forecast
 * algorithm. If there are not enough history response times use the initial
 * forecast algorithm
 *
 * @author Kim Christian Mannstedt
 * @author Enno Schwanke
 *
 */
public abstract class AbstractForecaster {

	/**
	 * This method choose the forecast response time
	 *
	 * @param historyResponseTimes
	 *            history response times
	 * @param historyForecastResponseTimes
	 *            history forecast response times
	 * @return forecast response time
	 */
	public static double forecast(final TreeMapLongDoubleIValue historyResponseTimes,
			final TreeMapLongDoubleIValue historyForecastResponseTimes) {
		long currentResponseTimeKey = Collections.max(historyResponseTimes.keySet());
		double currentResponseTime = historyResponseTimes.get(currentResponseTimeKey);
		historyResponseTimes.remove(currentResponseTimeKey);
		if (historyResponseTimes.size() < Configuration.TIME_SERIES_WINDOW_SIZE) {
			return doInitializationForecast(currentResponseTime, historyResponseTimes,
					historyForecastResponseTimes);
		} else {
			return doStandardForecast(historyResponseTimes, historyForecastResponseTimes);
		}
	}

	private static double doInitializationForecast(double currentResponseTime,
			TreeMapLongDoubleIValue historyResponseTimes,
			TreeMapLongDoubleIValue historyForecastResponseTimes) {
		if (historyResponseTimes.size() == 0) {
			return currentResponseTime;
		} else if (historyResponseTimes.size() == 1) {
			return NaiveForecaster.forecast(historyResponseTimes);
		} else {
			switch (Configuration.INIT_FORECASTING_ALGORITHM) {
				case "explorviz.plugin_server.anomalydetection.forecast.NaiveForecaster":
					// final NaiveForecaster naiveForecaster = new
					// NaiveForecaster();
					return NaiveForecaster.forecast(historyResponseTimes);
				case "explorviz.plugin_server.anomalydetection.forecast.MovingAverageForecaster":
					// final MovingAverageForecaster movingAverageForecaster =
					// new MovingAverageForecaster();
					return MovingAverageForecaster.forecast(historyResponseTimes);
				case "explorviz.plugin_server.anomalydetection.forecast.WeightedForecaster":
					return WeightedForecaster.forecast(historyResponseTimes);
				default:
					throw new ForecasterNotFoundException(
							"Forecaster not available as initialization-algorithm. Check configuration!");
			}
		}
	}

	private static double doStandardForecast(TreeMapLongDoubleIValue historyResponseTimes,
			TreeMapLongDoubleIValue historyForecastResponseTimes) {
		TreeMapLongDoubleIValue delimitedHistoryResponseTimes = delimitTreeMap(historyResponseTimes);
		TreeMapLongDoubleIValue delimitedHistoryForecastResponseTimes = delimitTreeMap(historyForecastResponseTimes);
		switch (Configuration.FORECASTING_ALGORITHM) {
			case "explorviz.plugin_server.anomalydetection.forecast.NaiveForecaster":
				// final NaiveForecaster naiveForecaster = new
				// NaiveForecaster();
				return NaiveForecaster.forecast(delimitedHistoryResponseTimes);
			case "explorviz.plugin_server.anomalydetection.forecast.MovingAverageForecaster":
				// final MovingAverageForecaster movingAverageForecaster = new
				// MovingAverageForecaster();
				return MovingAverageForecaster.forecast(delimitedHistoryResponseTimes);
			case "explorviz.plugin_server.anomalydetection.forecast.WeightedForecaster":
				return WeightedForecaster.forecast(delimitedHistoryResponseTimes);
			default:
				throw new ForecasterNotFoundException(
						"Forecaster not available. Check configuration!");
		}
	}

	private static TreeMapLongDoubleIValue delimitTreeMap(TreeMapLongDoubleIValue map) {
		TreeMapLongDoubleIValue newMap = new TreeMapLongDoubleIValue();
		for (int i = 0; i < Configuration.TIME_SERIES_WINDOW_SIZE; i++) {
			long key = Collections.max(map.keySet());
			newMap.put(key, map.get(key));
			map.remove(key);
		}
		return newMap;
	}
}
