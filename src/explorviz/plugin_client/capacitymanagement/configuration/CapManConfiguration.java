package explorviz.plugin_client.capacitymanagement.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author jgi, dtj Initialize cloud configuration (properties) and set scale
 *         for cpu threshold. Set strategies and nodestrategy-properties.
 */
public class CapManConfiguration {
	private final int cpuUtilizationReaderListenerPort;

	// //////////////////////////////strategies/////////////////////////////////

	private final String scalingStrategy;
	private final String cloudProvider;

	// ////////////////////////nodeStrategy properties//////////////////////////

	private final double scalingLowCpuThreshold;
	private final double scalingHighCpuThreshold;
	private final int cpuUtilizationHistoryLimit;
	private final int averageCpuUtilizationTimeWindowInMillisecond;

	// ////////////////////////////cloud properties/////////////////////////////

	private final int shutdownDelayInMillis;
	private final int waitTimeBeforeNewBootInMillis;

	private final int cloudNodeLimit;
	private final String cloudKey;

	private final String sshUsername;
	private final String sshPrivateKey;

	private final String systemMonitoringFolder;
	private final String startSystemMonitoringScript;

	/**
	 * Write values from settingsfile into variables.
	 * 
	 * @param filename
	 *            Load settings from file (uses file from main class).
	 * @throws IOException
	 *             If file was was not found/ could not be read etc..
	 */
	public CapManConfiguration(final String filename) throws IOException {
		final Properties settings = new Properties();
		settings.load(new FileInputStream(filename));

		cpuUtilizationReaderListenerPort = new Integer(
				settings.getProperty("cpuUtilizationReaderListenerPort"));

		scalingStrategy = settings.getProperty("scalingStrategy");
		cloudProvider = settings.getProperty("cloudProvider");

		scalingLowCpuThreshold = Double.parseDouble(settings.getProperty("scalingLowCpuThreshold"));
		scalingHighCpuThreshold = Double.parseDouble(settings
				.getProperty("scalingHighCpuThreshold"));
		cpuUtilizationHistoryLimit = Integer.parseInt(settings
				.getProperty("cpuUtilizationHistoryLimit"));
		averageCpuUtilizationTimeWindowInMillisecond = Integer.parseInt(settings
				.getProperty("averageCpuUtilizationTimeWindowInMillisecond"));

		shutdownDelayInMillis = Integer.parseInt(settings.getProperty("shutdownDelayInMillis"));
		waitTimeBeforeNewBootInMillis = Integer.parseInt(settings
				.getProperty("waitTimeBeforeNewBootInMillis"));

		cloudNodeLimit = Integer.parseInt(settings.getProperty("cloudNodeLimit"));
		cloudKey = settings.getProperty("cloudKey");

		sshUsername = settings.getProperty("sshUsername");
		sshPrivateKey = settings.getProperty("sshPrivateKey");

		systemMonitoringFolder = settings.getProperty("systemMonitoringFolder");
		startSystemMonitoringScript = settings.getProperty("startSystemMonitoringScript");
	}

	public int getCpuUtilizationReaderListenerPort() {
		return cpuUtilizationReaderListenerPort;
	}

	public String getScalingStrategy() {
		return scalingStrategy;
	}

	public String getCloudProvider() {
		return cloudProvider;
	}

	public double getScalingLowCpuThreshold() {
		return scalingLowCpuThreshold;
	}

	public double getScalingHighCpuThreshold() {
		return scalingHighCpuThreshold;
	}

	public int getCpuUtilizationHistoryLimit() {
		return cpuUtilizationHistoryLimit;
	}

	public int getAverageCpuUtilizationTimeWindowInMillisecond() {
		return averageCpuUtilizationTimeWindowInMillisecond;
	}

	public int getShutdownDelayInMillis() {
		return shutdownDelayInMillis;
	}

	public int getWaitTimeBeforeNewBootInMillis() {
		return waitTimeBeforeNewBootInMillis;
	}

	public int getCloudNodeLimit() {
		return cloudNodeLimit;
	}

	public String getCloudKey() {
		return cloudKey;
	}

	public String getSSHUsername() {
		return sshUsername;
	}

	public String getSSHPrivateKey() {
		return sshPrivateKey;
	}

	public String getSystemMonitoringFolder() {
		return systemMonitoringFolder;
	}

	public String getStartSystemMonitoringScript() {
		return startSystemMonitoringScript;
	}
}
