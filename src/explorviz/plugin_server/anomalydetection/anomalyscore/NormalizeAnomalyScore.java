package explorviz.plugin_server.anomalydetection.anomalyscore;

public class NormalizeAnomalyScore {

	public double normalizeAnomalyScore(final double anomalyScore, final double responseTime,
			final double forecastResponseTime) {
		// Ergebnisraum von [0,1] zu [-1,1] geändert
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
