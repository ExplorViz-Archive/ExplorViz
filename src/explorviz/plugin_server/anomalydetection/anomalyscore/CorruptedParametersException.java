package explorviz.plugin_server.anomalydetection.anomalyscore;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CorruptedParametersException extends RuntimeException {

	private static final Logger logger = Logger.getLogger(CorruptedParametersException.class
			.getName());

	public CorruptedParametersException(String message) {
		super(message);
		logger.log(Level.SEVERE, message);
	}

}
