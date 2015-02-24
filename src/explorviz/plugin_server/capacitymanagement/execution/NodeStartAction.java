package explorviz.plugin_server.capacitymanagement.execution;

import java.util.List;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.*;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeStartAction extends ExecutionAction {

	private NodeGroup parent;
	private Node newNode;
	private int failing_index = 0;

	public NodeStartAction(String hostname, String flavor, String image, List<Application> apps,
			NodeGroup parent) {

		newNode = new Node();
		newNode.setHostname(hostname);
		newNode.setImage(image);
		newNode.setFlavor(flavor);

		this.parent = parent;

		for (Application app : apps) {
			Application newApp = new Application();
			newApp.setParent(newNode);
			newApp.copyAttributs(app);
			newApp.setLastUsage(0);
			newNode.addApplication(newApp);
		}
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
		String ipAdress = controller.startNode(parent, newNode);
		if (ipAdress == "null") {
			state = ExecutionActionState.ABORTED;
			return false;
		} else {
			newNode.setIpAddress(ipAdress);

			newNode.setId(controller.retrieveIdFromNode(newNode));

			for (Application app : newNode.getApplications()) {
				String scalinggroupName = app.getScalinggroupName();
				ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
				String pid;
				controller.copyApplicationToInstance(ipAdress, app, scalinggroup);
				pid = controller.startApplication(app, scalinggroup);
				if (pid != "null") {
					app.setPid(pid);
					scalinggroup.addApplication(app);
				} else {
					failing_index = newNode.getApplications().indexOf(app);
					return false;
				}

			}
			return true;
		}
	}

	@Override
	protected void afterAction() {
		newNode.setParent(parent);

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
	protected void compensate(ICloudController controller, ScalingGroupRepository repository)
			throws Exception {
		if (controller.instanceExisting(newNode.getHostname())) {
			int i = 0;
			while (i < failing_index) {
				Application app = newNode.getApplications().get(i);
				String scalinggroupName = app.getScalinggroupName();
				ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
				controller.terminateApplication(app, scalinggroup);
				scalinggroup.removeApplication(app);
				i++;
			}
			controller.terminateNode(newNode);
		}

	}
}
