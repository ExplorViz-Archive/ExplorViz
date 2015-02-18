package explorviz.plugin_server.capacitymanagement.cloud_control;

import explorviz.shared.model.*;

public interface ICloudController {

	/**
	 * Start new instance in Cloud.
	 *
	 * @param nodegroup
	 *            parent of new node
	 * @param nodeToStart
	 *            Model of new node, hostname, image and flavor have to be set
	 *
	 * @return
	 * @throws Exception
	 */
	Node startNode(NodeGroup nodegroup, Node node) throws Exception;

	/**
	 * Starts a copy of the given Node in the Cloud
	 *
	 * @param nodegroup
	 *            parent of original and new Node
	 * @param originalNode
	 *            Node to copy
	 * @return
	 */
	Node replicateNode(NodeGroup nodegroup, Node originalNode);

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
	boolean restartApplication(Application application) throws Exception;

	/**
	 * Terminate an Application in the Cloud.
	 *
	 * @param application
	 *            Model of Application to terminate.
	 * @return Success of the Action.
	 * @throws Exception
	 */
	boolean terminateApplication(Application application) throws Exception;

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
	boolean migrateApplication(Application application, Node node) throws Exception;

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
	String startApplicationOnInstance(final String privateIP, final ScalingGroup scalingGroup,
			final String name) throws Exception;

	/**
	 * Gets number of active instances in the cloud.
	 *
	 * @return Number of active instances.
	 */
	int retrieveRunningNodeCount();
}
