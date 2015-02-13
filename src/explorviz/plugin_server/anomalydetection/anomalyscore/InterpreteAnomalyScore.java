package explorviz.plugin_server.anomalydetection.anomalyscore;

import explorviz.plugin_server.anomalydetection.Configuration;

public class InterpreteAnomalyScore {

	public boolean[] interprete(final double anomalyScore) {
		// errorWarning[0] = warning; errorWarning[1] = error
		if ((anomalyScore > 1) || (anomalyScore < -1)) {
			throw new CorruptedParametersException("Anomaly Score must be a value between -1 and 1.");
		}

		final boolean[] errorWarning = new boolean[] { false, false };

		if ((anomalyScore >= Configuration.WARNING_ANOMALY)
				|| (anomalyScore <= -Configuration.WARNING_ANOMALY)) {
			errorWarning[0] = true;
		}

		if ((anomalyScore >= Configuration.ERROR_ANOMALY)
				|| (anomalyScore <= -Configuration.ERROR_ANOMALY)) {
			errorWarning[1] = true;
		}

		return errorWarning;
	}
}
