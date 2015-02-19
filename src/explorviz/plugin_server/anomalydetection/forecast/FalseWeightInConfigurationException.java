package explorviz.plugin_server.anomalydetection.forecast;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This exception gets thrown if an invalid weighting value is used for the
 * WeightedForecaster
 *
 * @author Kim Christian Mannstedt
 *
 */
public class FalseWeightInConfigurationException extends RuntimeException {

	private static final Logger logger = Logger
			.getLogger((FalseWeightInConfigurationException.class).getName());

	/**
	 * Create exception with severe loggerlevel
	 *
	 * @param message
	 *            error message
	 */
	public FalseWeightInConfigurationException(String message) {
		super(message);
		logger.log(Level.SEVERE, message);
	}
}
