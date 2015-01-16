package explorviz.plugin.anomalydetection;

public class Configuration {
	public static double WARNING_ANOMALY = 0.5;
	public static double ERROR_ANOMALY = 0.8;
	public static int TIME_SERIES_WINDOW_SIZE = 15;
	public static String FORECASTING_ALGORITHM = "explorviz.plugin.anomalydetection.forecast.NaiveForecaster";
}
