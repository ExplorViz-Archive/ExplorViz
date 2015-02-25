package explorviz.plugin_client.capacitymanagement.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author jgi, dtj Initialize cloud configuration (properties) and set scale
 *         for cpu threshold. Set strategies and nodestrategy-properties.
 */
public class CapManConfiguration {

	// //////////////////////////////strategies/////////////////////////////////

	private final String scalingStrategy;
	private final String cloudProvider;

	private int maxTriesForCloud = 1;
	private int maxTriesUntilCompensate = 1;

	// ////////////////////////nodeStrategy properties//////////////////////////

	private final int waitTimeForNewPlan;

	// ////////////////////////////cloud properties/////////////////////////////

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

		scalingStrategy = settings.getProperty("scalingStrategy");
		cloudProvider = settings.getProperty("cloudProvider");

		waitTimeForNewPlan = Integer.parseInt(settings.getProperty("waitTimeForNewPlan"));
		maxTriesForCloud = Integer.parseInt(settings.getProperty("maxTriesForCloud"));
		maxTriesUntilCompensate = Integer.parseInt(settings.getProperty("maxTriesUntilCompensate"));

		waitTimeBeforeNewBootInMillis = Integer.parseInt(settings
				.getProperty("waitTimeBeforeNewBootInMillis"));

		cloudNodeLimit = Integer.parseInt(settings.getProperty("cloudNodeLimit"));
		cloudKey = settings.getProperty("cloudKey");

		sshUsername = settings.getProperty("sshUsername");
		sshPrivateKey = settings.getProperty("sshPrivateKey");

		systemMonitoringFolder = settings.getProperty("systemMonitoringFolder");
		startSystemMonitoringScript = settings.getProperty("startSystemMonitoringScript");
	}

	public String getScalingStrategy() {
		return scalingStrategy;
	}

	public String getCloudProvider() {
		return cloudProvider;
	}

	public int getWaitTimeForNewPlan() {
		return waitTimeForNewPlan;
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

	public int getMaxTriesForCloud() {
		return maxTriesForCloud;
	}

	public int getMaxTriesUntilCompensate() {
		return maxTriesUntilCompensate;
	}

	// TODO: remove when finished
	public CapManConfiguration() {

		scalingStrategy = "ScalingStrategyPerformance";
		waitTimeForNewPlan = 600;

		cloudProvider = "explorviz.plugin_server.capacitymanagement.cloud_control.openstack.OpenStackCloudController";

		cloudNodeLimit = 256;
		cloudKey = "slastic";

		waitTimeBeforeNewBootInMillis = 30000;

		sshUsername = "ubuntu";
		sshPrivateKey = "/home/ubuntu/.ssh/id_rsa";

		systemMonitoringFolder = "system-monitor";
		startSystemMonitoringScript = "cd system-monitor && chmod a+x start.sh && ./start.sh";

		// loadBalancersCount=1;

		// loadBalancer1Host="localhost";
		// loadBalancer1Port=10200;

	}
}
