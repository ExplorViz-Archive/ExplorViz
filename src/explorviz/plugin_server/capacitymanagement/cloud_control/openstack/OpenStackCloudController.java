package explorviz.plugin_server.capacitymanagement.cloud_control.openstack;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.cloud_control.common.*;
import explorviz.plugin_server.capacitymanagement.todo.CapManUtil;
import explorviz.shared.model.*;

/**
 * @author jgi, dtj Get key pair, ssh username, private ssh key, system
 *         monitoring folder, system monitoring script from Configuration.
 *         Starts and shuts down Nodes.
 *
 */
public class OpenStackCloudController implements ICloudController {

	private static final Logger LOG = LoggerFactory.getLogger(OpenStackCloudController.class);

	private final String keyPairName;

	private final String sshUsername;
	private final String sshPrivateKey;

	private final String systemMonitoringFolder;
	private final String startSystemMonitoringScript;

	public OpenStackCloudController(final CapManConfiguration settings) {

		keyPairName = settings.getCloudKey();

		sshPrivateKey = settings.getSSHPrivateKey();
		sshUsername = settings.getSSHUsername();

		systemMonitoringFolder = settings.getSystemMonitoringFolder();
		startSystemMonitoringScript = settings.getStartSystemMonitoringScript();
	}

	// TODO jek/jkr: müssen die Node-Aktionen auch jeweils ihre Applikationen
	// ansteuern?

	public Node startNode(final NodeGroup nodegroup) {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	// public boolean shutdownNode(final Node node) {
	// return false;
	//
	// }

	@Override
	public boolean restartNode(final Node node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean terminateApplication(final Application application) {
		return false;

	}

	@Override
	public boolean migrateApplication(final Application application, final Node node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean restartApplication(final Application application) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Node replicateNode(final NodeGroup nodegroup, final Node originalNode) {
		final String hostname = nodegroup.generateNewUniqueHostname();

		try {
			final String instanceId = bootNewNodeInstance(hostname, nodegroup);

			waitForInstanceStart(120, instanceId, 1000);

			final String privateIP = getPrivateIPFromInstance(instanceId);

			copyAllApplicationsToInstance(privateIP, originalNode);
			startAllApplicationsOnInstance(privateIP, originalNode);

			copySystemMonitoringToInstance(privateIP);
			startSystemMonitoringOnInstance(privateIP);

			LOG.info("Successfully started node " + hostname + " on " + privateIP);

			final Node newNode = new Node();
			newNode.setIpAddress(privateIP);
			// TODO: jek, jkr: instanceID brauchen wir die?
			CapManUtil.setHostname(newNode, hostname);
			return newNode;
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			// compensate
			shutDownNodeByHostname(hostname);
			return null;
		}

	}

	/**
	 * Start new instance within NodeGroup with newly generated hostname.
	 *
	 * @param hostname
	 *            New generated (startNode) unique hostname.
	 * @param nodeGroup
	 *            NodeGroup in which the instance should be started.
	 * @return Id of new Instance.
	 * @throws Exception
	 *             E.g. booterror.
	 */
	private String bootNewNodeInstance(final String hostname, final NodeGroup nodegroup)
			throws Exception {
		LOG.info("Starting new instance in node group " + nodegroup.getName() + "...");
		final String bootCommand = "nova boot " + hostname + " --image " + CapManUtil.getImage()
				+ " --flavor " + CapManUtil.getFlavor() + " --key_name " + keyPairName;

		final List<String> output = TerminalCommunication.executeCommand(bootCommand);

		final StringToMapParser parser = new OpenStackOutputParser();
		parser.parseAndAddStringList(output);

		final String instanceId = parser.getMap().get("id");

		if ((instanceId == null) || instanceId.isEmpty()
				|| "Error".equalsIgnoreCase(parser.getMap().get("status"))) {
			throw new Exception("Error at instance boot!");
		}

		return instanceId;
	}

	/**
	 * @param retryCount
	 *            Number of retries before throwing exception.
	 * @param instanceId
	 *            Id of Nodeinstance to be started.
	 * @param sleepTimeInMilliseconds
	 *            Do nothing.
	 * @throws Exception
	 *             If Nodeinstance could not be started.
	 */
	// TODO: jek/jkr: besser mit Ping
	private void waitForInstanceStart(int retryCount, final String instanceId,
			final int sleepTimeInMilliseconds) throws Exception {
		LOG.info("Waiting for instance to start...");

		boolean started = false;
		while (retryCount > 0) {
			try {
				final List<String> statusOutput = TerminalCommunication
						.executeCommand("nova console-log --length 10 " + instanceId);

				for (final String outputline : statusOutput) {
					final String line = outputline.toLowerCase();
					if (line.contains("finished at ")) {
						LOG.info(line);
						started = true;
						break;
					}
				}
			} catch (final Exception e) {
				final String errorString = e.getMessage().toLowerCase();
				if (errorString.contains("error: instance") && errorString.contains("is not ready")) {
					started = false;
					LOG.info(e.getMessage(), e);
				}
			}

			if (started) {
				break;
			}
			try {
				Thread.sleep(sleepTimeInMilliseconds);
			} catch (final InterruptedException e) {
			}

			retryCount--;
		}

		if (!started) {
			throw new Exception("Instance could not be started");
		}
	}

	/**
	 * @param instanceId
	 *            Instanceid to get ip from.
	 * @return Ip of Nodeinstance.
	 * @throws Exception
	 *             If private IP address not available.
	 */
	private String getPrivateIPFromInstance(final String instanceId) throws Exception {
		LOG.info("Getting private IP for instance " + instanceId);
		final List<String> output = TerminalCommunication.executeCommand("nova show " + instanceId);
		final StringToMapParser parser = new OpenStackOutputParser();
		parser.parseAndAddStringList(output);

		final String privateIP = parser.getMap().get("vmnet network");

		if ((privateIP == null) || privateIP.equals("")) {
			throw new Exception("Private IP address not available.");
		}

		return privateIP;
	}

	/**
	 * Copy application to instance of Node.
	 *
	 * @param privateIP
	 *            Ip of Node for application to be copied.
	 * @param nodeGroup
	 *            Arraylist containing Node.
	 * @throws Exception
	 *             Copying failed.
	 */
	private void copyApplicationToInstance(final String privateIP, final Application app)
			throws Exception {
		// TODO: jek/ jkr: iteriere durch alle Applikationen eines Nodes
		LOG.info("Copying application '" + CapManUtil.getApplicationFolder() + "' to node "
				+ privateIP);

		final String copyApplicationCommand = "scp -o stricthostkeychecking=no -i " + sshPrivateKey
				+ " -r " + CapManUtil.getApplicationFolder() + " " + sshUsername + "@" + privateIP
				+ ":/home/" + sshUsername + "/";

		TerminalCommunication.executeCommand(copyApplicationCommand);
	}

	private void copyAllApplicationsToInstance(final String privateIP, final Node originalNode)
			throws Exception {

		for (final Application app : originalNode.getApplications()) {
			copyApplicationToInstance(privateIP, app);

		}
	}

	/**
	 * Start application on instance of Node.
	 *
	 * @param privateIP
	 *            Ip of Node for application to be started.
	 * @param nodegroup
	 *            Arraylist containing Node.
	 * @throws Exception
	 *             Starting the application failed.
	 */
	private void startApplicationOnInstance(final String privateIP, final String startscript,
			final int waitTimeInMillis) throws Exception {
		LOG.info("Starting application script - " + startscript + " - on node " + privateIP);

		SSHCommunication.runScriptViaSSH(privateIP, sshUsername, sshPrivateKey, startscript);
		waitFor(waitTimeInMillis, "application start");
	}

	private void startAllApplicationsOnInstance(final String privateIP, final Node node)
			throws Exception {
		for (final Application app : node.getApplications()) {
			startApplicationOnInstance(privateIP, app.getStartScript(),
					app.getWaitTimeForStarting());

		}
	}

	private void waitFor(final int millis, final String description) {
		LOG.info("Waiting " + millis + " milliseconds for " + description + "...");
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
		}
	}

	/**
	 * @param privateIP
	 *            Ip of Node for Application.
	 * @throws Exception
	 *             Copying System Monitoring failed.
	 */
	private void copySystemMonitoringToInstance(final String privateIP) throws Exception {
		LOG.info("Copying system monitoring '" + systemMonitoringFolder + "' to node " + privateIP);

		final String copySystemMonitoringCommand = "scp -o stricthostkeychecking=no -i "
				+ sshPrivateKey + " -r " + systemMonitoringFolder + " " + sshUsername + "@"
				+ privateIP + ":/home/" + sshUsername + "/";

		TerminalCommunication.executeCommand(copySystemMonitoringCommand);
	}

	private void startSystemMonitoringOnInstance(final String privateIP) throws Exception {
		LOG.info("Starting system monitoring script - " + startSystemMonitoringScript
				+ " - on node " + privateIP);

		SSHCommunication.runScriptViaSSH(privateIP, sshUsername, sshPrivateKey,
				startSystemMonitoringScript);
	}

	private void shutDownNodeByHostname(final String hostName) {

	}

	@Override
	public boolean terminateNode(final Node node) throws Exception {
		LOG.info("Deleting node: " + CapManUtil.getHostname(node));

		TerminalCommunication.executeCommand("nova delete " + CapManUtil.getHostname(node));

		LOG.info("Shut down node: " + CapManUtil.getHostname(node));
		// überdenken
		return !instanceExisting(CapManUtil.getHostname(node));

	}

	/**
	 * It is presumed that exceptions would only be thrown for communication
	 * errors. Therefore, catching them does not influence the rest of the
	 * controller (as all other commands would not work neither)
	 */
	// überdenken
	private boolean instanceExisting(final String name) {
		final String command = "nova list";
		List<String> output = new ArrayList<String>();
		try {
			output = TerminalCommunication.executeCommand(command);
		} catch (final Exception e) {
			LOG.error("Error while listing instances " + e.getMessage());
		}
		for (final String outputline : output) {
			if (outputline.contains(name)) {
				return true;
			}
		}
		return false;
	}
}
