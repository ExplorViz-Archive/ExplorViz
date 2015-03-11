package explorviz.plugin_server.capacitymanagement.execution;

import java.util.List;

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
 * Action which terminates a node and the running applications.
 *
 */
public class NodeTerminateAction extends ExecutionAction {

	private final Node node;
	private final String ipAddress;
	private List<Application> apps;

	public NodeTerminateAction(final Node node) {
		this.node = node;
		ipAddress = node.getIpAddress();
		apps = CapManRealityMapper.getApplicationsFromNode(ipAddress);
	}

	@Override
	protected GenericModelElement getActionObject() {
		// sollte kein Problem sein, das Object an sich wird nicht geloescht
		return node;
	}

	@Override
	protected SyncObject synchronizeOn() {
		// sollte kein Problem sein, das Object an sich wird nicht geloescht
		return node;
	}

	@Override
	protected void beforeAction() {
		// nothing happens
	}

	@Override
	protected boolean concreteAction(final ICloudController controller,
			ScalingGroupRepository repository) throws Exception {
		node.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE, CapManExecutionStates.TERMINATING);
		for (Application app : apps) {
			String scalinggroupName = app.getScalinggroupName();
			ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
			try {
				controller.terminateApplication(app, scalinggroup);
			} catch (Exception e) {
				// it is not that important that all applications were
				// terminated properly
			}
			scalinggroup.removeApplication(app);
		}
		boolean success = controller.terminateNode(node);
		return success;
	}

	@Override
	protected void afterAction() {
		node.getParent().removeNode(ipAddress);
		CapManRealityMapper.removeNode(ipAddress);
	}

	@Override
	protected void finallyDo() {
		// nothing happens
	}

	@Override
	protected String getLoggingDescription() {
		return "terminating node: " + node.getName() + " with IP: " + node.getIpAddress();
	}

	@Override
	protected ExecutionAction getCompensateAction() {
		return new NodeStartAction(node.getHostname(), node.getFlavor(), node.getImage(), apps,
				node.getParent());
	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository) {
		// TODO: if node running: terminate?
	}

}
