package explorviz.plugin_server.capacitymanagement.execution;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.capacitymanagement.CapManExecutionStates;
import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.CapManRealityMapper;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

/**
 * Action which starts applications on their parent node. This action is used as
 * compensate-action of {@link ApplicationTerminateAction}
 *
 */
public class ApplicationStartAction extends ExecutionAction {

	private final Application newApp;
	private final Node parent;
	private final String ipParent;
	private final String name;
	private final String scalingGroupName;
	private String pid;

	public ApplicationStartAction(String name, Node parent, String scalingGroupName) {
		newApp = new Application();
		this.name = name;
		newApp.setName(name);
		this.parent = parent;
		ipParent = parent.getIpAddress();
		newApp.setParent(parent);
		this.scalingGroupName = scalingGroupName;
		newApp.setScalinggroupName(scalingGroupName);
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
		getActionObject().putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
				CapManExecutionStates.STARTING);
		ScalingGroup scalinggroup = repository.getScalingGroupByName(scalingGroupName);
		pid = controller.startApplication(newApp, scalinggroup);

		if (pid != null) {

			scalinggroup.addApplication(newApp);
			return true;
		}
		return false;
	}

	@Override
	protected void afterAction() {
		newApp.setPid(pid);
		CapManRealityMapper.addApplicationtoNode(ipParent, newApp);
	}

	@Override
	protected void finallyDo() {
		unlockingNodeForApplications(parent);

	}

	@Override
	protected String getLoggingDescription() {
		return "starting application " + name + " on node " + parent.getName();

	}

	@Override
	protected ExecutionAction getCompensateAction() {
		// just thought as compensateAction for terminating Apps and inside of
		// ReplicateNode, so compensate would never be called here
		return null;
	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository) {
		// nothing to do because if application is not started, no mapping will
		// be done yet
		// Also, if application start did not finish successfully, we did not
		// get a pid, so we cannot "kill" the application.

	}

}
