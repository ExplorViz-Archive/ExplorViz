package explorviz.plugin_server.rootcausedetection.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This exception gets thrown if an invalid targetHash is used with the
 * DistanceGraph
 *
 * @author Christian Jens Michaelis
 *
 */
public class InvalidDistanceGraphException extends RuntimeException {

	private static final long serialVersionUID = 9133500243492642468L;

	private static final Logger log = Logger.getLogger(InvalidDistanceGraphException.class
			.getName());

	/**
	 * Create this exception and log it with a severe level.
	 *
	 * @param message
	 *            error message
	 */
	public InvalidDistanceGraphException(final String message) {
		super(message);

		log.log(Level.SEVERE, message);
	}

}
