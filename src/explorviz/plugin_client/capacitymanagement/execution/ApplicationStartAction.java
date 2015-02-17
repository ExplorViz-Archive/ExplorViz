package explorviz.plugin_client.capacitymanagement.execution;

import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.shared.model.*;
import explorviz.shared.model.helper.GenericModelElement;

public class ApplicationStartAction extends ExecutionAction {

	private final Application newApp;
	private final Node parent;
	private final String appName;
	private final ScalingGroup scalinggroup;
	private String pid;

	public ApplicationStartAction(Application newApp) {
		this.newApp = newApp;
		appName = newApp.getName();
		parent = newApp.getParent();
		scalinggroup = newApp.getScalinggroup();

	}

	@Override
	protected GenericModelElement getActionObject() {

		return newApp;
	}

	@Override
	protected SyncObject synchronizeOn() {
		// TODO: worauf hier synchronisieren?
		return null;
	}

	@Override
	protected void beforeAction() {
		lockingNodeForApplications(parent);
	}

	@Override
	protected boolean concreteAction(ICloudController controller) throws Exception {
		pid = controller.startApplicationOnInstance(parent.getIpAddress(), scalinggroup, appName);
		return !pid.equals("null");
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

}
