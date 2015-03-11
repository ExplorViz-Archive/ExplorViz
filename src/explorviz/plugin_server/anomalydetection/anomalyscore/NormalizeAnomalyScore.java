package explorviz.plugin_server.anomalydetection.anomalyscore;

/**
 *
 * This class normalize the calculated anomaly score
 *
 * @author Kim Christian Mannstedt
 * @author Enno Schwanke
 *
 */
public class NormalizeAnomalyScore {

	/**
	 *
	 * normalize the calculated anomaly score and return a value between -1 and
	 * 1
	 *
	 * @param anomalyScore
	 *            calculated anomaly score
	 * @param responseTime
	 *            actual response time
	 * @param forecastResponseTime
	 *            forecast response time for actual response time
	 * @return normalized anomaly score
	 */
	public double normalizeAnomalyScore(final double anomalyScore, final double responseTime,
			final double forecastResponseTime) {
		// Ergebnisraum von [0,1] zu [-1,1] geaendert
		// final double normalizedAnomalyScore = Math.abs(anomalyScore
		// / (responseTime + forecastResponseTime));
		final double normalizedAnomalyScore = anomalyScore / (responseTime + forecastResponseTime);
		if ((normalizedAnomalyScore < -1) || (normalizedAnomalyScore > 1)) {
			throw new CorruptedParametersException("The calculated anomaly score ("
					+ Double.toString(anomalyScore) + ") does not fit to the given response time ("
					+ Double.toString(responseTime) + ") and forecasted response time ("
					+ Double.toString(forecastResponseTime) + ").");
		}
		return normalizedAnomalyScore;
	}
}
