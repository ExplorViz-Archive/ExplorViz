package explorviz.plugin_server.capacitymanagement.cloud_control;

import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.shared.model.*;

/**
 * Interface for controlling a cloud e.g. starting or terminating nodes or
 * applications. <br>
 * Partly used from capacity-manager-project.
 */
public interface ICloudController {

	/**
	 * Start new instance in Cloud.
	 *
	 * @param nodegroup
	 *            parent of new node
	 * @param nodeToStart
	 *            Model of new node, hostname, image and flavor have to be set
	 *
	 * @return ipAdress of started Node
	 * @throws Exception
	 */
	String startNode(NodeGroup nodegroup, Node node) throws Exception;

	/**
	 * Starts a copy of the given Node in the Cloud
	 *
	 * @param nodegroup
	 *            parent of original and new Node
	 * @param originalNode
	 *            Node to copy
	 * @return newNode
	 */
	Node replicateNode(NodeGroup nodegroup, Node originalNode) throws Exception;

	/**
	 * Terminate an Instance running in the Cloud.
	 *
	 * @param node
	 *            Node to terminate.
	 * @return success of action
	 * @throws Exception
	 */
	boolean terminateNode(Node node) throws Exception;

	/**
	 * Restart Node in Cloud.
	 *
	 * @param node
	 *            node with to restart
	 * @return success of action
	 */
	boolean restartNode(Node node) throws Exception;

	/**
	 * Restarts an Application in the Cloud.
	 *
	 * @param application
	 *            Model of the Application to restart.
	 * @return Success of the Action.
	 * @throws Exception
	 */
	String restartApplication(Application application, ScalingGroup scalingGroup) throws Exception;

	/**
	 * Terminate an Application in the Cloud.
	 *
	 * @param application
	 *            Model of Application to terminate.
	 * @return Success of the Action.
	 * @throws Exception
	 */
	boolean terminateApplication(Application application, ScalingGroup scalingGroup)
			throws Exception;

	/**
	 * Migration of an Application from one Instance to another.
	 *
	 * @param application
	 *            Model of Application to Migrate.
	 * @param node
	 *            Destination node.
	 * @return Success of the Action
	 * @throws Exception
	 */
	boolean migrateApplication(Application application, Node node, ScalingGroup scalingGroup)
			throws Exception;

	/**
	 * Starts an Application on a given Instance in the Cloud.
	 *
	 * @param privateIP
	 *            Private IP of instance on which to start the application.
	 * @param scalingGroup
	 * @param name
	 *            Name of application.
	 * @return PID of the application.
	 * @throws Exception
	 */
	String startApplication(Application app, ScalingGroup scalingGroup) throws Exception;

	/**
	 * Gets number of active instances in the cloud.
	 *
	 * @return Number of active instances.
	 */
	int retrieveRunningNodeCount();

	/**
	 * Retrieves Id of node from the Cloud.
	 *
	 * @param node
	 * @return Id of node as String
	 * @throws Exception
	 */
	String retrieveIdFromNode(Node node) throws Exception;

	/**
	 * Copies executable files to a running instance in the cloud.
	 *
	 * @param privateIP
	 *            IPAdress of the Instance to which the application is to be
	 *            copied.
	 * @param app
	 *            Model of application to be copied.
	 * @param scalingGroup
	 *            ScalingGroup of Application, containing path where executable
	 *            is found locally.
	 * @throws Exception
	 */
	void copyApplicationToInstance(final String privateIP, final Application app,
			ScalingGroup scalingGroup) throws Exception;

	/**
	 * Checks if instance given by name and pid is running on instance given by
	 * IP.
	 *
	 * @param privateIP
	 *            IP-Adress of instance.
	 * @param pid
	 *            Process ID of application.
	 * @param name
	 *            Name of application.
	 * @return True if application is running.
	 */
	boolean checkApplicationIsRunning(final String privateIP, final String pid, final String name);

	/**
	 * Checks if instance exists in the cloud.
	 *
	 * @param ip
	 *            ip address of instance.
	 * @return True if instance exists.
	 */
	public boolean instanceExistingByIpAddress(final String name);

	/**
	 * Checks if instance exists in the cloud.
	 *
	 * @param name
	 *            Hostname of instance.
	 * @return True if instance exists.
	 */
	public boolean instanceExistingByHostname(String hostname);

	/**
	 * Returns privateIP retrieved from cloud.
	 *
	 * @param instanceId
	 *            Instanceid or hostname to get ip from.
	 * @return Ip of Nodeinstance.
	 * @throws Exception
	 *             If private IP address not available.
	 */
	String retrievePrivateIPFromInstance(final String instanceId) throws Exception;
}
