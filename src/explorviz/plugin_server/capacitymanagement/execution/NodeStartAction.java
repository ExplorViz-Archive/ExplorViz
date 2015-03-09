package explorviz.plugin_server.capacitymanagement.execution;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.CapManRealityMapper;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.*;
import explorviz.shared.model.helper.GenericModelElement;

/**
 *
 * Action which starts a node. This action is used for the initial setup during
 * the start of CapMan. It is also the CompensationAction for
 * {@link NodeTerminateAction}
 *
 */
public class NodeStartAction extends ExecutionAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeStartAction.class);

	private NodeGroup parent;
	private Node newNode;

	public NodeStartAction(String hostname, String flavor, String image, List<Application> apps,
			NodeGroup parent) {

		newNode = new Node();
		newNode.setHostname(hostname);
		newNode.setImage(image);
		newNode.setFlavor(flavor);

		this.parent = parent;
		newNode.setApplications(apps);

		for (Application app : apps) {
			app.setParent(newNode);
		}

		// app in the list apps must NOT be created by worker
		// for (Application app : apps) {
		// Application newApp = new Application();
		// newApp.setParent(newNode);
		// newApp.copyAttributs(app);
		// newApp.setLastUsage(0);
		// newNode.addApplication(newApp);
		// }
	}

	@Override
	protected GenericModelElement getActionObject() {

		return newNode;
	}

	@Override
	protected SyncObject synchronizeOn() {
		return newNode;
	}

	@Override
	protected void beforeAction() {

	}

	@Override
	protected boolean concreteAction(ICloudController controller, ScalingGroupRepository repository)
			throws Exception {
		if (controller.instanceExisting(newNode.getHostname())) {
			throw new Exception("Node with hostname " + newNode.getHostname()
					+ " already exists in the cloud!");
		}
		String ipAdress = controller.startNode(parent, newNode);
		if (ipAdress == null) {
			state = ExecutionActionState.ABORTED;
			return false;
		} else {
			newNode.setIpAddress(ipAdress);

			newNode.setId(controller.retrieveIdFromNode(newNode));
			boolean success = true;
			for (Application app : newNode.getApplications()) {
				String scalinggroupName = app.getScalinggroupName();
				ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
				controller.copyApplicationToInstance(ipAdress, app, scalinggroup);

				waitForDependendNodes(controller, app);

				String pid = controller.startApplication(app, scalinggroup);
				if (pid != null) {
					app.setPid(pid);
					scalinggroup.addApplication(app);
				} else {
					success = false;
				}

			}
			return success;
		}
	}

	@Override
	protected void afterAction() {
		newNode.setParent(parent);
		String ipAddress = newNode.getIpAddress();
		CapManRealityMapper.addNode(ipAddress);
		for (Application app : newNode.getApplications()) {
			CapManRealityMapper.addApplicationtoNode(ipAddress, app);
		}
	}

	@Override
	protected void finallyDo() {

	}

	@Override
	protected String getLoggingDescription() {
		return "starting node: " + newNode.getHostname();
	}

	@Override
	protected ExecutionAction getCompensateAction() {

		return new NodeTerminateAction(newNode);
	}

	@Override
	protected boolean checkBeforeAction(ICloudController controller) {
		return (ExecutionOrganizer.maxRunningNodesLimit > controller.retrieveRunningNodeCount());

	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository) {
		try {
			controller.terminateNode(newNode);
		} catch (Exception e) {
			LOGGER.error("Could not terminate node " + newNode.getHostname() + " for compensation");
			e.printStackTrace();
		}
	}
}
