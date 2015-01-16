package explorviz.plugin.anomalydetection.anomalyscore;

public class CalculateAnomalyScore {

	public double getAnomalyScore(final double responseTime, final double forecastResponseTime) {
		final double score = Math.sqrt(Math.pow((responseTime - forecastResponseTime), 2));

		final NormalizeAnomalyScore normalize = new NormalizeAnomalyScore();
		final double normalizedScore = normalize.normalizeAnomalyScore(score, responseTime,
				forecastResponseTime);

		return normalizedScore;
	}
}
