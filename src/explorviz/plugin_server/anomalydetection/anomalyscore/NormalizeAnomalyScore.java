package explorviz.plugin_server.anomalydetection.anomalyscore;

public class NormalizeAnomalyScore {

	public double normalizeAnomalyScore(final double anomalyScore, final double responseTime,
			final double forecastResponseTime) {
		// Ergebnisraum von [0,1] zu [-1,1] geändert
		// final double normalizedAnomalyScore = Math.abs(anomalyScore
		// / (responseTime + forecastResponseTime));
		final double normalizedAnomalyScore = anomalyScore / (responseTime + forecastResponseTime);
		return normalizedAnomalyScore;
	}
}
