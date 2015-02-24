package explorviz.plugin_server.capacitymanagement.execution;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public class ApplicationStartAction extends ExecutionAction {

	private final Application newApp;
	private final Node parent;
	private final String appName;

	private String pid;

	public ApplicationStartAction(Application newApp) {
		this.newApp = newApp;
		appName = newApp.getName();
		parent = newApp.getParent();

	}

	@Override
	protected GenericModelElement getActionObject() {

		return newApp;
	}

	@Override
	protected SyncObject synchronizeOn() {
		return newApp;
	}

	@Override
	protected void beforeAction() {
		lockingNodeForApplications(parent);
	}

	@Override
	protected boolean concreteAction(ICloudController controller, ScalingGroupRepository repository)
			throws Exception {
		String scalinggroupName = newApp.getScalinggroupName();
		ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
		pid = controller.startApplication(newApp, scalinggroup);

		if (!pid.equals("null")) {

			scalinggroup.addApplication(newApp);
			return true;
		}
		return false;
	}

	@Override
	protected void afterAction() {
		newApp.setPid(pid);
	}

	@Override
	protected void finallyDo() {
		unlockingNodeForApplications(parent);

	}

	@Override
	protected String getLoggingDescription() {
		return "starting application " + appName + " on node " + parent.getName();

	}

	@Override
	protected ExecutionAction getCompensateAction() {
		// just thought as compensateAction for terminating Apps and inside of
		// ReplicateNode, so compensate would never be called here
		return null;
	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository)
			throws Exception {
		if (controller.checkApplicationIsRunning(newApp.getParent().getIpAddress(),
				newApp.getPid(), newApp.getPid())) {
			String scalinggroupName = newApp.getScalinggroupName();
			ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
			controller.terminateApplication(newApp, scalinggroup);
			scalinggroup.removeApplication(newApp);
		}
	}

}
