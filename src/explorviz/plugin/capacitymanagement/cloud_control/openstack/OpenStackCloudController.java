package explorviz.plugin.capacitymanagement.cloud_control.openstack;

import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.configuration.CapManConfiguration;
import explorviz.shared.model.*;

/**
 * @author jgi, dtj Get key pair, ssh username, private ssh key, system
 *         monitoring folder, system monitoring script from Configuration.
 *         Starts and shuts down Nodes.
 *
 */
public class OpenStackCloudController implements ICloudController {

	public OpenStackCloudController(final CapManConfiguration configuration) {
	}

	// TODO jek/jkr: müssen die Node-Aktionen auch jeweils ihre Applikationen
	// ansteuern?

	@Override
	public Node startNode(final NodeGroup nodegroup) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Node cloneNode(final NodeGroup nodegroup, final Node originalNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean shutdownNode(final Node node) {
		return false;

	}

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

	// private static final Logger LOG =
	// LoggerFactory.getLogger(OpenStackCloudController.class);
	//
	// private final String keyPairName;
	//
	// private final String sshUsername;
	// private final String sshPrivateKey;
	//
	// private final String systemMonitoringFolder;
	// private final String startSystemMonitoringScript;
	//
	// public OpenStackCloudController(final CapManConfiguration settings) {
	//
	// keyPairName = settings.getCloudKey();
	//
	// sshPrivateKey = settings.getSSHPrivateKey();
	// sshUsername = settings.getSSHUsername();
	//
	// systemMonitoringFolder = settings.getSystemMonitoringFolder();
	// startSystemMonitoringScript = settings.getStartSystemMonitoringScript();
	// }
	//
	// @Override
	// public Node startNode(final ScalingGroup scalingGroup) throws Exception {
	// final String hostname = scalingGroup.generateNewUniqueHostname();
	//
	// try {
	// final String instanceId = bootNewInstance(hostname, scalingGroup);
	//
	// waitForInstanceStart(120, instanceId, 1000);
	//
	// final String privateIP = getPrivateIPFromInstance(instanceId);
	//
	// copyApplicationToInstance(privateIP, scalingGroup);
	// startApplicationOnInstance(privateIP, scalingGroup);
	// waitForApplicationStart(privateIP, scalingGroup);
	//
	// copySystemMonitoringToInstance(privateIP);
	// startSystemMonitoringOnInstance(privateIP);
	//
	// LOG.info("Successfully started node " + hostname + " on " + privateIP);
	//
	// scalingGroup.addNode(privateIP, instanceId, hostname);
	// return scalingGroup.getNodeByHostname(hostname);
	// } catch (final Exception e) {
	// LOG.error(e.getMessage(), e);
	// shutDownNodeByHostname(hostname);
	// }
	// return null;
	// }
	//
	// /**
	// * Start new instance within ScalingGroup with newly generated hostname.
	// *
	// * @param hostname
	// * New generated (startNode) unique hostname.
	// * @param scalingGroup
	// * ScalingGroup in which the instance should be started.
	// * @return Id of new Instance.
	// * @throws Exception
	// * E.g. booterror.
	// */
	// private String bootNewInstance(final String hostname, final ScalingGroup
	// scalingGroup)
	// throws Exception {
	// LOG.info("Starting new instance of type " +
	// scalingGroup.getApplicationFolder() + "...");
	// final String bootCommand = "nova boot " + hostname + " --image " +
	// scalingGroup.getImage()
	// + " --flavor " + scalingGroup.getFlavor() + " --key_name " + keyPairName;
	//
	// final List<String> output =
	// TerminalCommunication.executeCommand(bootCommand);
	//
	// final StringToMapParser parser = new OpenStackOutputParser();
	// parser.parseAndAddStringList(output);
	//
	// final String instanceId = parser.getMap().get("id");
	//
	// if ((instanceId == null) || instanceId.isEmpty()
	// || "Error".equalsIgnoreCase(parser.getMap().get("status"))) {
	// throw new Exception("Error at instance boot!");
	// }
	//
	// return instanceId;
	// }
	//
	// /**
	// * @param retryCount
	// * Number of retries before throwing exception.
	// * @param instanceId
	// * Id of Nodeinstance to be started.
	// * @param sleepTimeInMilliseconds
	// * Do nothing.
	// * @throws Exception
	// * If Nodeinstance could not be started.
	// */
	// private void waitForInstanceStart(int retryCount, final String
	// instanceId,
	// final int sleepTimeInMilliseconds) throws Exception {
	// LOG.info("Waiting for instance to start...");
	//
	// boolean started = false;
	// while (retryCount > 0) {
	// try {
	// final List<String> statusOutput = TerminalCommunication
	// .executeCommand("nova console-log --length 10 " + instanceId);
	//
	// for (final String outputline : statusOutput) {
	// final String line = outputline.toLowerCase();
	// if (line.contains("finished at ")) {
	// LOG.info(line);
	// started = true;
	// break;
	// }
	// }
	// } catch (final Exception e) {
	// final String errorString = e.getMessage().toLowerCase();
	// if (errorString.contains("error: instance") &&
	// errorString.contains("is not ready")) {
	// started = false;
	// LOG.info(e.getMessage(), e);
	// }
	// }
	//
	// if (started) {
	// break;
	// }
	// try {
	// Thread.sleep(sleepTimeInMilliseconds);
	// } catch (final InterruptedException e) {
	// }
	//
	// retryCount--;
	// }
	//
	// if (!started) {
	// throw new Exception("Instance could not be started");
	// }
	// }
	//
	// /**
	// * @param instanceId
	// * Instanceid to get ip from.
	// * @return Ip of Nodeinstance.
	// * @throws Exception
	// * If private IP address not available.
	// */
	// private String getPrivateIPFromInstance(final String instanceId) throws
	// Exception {
	// LOG.info("Getting private IP for instance " + instanceId);
	// final List<String> output =
	// TerminalCommunication.executeCommand("nova show " + instanceId);
	// final StringToMapParser parser = new OpenStackOutputParser();
	// parser.parseAndAddStringList(output);
	//
	// final String privateIP = parser.getMap().get("vmnet network");
	//
	// if ((privateIP == null) || privateIP.equals("")) {
	// throw new Exception("Private IP address not available.");
	// }
	//
	// return privateIP;
	// }
	//
	// /**
	// * Copy application to instance of Node.
	// *
	// * @param privateIP
	// * Ip of Node for application to be copied.
	// * @param scalingGroup
	// * Arraylist containing Node.
	// * @throws Exception
	// * Copying failed.
	// */
	// private void copyApplicationToInstance(final String privateIP, final
	// ScalingGroup scalingGroup)
	// throws Exception {
	// LOG.info("Copying application '" + scalingGroup.getApplicationFolder() +
	// "' to node "
	// + privateIP);
	//
	// final String copyApplicationCommand =
	// "scp -o stricthostkeychecking=no -i " + sshPrivateKey
	// + " -r " + scalingGroup.getApplicationFolder() + " " + sshUsername + "@"
	// + privateIP + ":/home/" + sshUsername + "/";
	//
	// TerminalCommunication.executeCommand(copyApplicationCommand);
	// }
	//
	// /**
	// * Start application on instance of Node.
	// *
	// * @param privateIP
	// * Ip of Node for application to be started.
	// * @param scalingGroup
	// * Arraylist containing Node.
	// * @throws Exception
	// * Starting the application failed.
	// */
	// private void startApplicationOnInstance(final String privateIP, final
	// ScalingGroup scalingGroup)
	// throws Exception {
	// LOG.info("Starting application script - " +
	// scalingGroup.getStartApplicationScript()
	// + " - on node " + privateIP);
	//
	// SSHCommunication.runScriptViaSSH(privateIP, sshUsername, sshPrivateKey,
	// scalingGroup.getStartApplicationScript());
	// }
	//
	// /**
	// * @param privateIP
	// * Ip of Node for application to be started.
	// * @param scalingGroup
	// * Arraylist containing Node.
	// */
	// private void waitForApplicationStart(final String privateIP, final
	// ScalingGroup scalingGroup) {
	// LOG.info("Waiting " +
	// scalingGroup.getWaitTimeForApplicationStartInMillis()
	// + " milliseconds for application to start...");
	// try {
	// Thread.sleep(scalingGroup.getWaitTimeForApplicationStartInMillis());
	// } catch (final InterruptedException e) {
	// }
	// }
	//
	// /**
	// * @param privateIP
	// * Ip of Node for Application.
	// * @throws Exception
	// * Copying System Monitoring failed.
	// */
	// private void copySystemMonitoringToInstance(final String privateIP)
	// throws Exception {
	// LOG.info("Copying system monitoring '" + systemMonitoringFolder +
	// "' to node " + privateIP);
	//
	// final String copySystemMonitoringCommand =
	// "scp -o stricthostkeychecking=no -i "
	// + sshPrivateKey + " -r " + systemMonitoringFolder + " " + sshUsername +
	// "@"
	// + privateIP + ":/home/" + sshUsername + "/";
	//
	// TerminalCommunication.executeCommand(copySystemMonitoringCommand);
	// }
	//
	// private void startSystemMonitoringOnInstance(final String privateIP)
	// throws Exception {
	// LOG.info("Starting system monitoring script - " +
	// startSystemMonitoringScript
	// + " - on node " + privateIP);
	//
	// SSHCommunication.runScriptViaSSH(privateIP, sshUsername, sshPrivateKey,
	// startSystemMonitoringScript);
	// }
	//
	// private void shutDownNodeByHostname(final String hostName) {
	// try {
	// TerminalCommunication.executeCommand("nova delete " + hostName);
	// } catch (final Exception e) {
	// LOG.error(e.getMessage(), e);
	// }
	// LOG.info("Shut down node: " + hostName);
	// }
	//
	// @Override
	// public void shutdownNode(final Node node) {
	// LOG.info("Deleting node: " + node.getHostname());
	//
	// shutDownNodeByHostname(node.getHostname());
	//
	// node.getParent().removeNode(node);
	// }
}
