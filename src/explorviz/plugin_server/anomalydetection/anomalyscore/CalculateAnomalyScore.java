package explorviz.plugin_server.anomalydetection.anomalyscore;

/**
 * This class calculate the anomaly score
 *
 * @author Kim Christian Mannstedt
 * @author Enno Schwanke
 *
 */
public class CalculateAnomalyScore {

	/**
	 *
	 * @param responseTime
	 *            actual response Time
	 * @param forecastResponseTime
	 *            forecast response time for actual response time
	 * @return normalized anomaly score
	 * @throws CorruptedParametersException
	 *             throw exception if there are negative values for parameters
	 */
	public double getAnomalyScore(final double responseTime, final double forecastResponseTime)
			throws CorruptedParametersException {
		// Ergebnisraum von [0,x] zu [-x,x] geändert
		// final double score = Math.sqrt(Math.pow((responseTime -
		// forecastResponseTime), 2));
		if ((responseTime < 0) || (forecastResponseTime < 0)) {
			throw new CorruptedParametersException(
					"Response time or forecast response time is less than 0. That is not possible.");
		}
		final double score = responseTime - forecastResponseTime;

		if (score != 0) {
			final NormalizeAnomalyScore normalize = new NormalizeAnomalyScore();
			final double normalizedScore = normalize.normalizeAnomalyScore(score, responseTime,
					forecastResponseTime);
			return normalizedScore;
		} else {
			return score;
		}
	}
}
