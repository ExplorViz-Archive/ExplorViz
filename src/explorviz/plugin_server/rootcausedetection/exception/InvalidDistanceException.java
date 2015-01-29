package explorviz.plugin_server.rootcausedetection.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This exception gets thrown if an invalid distance was supposed to be written
 * (less than 1).
 *
 * @author Christian Jens Michaelis
 *
 */
public class InvalidDistanceException extends RuntimeException {

	private static final long serialVersionUID = 9133500243492642468L;

	private static final Logger log = Logger.getLogger(InvalidRootCauseRatingException.class
			.getName());

	/**
	 * Create this exception and log it with a severe level.
	 *
	 * @param message
	 *            error message
	 */
	public InvalidDistanceException(final String message) {
		super(message);

		log.log(Level.SEVERE, message);
	}

}
