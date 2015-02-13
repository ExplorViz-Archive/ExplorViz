package explorviz.plugin_server.anomalydetection.forecast;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ForecasterNotFoundException extends RuntimeException {

	private static final Logger logger = Logger.getLogger(ForecasterNotFoundException.class
			.getName());

	public ForecasterNotFoundException(String message) {
		super(message);
		logger.log(Level.SEVERE, message);
	}
}
