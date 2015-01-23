package explorviz.plugin_server.anomalydetection.anomalyscore;

public class CalculateAnomalyScore {

	public double getAnomalyScore(final double responseTime, final double forecastResponseTime) {
		// TODO Ergebnisraum von [0,1] zu [-1,1] ändern
		final double score = Math.sqrt(Math.pow((responseTime - forecastResponseTime), 2));

		final NormalizeAnomalyScore normalize = new NormalizeAnomalyScore();
		final double normalizedScore = normalize.normalizeAnomalyScore(score, responseTime,
				forecastResponseTime);

		return normalizedScore;
	}
}
