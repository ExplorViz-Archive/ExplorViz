package explorviz.plugin_server.anomalydetection.forecast;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FalseWeightInConfigurationException extends RuntimeException {

	private static final Logger logger = Logger
			.getLogger((FalseWeightInConfigurationException.class).getName());

	public FalseWeightInConfigurationException(String message) {
		super(message);
		logger.log(Level.SEVERE, message);
	}
}
