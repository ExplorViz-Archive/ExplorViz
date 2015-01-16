package explorviz.plugin.anomalydetection.anomalyscore;

import explorviz.plugin.anomalydetection.Configuration;

public class InterpreteAnomalyScore {

	public boolean[] interprete(final double anomalyScore) {
		// errorWarning[0] = warning; errorWarning[1] = error
		final boolean[] errorWarning = new boolean[] { false, false };

		// TODO use config values for warning and error
		if (anomalyScore > Configuration.WARNING_ANOMALY) {
			errorWarning[0] = true;
		}

		if (anomalyScore >= Configuration.ERROR_ANOMALY) {
			errorWarning[1] = true;
		}

		return errorWarning;
	}
}
