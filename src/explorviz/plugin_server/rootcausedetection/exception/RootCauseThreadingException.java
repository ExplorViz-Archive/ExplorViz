package explorviz.plugin_server.rootcausedetection.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import explorviz.plugin_server.rootcausedetection.util.RCDThreadPool;

/**
 * This exception gets thrown if we somehow could not wait for the threads to
 * finish in {@link RCDThreadPool#startThreads()
 * RCDThreadPool.startThreads(...)}.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class RootCauseThreadingException extends RuntimeException {

	private static final long serialVersionUID = 2761428471599895539L;

	private static final Logger log = Logger.getLogger(RootCauseThreadingException.class.getName());

	/**
	 * Create this exception and log it with a severe level.
	 *
	 * @param message
	 *            error message
	 */
	public RootCauseThreadingException(final String message) {
		super(message);

		log.log(Level.SEVERE, message);
	}
}
