package explorviz.plugin_server.capacitymanagement.execution;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.CapManRealityMapper;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public class ApplicationTerminateAction extends ExecutionAction {

	private final Application app;
	private final String name;
	private final Node parent;
	private final String ipParent;

	public ApplicationTerminateAction(final Application app) {
		parent = app.getParent();
		ipParent = parent.getIpAddress();
		name = app.getName();
		this.app = CapManRealityMapper.getApplication(ipParent, name);

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
		CapManRealityMapper.removeApplicationFromNode(ipParent, name);
	}

	@Override
	protected void finallyDo() {
		unlockingNodeForApplications(parent);
	}

	@Override
	protected String getLoggingDescription() {
		return "terminating application " + name + " on node " + parent.getName();
	}

	@Override
	protected ExecutionAction getCompensateAction() {

		// TODO: jkr/jek waere lastUsage hier auch wichtig?
		ApplicationStartAction compensate = new ApplicationStartAction(name, parent,
				app.getScalinggroupName());
		return compensate;
	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository) {

		if (controller.checkApplicationIsRunning(ipParent, app.getPid(), name)) {

			String scalinggroupName = app.getScalinggroupName();
			ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
			scalinggroup.addApplication(app);
			CapManRealityMapper.setApplication(ipParent, app);
		}

	}

}
