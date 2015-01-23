package explorviz.plugin_server.anomalydetection.anomalyscore;

public class NormalizeAnomalyScore {

	public double normalizeAnomalyScore(final double anomalyScore, final double responseTime,
			final double forecastResponseTime) {
		// TODO Ergebnisraum von [0,1] zu [-1,1] ändern
		final double normalizedAnomalyScore = Math.abs(anomalyScore
				/ (responseTime + forecastResponseTime));

		return normalizedAnomalyScore;
	}
}
