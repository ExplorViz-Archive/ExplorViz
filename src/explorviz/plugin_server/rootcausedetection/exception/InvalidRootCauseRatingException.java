package explorviz.plugin_server.rootcausedetection.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This exception gets thrown if an invalid RootCauseRating was supposed to be
 * written (e. g. less than 0 or greater than 1).
 *
 * @author Christian Claus Wiechmann
 *
 */
public class InvalidRootCauseRatingException extends RuntimeException {

	private static final long serialVersionUID = 9133100243492642468L;

	private static final Logger log = Logger.getLogger(InvalidRootCauseRatingException.class
			.getName());

	/**
	 * Create this exception and log it with a severe level.
	 *
	 * @param message
	 *            error message
	 */
	public InvalidRootCauseRatingException(final String message) {
		super(message);

		log.log(Level.SEVERE, message);
	}

}
