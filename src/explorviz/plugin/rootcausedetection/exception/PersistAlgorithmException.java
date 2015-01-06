package explorviz.plugin.rootcausedetection.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This exception gets thrown if there was an error while persisting
 * RootCauseRatings.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class PersistAlgorithmException extends RuntimeException {

	private static final long serialVersionUID = -6693146043190820200L;

	private static final Logger log = Logger.getLogger(PersistAlgorithmException.class.getName());

	/**
	 * Create this exception and log it with a severe level.
	 *
	 * @param message
	 *            error message
	 */
	public PersistAlgorithmException(final String message) {
		super(message);

		log.log(Level.SEVERE, message);
	}
}
