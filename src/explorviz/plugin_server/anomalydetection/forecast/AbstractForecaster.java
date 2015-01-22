package explorviz.plugin_server.anomalydetection.forecast;

import java.util.logging.Level;
import java.util.logging.Logger;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;

public abstract class AbstractForecaster {
	private static final Logger logger = Logger.getLogger(AbstractForecaster.class.getName());

	@SuppressWarnings("static-access")
	public static double forecast(final TreeMapLongDoubleIValue historyResponseTimes,
			final TreeMapLongDoubleIValue historyForecastResponseTimes) {
		try {
			switch (Configuration.FORECASTING_ALGORITHM) {
				case "explorviz.plugin_server.anomalydetection.forecast.NaiveForecaster":
					final NaiveForecaster naiveForecaster = new NaiveForecaster();
					return naiveForecaster.forecast(historyResponseTimes);
				case "explorviz.plugin_server.anomalydetection.forecast.ARIMAForecaster":
					final ARIMAForecaster arimaForecaster = new ARIMAForecaster();
					return arimaForecaster.forecast(historyResponseTimes,
							historyForecastResponseTimes);
				case "explorviz.plugin_server.anomalydetection.forecast.MovingAverageForecaster":
					final MovingAverageForecaster movingAverageForecaster = new MovingAverageForecaster();
					return movingAverageForecaster.forecast(historyResponseTimes);
				default:
					throw new Exception("Forecaster not available. Check configuration!");
			}
			// TODO: AnomalyScoreException einführen.
		} catch (final Exception e) {
			logger.log(Level.SEVERE, "Forecaster not available. Check configuration!", e);
		}
		// TODO: wird das erreicht?
		return 0;
	}
}
