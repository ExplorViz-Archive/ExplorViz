package explorviz.plugin_server.anomalydetection.anomalyscore;

public class CalculateAnomalyScore {

	public double getAnomalyScore(final double responseTime, final double forecastResponseTime) {
		// Ergebnisraum von [0,x] zu [-x,x] geändert
		// final double score = Math.sqrt(Math.pow((responseTime -
		// forecastResponseTime), 2));
		final double score = responseTime - forecastResponseTime;

		final NormalizeAnomalyScore normalize = new NormalizeAnomalyScore();
		final double normalizedScore = normalize.normalizeAnomalyScore(score, responseTime,
				forecastResponseTime);

		return normalizedScore;
	}
}
