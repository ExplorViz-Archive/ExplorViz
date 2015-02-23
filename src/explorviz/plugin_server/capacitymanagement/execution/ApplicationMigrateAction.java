package explorviz.plugin_server.capacitymanagement.execution;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

//TODO: review
public class ApplicationMigrateAction extends ExecutionAction {

	Application application;
	Node parent;
	Node destination;

	public ApplicationMigrateAction(final Application app, final Node destination) {
		application = app;
		parent = application.getParent();
		this.destination = destination;
	}

	@Override
	protected GenericModelElement getActionObject() {
		return application;
	}

	@Override
	protected SyncObject synchronizeOn() {
		// parent? destination? nodegroup?
		return application;
	}

	@Override
	protected void beforeAction() {
		lockingNodeForApplications(parent);
	}

	@Override
	protected boolean concreteAction(final ICloudController controller) throws Exception {
		return controller.migrateApplication(application, destination);
	}

	@Override
	protected void afterAction() {
		parent.removeApplication(application.getId());
		destination.addApplication(application);
	}

	@Override
	protected void finallyDo() {
		unlockingNodeForApplications(parent);
	}

	@Override
	protected String getLoggingDescription() {
		return "migrating application " + application.getName() + " to node "
				+ destination.getName() + "with IP: " + destination.getIpAddress();
	}

	@Override
	protected ExecutionAction getCompensateAction() {

		return new ApplicationMigrateAction(application, parent);
	}
}
