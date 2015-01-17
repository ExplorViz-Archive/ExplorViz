package explorviz.plugin_server.anomalydetection.forecast;

import java.util.logging.Level;
import java.util.logging.Logger;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;

public abstract class AbstractForecaster {
	private static final Logger logger = Logger.getLogger(AbstractForecaster.class.getName());

	@SuppressWarnings("static-access")
	public static double forecast(final TreeMapLongDoubleIValue historyResponseTime) {
		try {
			switch (Configuration.FORECASTING_ALGORITHM) {
				case "explorviz.plugin.anomalydetection.forecast.NaiveForecaster":
					final NaiveForecaster naiveForecaster = new NaiveForecaster();
					return naiveForecaster.forecast(historyResponseTime);
				case "explorviz.plugin.anomalydetection.forecast.ARIMAForecaster":
					final ARIMAForecaster arimaForecaster = new ARIMAForecaster();
					return arimaForecaster.forecast(historyResponseTime);
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
