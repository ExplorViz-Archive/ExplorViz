package explorviz.plugin.anomalydetection;

public class Configuration {
	double WARNING_ANOMALY = 0.5;
	double ERROR_ANOMALY = 0.8;
	int TIME_SERIES_WINDOW_SIZE = 15;
	String FORECASTING_ALGORITHM = "explorviz.plugin.anomalydetection.forecast.NaiveForecaster";
}
