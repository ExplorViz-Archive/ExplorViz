package explorviz.plugin_server.capacitymanagement.execution;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public class ApplicationRestartAction extends ExecutionAction {

	Application application;
	Node parent;

	public ApplicationRestartAction(final Application app) {
		application = app;
		parent = application.getParent();
	}

	@Override
	protected GenericModelElement getActionObject() {
		return application;
	}

	@Override
	protected SyncObject synchronizeOn() {
		return application;
	}

	@Override
	protected void beforeAction() {
		lockingNodeForApplications(parent);
	}

	@Override
	protected boolean concreteAction(final ICloudController controller,
			ScalingGroupRepository repository) throws Exception {
		String scalinggroupName = application.getScalinggroupName();
		ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
		boolean success = controller.restartApplication(application, scalinggroup);
		if (!success) {

			scalinggroup.removeApplication(application);
		}
		return success;
	}

	@Override
	protected void afterAction() {
		// nothing special happens
	}

	@Override
	protected void finallyDo() {
		unlockingNodeForApplications(parent);
	}

	@Override
	protected String getLoggingDescription() {
		return "restarting application " + application.getName() + " on node " + parent.getName();
	}

	@Override
	protected ExecutionAction getCompensateAction() {
		return null;
	}

}
