package explorviz.plugin.anomalydetection.anomalyscore;

public class CalculateAnomalyScore {

	public double getAnomalyScore(final double responseTime, final double forecastResponseTime) {
		final double score = 0;

		// TODO calculate anomaly score

		final NormalizeAnomalyScore normalize = new NormalizeAnomalyScore();
		final double normalizedScore = normalize.normalizeAnomalyScore(score);

		return normalizedScore;
	}
}
