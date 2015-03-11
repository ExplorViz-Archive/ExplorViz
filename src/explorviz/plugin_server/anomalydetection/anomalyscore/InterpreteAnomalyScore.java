package explorviz.plugin_server.anomalydetection.anomalyscore;

import explorviz.plugin_server.anomalydetection.Configuration;

/**
 *
 * This class interprete the calculated anomaly score
 *
 * @author Kim Christian Mannstedt
 * @author Enno Schwanke
 *
 */
public class InterpreteAnomalyScore {

	/**
	 * set the boolean values for warning anomaly and error anomaly
	 *
	 * @param anomalyScore
	 *            calculated anomaly score
	 * @return boolean value for warning and error anomaly
	 */
	public boolean[] interprete(final double anomalyScore) {
		// errorWarning[0] = warning; errorWarning[1] = error
		if ((anomalyScore > 1) || (anomalyScore < -1)) {
			throw new CorruptedParametersException(
					"Anomaly Score must be a value between -1 and 1.");
		}

		final boolean[] errorWarning = new boolean[] { false, false };

		if ((anomalyScore >= Configuration.ERROR_ANOMALY)
				|| (anomalyScore <= -Configuration.ERROR_ANOMALY)) {
			errorWarning[1] = true;
			return errorWarning;
		}

		if ((anomalyScore >= Configuration.WARNING_ANOMALY)
				|| (anomalyScore <= -Configuration.WARNING_ANOMALY)) {
			errorWarning[0] = true;
			return errorWarning;
		}
		return errorWarning;

	}
}
