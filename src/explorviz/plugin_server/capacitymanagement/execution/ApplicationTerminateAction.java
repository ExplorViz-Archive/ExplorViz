package explorviz.plugin_server.capacitymanagement.execution;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public class ApplicationTerminateAction extends ExecutionAction {

	private final Application app;
	private final Node parent;

	public ApplicationTerminateAction(final Application app) {
		this.app = app;
		parent = app.getParent();
	}

	@Override
	protected GenericModelElement getActionObject() {

		return app;
	}

	@Override
	protected SyncObject synchronizeOn() {

		return app;
	}

	@Override
	protected void beforeAction() {
		lockingNodeForApplications(parent);
	}

	@Override
	protected boolean concreteAction(final ICloudController controller,
			ScalingGroupRepository repository) throws Exception {
		String scalinggroupName = app.getScalinggroupName();
		ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
		if (controller.terminateApplication(app, scalinggroup)) {

			scalinggroup.removeApplication(app);
			return true;
		}
		return false;
	}

	@Override
	protected void afterAction() {
		parent.removeApplication(app.getId());
	}

	@Override
	protected void finallyDo() {
		unlockingNodeForApplications(parent);
	}

	@Override
	protected String getLoggingDescription() {
		return "terminating application " + app.getName() + " on node " + parent.getName();
	}

	@Override
	protected ExecutionAction getCompensateAction() {
		Application newApp = new Application();
		newApp.setName(app.getName());
		newApp.setLastUsage(app.getLastUsage());
		newApp.setParent(parent);
		String scalinggroup = app.getScalinggroupName();
		newApp.setScalinggroupName(scalinggroup);

		ApplicationStartAction compensate = new ApplicationStartAction(newApp);
		return compensate;
	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository)
			throws Exception {
		if (!controller.checkApplicationIsRunning(app.getParent().getIpAddress(), app.getPid(),
				app.getPid())) {
			String scalinggroupName = app.getScalinggroupName();
			ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
			controller.startApplication(app, scalinggroup);
			scalinggroup.addApplication(app);
		}

	}

}
