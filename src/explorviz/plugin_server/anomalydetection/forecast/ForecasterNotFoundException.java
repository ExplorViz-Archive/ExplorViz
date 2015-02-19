package explorviz.plugin_server.anomalydetection.forecast;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * This exception gets thrown if an unknown forecaster is set in the
 * configuration
 *
 * @author Kim Christian Mannstedt
 * @author Enno Schwanke
 *
 */
public class ForecasterNotFoundException extends RuntimeException {

	private static final Logger logger = Logger.getLogger(ForecasterNotFoundException.class
			.getName());

	/**
	 * Create the Exception with the severe logginlevel
	 *
	 * @param message
	 *            information message
	 */
	public ForecasterNotFoundException(String message) {
		super(message);
		logger.log(Level.SEVERE, message);
	}
}
