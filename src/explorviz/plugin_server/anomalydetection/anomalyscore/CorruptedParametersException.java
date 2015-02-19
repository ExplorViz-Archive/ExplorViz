package explorviz.plugin_server.anomalydetection.anomalyscore;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This exception gets thrown if there are corrupted Parameters
 *
 * @author Enno Schwanke
 *
 */
public class CorruptedParametersException extends RuntimeException {

	private static final Logger logger = Logger.getLogger(CorruptedParametersException.class
			.getName());

	/**
	 * Create exception with severe logginglevel
	 *
	 * @param message
	 *            information message
	 */
	public CorruptedParametersException(String message) {
		super(message);
		logger.log(Level.SEVERE, message);
	}

}
