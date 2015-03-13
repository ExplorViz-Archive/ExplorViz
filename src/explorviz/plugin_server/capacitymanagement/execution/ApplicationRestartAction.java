package explorviz.plugin_server.capacitymanagement.execution;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.capacitymanagement.CapManExecutionStates;
import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.CapManRealityMapper;
import explorviz.plugin_server.capacitymanagement.MappingException;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

/**
 *
 * Action which restarts the given application in the cloud.
 *
 */
public class ApplicationRestartAction extends ExecutionAction {

	private final Application application;
	private final Node parent;
	private final String name;
	private final String ipParent;
	private String newPid;
	private String oldPid;

	public ApplicationRestartAction(final Application app) throws MappingException {
		parent = app.getParent();
		ipParent = parent.getIpAddress();
		name = app.getName();
		application = CapManRealityMapper.getApplication(ipParent, name);
		if (application == null) {
			throw new MappingException("Application " + name + " on " + ipParent
					+ " could not be mapped.");
		}
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
		getActionObject().putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
				CapManExecutionStates.RESTARTING);
		String scalinggroupName = application.getScalinggroupName();
		ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
		oldPid = application.getPid();
		newPid = controller.restartApplication(application, scalinggroup);

		if (newPid != null) {
			application.setPid(newPid);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void afterAction() {

	}

	@Override
	protected void finallyDo() {
		unlockingNodeForApplications(parent);
	}

	@Override
	protected String getLoggingDescription() {
		return "restarting application " + name + " on node " + parent.getName();
	}

	@Override
	protected ExecutionAction getCompensateAction() {
		return null;
	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository) {
		if (!controller.checkApplicationIsRunning(ipParent, oldPid, name)) {
			String scalinggroupName = application.getScalinggroupName();
			ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
			scalinggroup.removeApplication(application);
			CapManRealityMapper.removeApplicationFromNode(ipParent, name);
		}
	}
}
