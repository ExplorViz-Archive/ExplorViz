package explorviz.plugin_client.capacitymanagement.execution;

import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
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
	protected boolean concreteAction(final ICloudController controller) throws Exception {

		return controller.terminateApplication(app);
	}

	@Override
	protected void afterAction() {
		parent.removeApplication(app.getId());
		app.getScalinggroup().removeApplication(app);
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
		ScalingGroup scalinggroup = app.getScalinggroup();
		newApp.setScalinggroup(scalinggroup);
		scalinggroup.addApplication(newApp);

		ApplicationStartAction compensate = new ApplicationStartAction(newApp);
		return compensate;
	}

}
