package explorviz.plugin_server.capacitymanagement.execution;

import java.util.List;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeRestartAction extends ExecutionAction {

	private final Node node;

	public NodeRestartAction(final Node node) {
		this.node = node;
	}

	@Override
	protected GenericModelElement getActionObject() {
		return node;
	}

	@Override
	protected SyncObject synchronizeOn() {
		return node;
	}

	@Override
	protected void beforeAction() {

	}

	@Override
	protected boolean concreteAction(final ICloudController controller,
			ScalingGroupRepository repository) throws Exception {
		List<Application> apps = node.getApplications();
		for (Application app : apps) {
			String scalinggroupName = app.getScalinggroupName();
			ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
			scalinggroup.removeApplication(app);
			controller.terminateApplication(app, scalinggroup); // success is
			// not important
			// here
		}
		boolean success = controller.restartNode(node);
		if (success) {
			String pid;
			for (Application app : apps) {
				String scalinggroupName = app.getScalinggroupName();
				ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
				pid = controller.startApplication(app, scalinggroup);
				if (pid == "null") {
					return false;
				} else {
					app.setPid(pid);

					scalinggroup.addApplication(app);
				}

			}
		}
		return success;
	}

	@Override
	protected void afterAction() {

	}

	@Override
	protected void finallyDo() {
		// nothing happens
	}

	@Override
	protected String getLoggingDescription() {
		return "restarting node " + node.getName() + " with IP: " + node.getIpAddress();
	}

	@Override
	protected ExecutionAction getCompensateAction() {
		return null;
	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository)
			throws Exception {
		if (!controller.instanceExisting(node.getHostname())) {
			controller.startNode(node.getParent(), node);
			for (Application app : node.getApplications()) {
				String scalinggroupName = app.getScalinggroupName();
				ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);

				String pid = controller.startApplication(app, scalinggroup);
				if (!pid.equals("null")) {
					scalinggroup.addApplication(app);
				}
			}

		}
	}

}
