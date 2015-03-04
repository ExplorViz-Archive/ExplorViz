package explorviz.plugin_server.capacitymanagement.configuration;

/**
 * Exception is thrown, if a configuration file is not consistent.
 */
public class InvalidConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidConfigurationException(final String message) {
		super(message);
	}

}
