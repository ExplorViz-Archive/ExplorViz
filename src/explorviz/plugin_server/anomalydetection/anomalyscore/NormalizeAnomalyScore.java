package explorviz.plugin_server.anomalydetection.anomalyscore;

public class NormalizeAnomalyScore {

	public double normalizeAnomalyScore(final double anomalyScore, final double responseTime,
			final double forecastResponseTime) {

		final double normalizedAnomalyScore = Math.abs(anomalyScore
				/ (responseTime + forecastResponseTime));

		return normalizedAnomalyScore;
	}
}
