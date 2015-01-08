package explorviz.plugin.anomalydetection.anomalyscore;

public class InterpreteAnomalyScore {

	public boolean[] interprete(final double anomalyScore) {
		// errorWarning[0] = warning; errorWarning[1] = error
		final boolean[] errorWarning = new boolean[] { false, false };

		// TODO use config values for warning and error
		if (anomalyScore > 0.5) {
			errorWarning[0] = true;
		}

		if (anomalyScore >= 0.7) {
			errorWarning[1] = true;
		}

		return errorWarning;
	}
}
