package explorviz.plugin_server.capacitymanagement.cloud_control.openstack;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.cloud_control.common.*;
import explorviz.plugin_server.capacitymanagement.configuration.InitialSetupReader;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.shared.model.*;

/**
 * Controls access to an OpenStackCloud. realizes Node-funcionality with
 * Nova-Client. Access to Applications via ssh.
 *
 * @author jek, jkr <br>
 *         Partly inspired by/taken from capacity-manager-project.
 *
 */

public class OpenStackCloudController implements ICloudController {

	private static final Logger LOG = LoggerFactory.getLogger(OpenStackCloudController.class);

	private final String keyPairName;

	private final String sshUsername;
	private final String sshPrivateKey;

	// private final String systemMonitoringFolder;
	// private final String startSystemMonitoringScript;

	/**
	 * Constructor.
	 *
	 * @param settings
	 *            CapManConfigurationFile
	 */
	public OpenStackCloudController(final CapManConfiguration settings) {

		keyPairName = settings.getCloudKey();

		sshPrivateKey = settings.getSSHPrivateKey();
		sshUsername = settings.getSSHUsername();

		// systemMonitoringFolder = settings.getSystemMonitoringFolder();
		// startSystemMonitoringScript =
		// settings.getStartSystemMonitoringScript();

	}

	@Override
	public String startNode(final NodeGroup nodegroup, Node nodeToStart) throws Exception {
		String privateIP;
		try {
			String instanceId = bootNewNodeInstanceFromImage(nodeToStart.getHostname(), nodegroup,
					nodeToStart.getImage(), nodeToStart.getFlavor());
			// TODO: konfigurierbar?
			Thread.sleep(30000);
			privateIP = retrievePrivateIPFromInstance(instanceId);

			copySystemMonitoringToInstance(privateIP);
			// Thread.sleep(30000);
			startSystemMonitoringOnInstance(privateIP);

		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			return null;
		}

		return privateIP;
	}

	@Override
	public boolean restartNode(final Node node) throws Exception {
		String ipAdress = node.getIpAddress();
		final String hostname = retrieveHostnameFromNode(node);
		LOG.info("CloudController: restarting node " + hostname);
		final String command = "reboot " + hostname;
		try {
			TerminalCommunication.executeNovaCommand(command);
		} catch (final Exception e) {
			LOG.info("Error during restarting node " + hostname);
			return false;
		}

		if (instanceExistingByIpAddress(hostname)) {
			startSystemMonitoringOnInstance(ipAdress);
			return true;
		} else {
			return false;
		}
	}

	public String retrieveIdFromNode(final Node node) throws Exception {
		String id = node.getId();
		if (id == null) {
			final String command = " list";
			final List<String> output = TerminalCommunication.executeNovaCommand(command);
			final String ipAddress = node.getIpAddress();
			for (final String row : output) {
				if (row.contains(ipAddress)) {
					final int end = row.indexOf(" | ", 1);
					id = row.substring(1, end).trim();
				}
			}
		}

		return id;
	}

	protected String retrieveImageFromNode(final Node node) throws Exception {
		String image = node.getImage();
		if (image == null) {
			final String id = retrieveIdFromNode(node);
			final String command = " show  " + id;
			final List<String> output = TerminalCommunication.executeNovaCommand(command);
			final StringToMapParser parser = new OpenStackOutputParser();
			parser.parseAndAddStringList(output);
			final String imageString = parser.getMap().get("image");
			// check whether also Id of image is present
			if (imageString.contains("(")) {
				final int end = imageString.indexOf(" (");
				image = imageString.substring(0, end);
			} else {
				image = imageString;
			}

		}
		return image;
	}

	protected String retrieveHostnameFromNode(final Node node) throws Exception {
		String hostname = node.getHostname();
		if (hostname == null) {
			final String command = "list";
			final List<String> output = TerminalCommunication.executeNovaCommand(command);
			final String ipAddress = node.getIpAddress();
			for (final String row : output) {
				if (row.contains(ipAddress)) {

					final int start = row.indexOf(" | ", 1) + 2; // zweite
					// Spalte
					final int end = row.substring(start).indexOf(" |") + start;
					hostname = row.substring(start, end).trim();

				}
			}
		}
		return hostname;
	}

	private String retrieveHostnameFromIP(String ipAddress) throws Exception {
		final String command = "list";
		final List<String> output = TerminalCommunication.executeNovaCommand(command);
		String hostname = null;
		for (final String row : output) {
			if (row.contains(ipAddress)) {
				final int start = row.indexOf(" | ", 1) + 2; // zweite
				// Spalte
				final int end = row.substring(start).indexOf(" |") + start;
				hostname = row.substring(start, end).trim();
			}
		}
		return hostname;
	}

	protected String retrieveStatusOfInstance(final String ipAddress) throws Exception {
		String status = "unknown";
		String[] columns;
		final String command = "list";
		final List<String> output = TerminalCommunication.executeNovaCommand(command);
		for (final String row : output) {
			if (row.contains(ipAddress)) {
				columns = row.split(" | ");
				status = columns[5];
				for (String column : columns) {
					java.lang.System.out.println(column);
				}
				return status;
			}
		}
		return status;
	}

	@Override
	public boolean terminateApplication(final Application application, ScalingGroup scalingGroup) {
		String privateIP = application.getParent().getIpAddress();
		String pid = application.getPid();
		String name = application.getName();
		try {

			LOG.info("Terminating application " + pid);

			SSHCommunication.runScriptViaSSH(privateIP, sshUsername, sshPrivateKey, "kill " + pid);

			waitFor(scalingGroup.getWaitTimeForApplicationActionInMillis(), "application terminate");
		} catch (final Exception e) {
			LOG.error("Error during terminating application" + name + e.getMessage());
			return false;
		}
		return !checkApplicationIsRunning(privateIP, pid, name);

	}

	/*
	 * Migration for applications.
	 * 
	 * @author jgi
	 * 
	 * @see
	 * explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController
	 * #migrateApplication(explorviz.shared.model.Application,
	 * explorviz.shared.model.Node,
	 * explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup)
	 */
	@Override
	public boolean migrateApplication(final Application application, final Node targetNode,
			final ScalingGroup scalingGroup) {
		String sourceIp = application.getParent().getIpAddress();
		String targetIp = targetNode.getIpAddress();
		try {
			// Terminate the application before working on it.
			if (terminateApplication(application, scalingGroup)) {

				// Delete old application from loadbalancer to delete ip.
				scalingGroup.removeApplication(application);

				// Copy the application folder from sourceNode to master temp
				// folder.
				copyTempApplicationFromInstance(sourceIp, application, scalingGroup);

				// Copy the application folder from master temp folder to
				// targetNode.
				copyTempApplicationToInstance(targetIp, application, scalingGroup);

				// Delete the contents of the master temp folder.
				cleanTempFolder();

				// Set the parent of the migrated application to target node.
				application.setParent(targetNode);

				// Start the application on the target node
				String pid = startApplication(application, scalingGroup);

				// Set the new process id.
				application.setPid(pid);

				// Add new application to loadbalancer to get new ip.
				scalingGroup.addApplication(application);
			} else {
				return false;
			}

		} catch (Exception e) {
			// Should be passed through to ApplicationMigrateAction and
			// then to ExecutionAction.
			e.printStackTrace();
		}
		String newPid = application.getPid();
		String appName = application.getName();
		return checkApplicationIsRunning(targetIp, newPid, appName);
	}

	@Override
	// TODO: return pid
	public boolean restartApplication(final Application application, ScalingGroup scalingGroup) {
		Node parent = application.getParent();
		final String privateIP = parent.getIpAddress();
		final String name = application.getName();
		String pid = application.getPid();

		if (terminateApplication(application, scalingGroup)) {

			try {
				startApplication(application, scalingGroup);
			} catch (final Exception e) {
				LOG.info("Error during restarting application" + name + e.getMessage());
				return false;
			}
		} else {
			return false;
		}
		return checkApplicationIsRunning(privateIP, pid, name);

	}

	@Override
	public Node replicateNode(final NodeGroup nodegroup, final Node originalNode) throws Exception {
		final String hostname = nodegroup.generateNewUniqueHostname();
		final Node newNode = new Node();
		try {
			final String image = createImageFromInstance(retrieveHostnameFromNode(originalNode));
			final String flavor = retrieveFlavorFromNode(originalNode);
			final String instanceId = bootNewNodeInstanceFromImage(hostname, nodegroup, image,
					flavor);

			waitForInstanceStart(120, hostname, 1000);

			final String privateIP = retrievePrivateIPFromInstance(instanceId);

			copySystemMonitoringToInstance(privateIP);
			// Thread.sleep(100000);
			startSystemMonitoringOnInstance(privateIP);

			LOG.info("Successfully started node " + hostname + " on " + privateIP);

			newNode.setIpAddress(privateIP);
			newNode.setHostname(hostname);
			newNode.setImage(image);
			newNode.setFlavor(flavor);
			newNode.setId(retrieveIdFromNode(newNode));
			return newNode;
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			// compensate
			terminateNode(newNode);
			return null;
		}

	}

	protected String retrieveFlavorFromNode(Node node) throws Exception {
		String flavor = node.getFlavor();
		if (flavor == null) {
			final String id = retrieveIdFromNode(node);
			final String command = " show  " + id;
			final List<String> output = TerminalCommunication.executeNovaCommand(command);
			final StringToMapParser parser = new OpenStackOutputParser();
			parser.parseAndAddStringList(output);
			final String flavorString = parser.getMap().get("flavor");
			// check whether also Id of image is present
			if (flavorString.contains("(")) {
				final int end = flavorString.indexOf(" (");
				flavor = flavorString.substring(0, end);
			} else {
				flavor = flavorString;
			}
		}
		return flavor;
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
	private String bootNewNodeInstanceFromImage(final String hostname, final NodeGroup nodegroup,
			final String image, final String flavor) throws Exception {
		LOG.info("Starting new instance in node group " + nodegroup.getName() + "...");
		final String bootCommand = "boot " + hostname + " --image " + image + " --flavor " + flavor
				+ " --key_name " + keyPairName;

		final List<String> output = TerminalCommunication.executeNovaCommand(bootCommand);

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
	private void waitForInstanceStart(int retryCount, final String hostname,
			final int sleepTimeInMilliseconds) throws Exception {
		LOG.info("Waiting for instance to start...");

		boolean started = false;
		while (retryCount > 0) {
			try {
				final List<String> statusOutput = TerminalCommunication
						.executeNovaCommand("console-log --length 10 " + hostname);

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
	 *            Instanceid or hostname to get ip from.
	 * @return Ip of Nodeinstance.
	 * @throws Exception
	 *             If private IP address not available.
	 */
	public String retrievePrivateIPFromInstance(final String instanceId) throws Exception {
		LOG.info("Getting private IP for instance " + instanceId);
		final List<String> output = TerminalCommunication.executeNovaCommand("show " + instanceId);
		final StringToMapParser parser = new OpenStackOutputParser();
		parser.parseAndAddStringList(output);

		final String privateIP = parser.getMap().get("vmnet network");

		if ((privateIP == null) || privateIP.equals("")) {
			throw new Exception("Private IP address not available.");
		}

		return privateIP;
	}

	protected String createImageFromInstance(final String hostname) throws Exception {
		final String imageName = hostname + "Image";
		LOG.info("Getting Image from " + hostname);
		TerminalCommunication.executeNovaCommand("image-create " + hostname + " " + imageName);
		List<String> output = TerminalCommunication.executeNovaCommand("image-list");
		for (final String outputline : output) {
			final String line = outputline.toLowerCase();
			if (line.contains(imageName)) {
				return imageName;
			}
		}
		LOG.error("Error while creating Image of host " + hostname);
		return "null";
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
	public void copyApplicationToInstance(final String privateIP, final Application app,
			ScalingGroup scalingGroup) throws Exception {
		LOG.info("Copying application '" + app.getName() + "' to node " + privateIP);

		final String copyApplicationCommand = "scp -o stricthostkeychecking=no -i " + sshPrivateKey
				+ " -r " + InitialSetupReader.getApplicationsFolderPath()
				+ scalingGroup.getApplicationFolder() + " " + sshUsername + "@" + privateIP
				+ ":/home/" + sshUsername + "/";

		TerminalCommunication.executeCommand(copyApplicationCommand);

	}

	/**
	 * Copy the application from master temp folder to targetNode
	 *
	 * @param targetIp
	 *            Ip adress of targetNode.
	 * @param app
	 *            Application to be migrated.
	 * @param scalingGroup
	 *            ScalingGroup the application belongs to.
	 * @throws Exception
	 */
	public void copyTempApplicationToInstance(final String targetIp, final Application app,
			ScalingGroup scalingGroup) throws Exception {
		LOG.info("Copying application '" + app.getName() + "from node " + "' to node " + targetIp);

		final String copyApplicationCommand = "scp -o stricthostkeychecking=no -i " + sshPrivateKey
				+ " -r " + "temp/" + InitialSetupReader.getApplicationsFolderPath()
				+ scalingGroup.getApplicationFolder() + " " + sshUsername + "@" + targetIp
				+ ":/home/" + sshUsername + "/";

		TerminalCommunication.executeCommand(copyApplicationCommand);
	}

	/**
	 * @author jgi Copy the application from the sourceNode to master temp
	 *         folder.
	 * @param sourceIp
	 *            Ip adress of sourceNode.
	 * @param app
	 *            Application to be migrated.
	 * @param scalingGroup
	 *            ScalingGroup the application belongs to.
	 * @throws Exception
	 */
	public void copyTempApplicationFromInstance(final String sourceIp, final Application app,
			ScalingGroup scalingGroup) throws Exception {
		LOG.info("Copying application '" + app.getName() + "' to node " + sourceIp);

		final String copyApplicationCommand = "scp -o stricthostkeychecking=no -i " + sshPrivateKey
				+ " -r " + sshUsername + "@" + sourceIp + ":/home/" + sshUsername + "/"
				+ InitialSetupReader.getApplicationsFolderPath()
				+ scalingGroup.getApplicationFolder() + " " + "temp";
		// TODO ausprobieren (wird temp automatisch erstellt?)

		TerminalCommunication.executeCommand(copyApplicationCommand);
	}

	/**
	 * @author jgi Delete contents of master temp folder.
	 * @throws Exception
	 */
	public void cleanTempFolder() throws Exception {
		final String cleanTempFolderCommand = "rm -r" + " " + "/home/" + sshUsername + "/temp/*";

		TerminalCommunication.executeCommand(cleanTempFolderCommand);
	}

	// private void copyAllApplicationsToInstance(final String privateIP, final
	// Node originalNode,
	// ScalingGroup scalingGroup) throws Exception {
	//
	// for (final Application app : originalNode.getApplications()) {
	// copyApplicationToInstance(privateIP, app, scalingGroup);
	//
	// }
	// }

	/**
	 * Start application on instance of Node.
	 *
	 * @param privateIP
	 *            Ip of Node for application to be started.
	 * @param nodegroup
	 *            Arraylist containing Node.
	 * @return Pid of started application
	 * @throws Exception
	 *             Starting the application failed.
	 */
	@Override
	public String startApplication(Application app, ScalingGroup scalingGroup) throws Exception {
		String privateIP = app.getParent().getIpAddress();
		String name = app.getName();

		StringBuffer arguments = new StringBuffer();
		for (String arg : app.getArguments()) {
			arguments.append(arg);
			arguments.append(" ");
		}

		String startscript = "cd " + scalingGroup.getApplicationFolder() // navigate
				// to
				// application
				// folder
				+ " && echo ' echo $! > pid' > getpidcommand " // create file
				// with
				// getpid command
				+ " && cat start.sh getpidcommand > script.sh " // combine
				// start-Skript
				// with getpid
				// command
				+ " && chmod a+x script.sh" // allow execution of file script.sh
				+ " && ./script.sh ";
		String script = startscript + arguments.toString();
		LOG.info("Starting application script - " + script + " - on node " + privateIP);
		SSHCommunication.runScriptViaSSH(privateIP, sshUsername, sshPrivateKey, script);
		waitFor(scalingGroup.getWaitTimeForApplicationActionInMillis(), "application start");
		// read pid from file
		String command = "cd " + scalingGroup.getApplicationFolder() + " && head pid";
		List<String> output = SSHCommunication.runScriptViaSSH(privateIP, sshUsername,
				sshPrivateKey, command);
		LOG.info("Output: " + output.toString());

		if (!output.isEmpty()) {
			String pid = output.get(0);
			if (checkApplicationIsRunning(privateIP, pid, name)) {
				return pid;
			}
		}
		return null;
	}

	// private boolean startAllApplicationsOnInstance(final String privateIP,
	// final Node node,
	// ScalingGroup scalingGroup) throws Exception {
	// String pid;
	// for (final Application app : node.getApplications()) {
	// pid = startApplication(app, scalingGroup);
	// if (pid == "null") {
	// return false;
	//
	// }
	// }
	// return true;
	// }

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
		// LOG.info("Copying system monitoring '" + systemMonitoringFolder +
		// "' to node " + privateIP);
		//
		// final String copySystemMonitoringCommand =
		// "scp -o stricthostkeychecking=no -i "
		// + sshPrivateKey + " -r " + systemMonitoringFolder + " " + sshUsername
		// + "@"
		// + privateIP + ":/home/" + sshUsername + "/";
		//
		// TerminalCommunication.executeCommand(copySystemMonitoringCommand);
	}

	private void startSystemMonitoringOnInstance(final String privateIP) throws Exception {
		// LOG.info("Starting system monitoring script - " +
		// startSystemMonitoringScript
		// + " - on node " + privateIP);
		//
		// SSHCommunication.runScriptViaSSH(privateIP, sshUsername,
		// sshPrivateKey,
		// startSystemMonitoringScript);
	}

	@Override
	public boolean terminateNode(final Node node) throws Exception {
		if (instanceExistingByIpAddress(retrieveHostnameFromNode(node))) { // if
			// called
			// as
			// compensate of
			// replicate
			// it's
			// possible,
			// that Node was
			// not started
			// yet
			LOG.info("Deleting node: " + retrieveHostnameFromNode(node));

			TerminalCommunication.executeNovaCommand("delete " + retrieveHostnameFromNode(node));

			LOG.info("Shut down node: " + retrieveHostnameFromNode(node));
		}
		return !instanceExistingByIpAddress(retrieveHostnameFromNode(node));

	}

	/**
	 * It is presumed that exceptions would only be thrown for communication
	 * errors. Therefore, catching them does not influence the rest of the
	 * controller (as all other commands would not work neither)
	 */

	public boolean instanceExistingByIpAddress(final String ipAdress) {
		String name = "";
		try {
			name = retrieveHostnameFromIP(ipAdress);
		} catch (final Exception e) {
			LOG.error("Error while retrievin hostname " + e.getMessage());
		}
		return instanceExistingByHostname(name);
	}

	public boolean instanceExistingByHostname(String hostname) {

		final String command = "list";
		List<String> output = new ArrayList<String>();
		try {
			output = TerminalCommunication.executeNovaCommand(command);
		} catch (final Exception e) {
			LOG.error("Error while listing instances " + e.getMessage());
		}
		for (final String outputline : output) {
			if (outputline.contains(hostname) && outputline.contains("ACTIVE")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc} Checks with the command "ps PID" whether the process of the
	 * application is running. If the output has only 1 row, there are just
	 * headings. 2 rows means, it is running.
	 */
	public boolean checkApplicationIsRunning(final String privateIP, final String pid,
			final String name) {
		List<String> output = new ArrayList<String>();
		LOG.info("Check if application " + name + " is running.");
		try {
			output = SSHCommunication.runScriptViaSSH(privateIP, sshUsername, sshPrivateKey, "ps "
					+ pid);
		} catch (Exception e) {
			LOG.error("Error while running ssh-command 'ps " + pid + "' on node " + privateIP + ":"
					+ e.getMessage());
		}
		LOG.info("Output: " + output);
		if (output.size() == 2) {
			LOG.info("Application " + name + "is running.");
			return true;
		} else {
			LOG.info("Application " + name + "is not running.");
			return false;
		}
		// List<String> output = new ArrayList<String>();
		// LOG.info("Check if application " + name + " is running.");
		// try {
		// output = SSHCommunication.runScriptViaSSH(privateIP, sshUsername,
		// sshPrivateKey,
		// "pidof java");
		// } catch (Exception e) {
		// LOG.info("Error while checking application running");
		// }
		// if (!output.isEmpty()) {
		// return output.contains(pid);
		// } else {
		// LOG.info("Application " + name + "is not running.");
		// return false;
		// }
	}

	/**
	 * Count number of entries returned from 'nova list' command.
	 */
	@Override
	public int retrieveRunningNodeCount() {
		String command = "list ";
		List<String> output = new ArrayList<String>();
		try {
			output = TerminalCommunication.executeNovaCommand(command);
		} catch (Exception e) {
			LOG.error("Error while getting Node Count.");

		}

		return output.size();

	}
}
