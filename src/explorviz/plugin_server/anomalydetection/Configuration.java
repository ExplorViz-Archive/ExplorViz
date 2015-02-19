package explorviz.plugin_server.anomalydetection;

public class Configuration {
	public static String INIT_FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.MovingAverageForecaster";
	public static double WARNING_ANOMALY = 0.5;
	public static double ERROR_ANOMALY = 0.7;
	public static int TIME_SERIES_WINDOW_SIZE = 15;
	public static String FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.MovingAverageForecaster";
	public static String WEIGHTING_FORECASTER_WEIGHT = "LOW"; // LOW, MEAN,
	// STRONG
}
