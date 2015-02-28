package explorviz.plugin_server.capacitymanagement.execution;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.CapManRealityMapper;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.*;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeReplicateAction extends ExecutionAction {

	private final Node originalNode;
	private Node newNode;
	private final NodeGroup parent;
	private String ipAddress;

	public NodeReplicateAction(final Node originalNode) {
		this.originalNode = originalNode;
		parent = originalNode.getParent();
		ipAddress = originalNode.getIpAddress();
	}

	@Override
	protected GenericModelElement getActionObject() {

		return originalNode;
	}

	@Override
	protected SyncObject synchronizeOn() {

		return originalNode;
	}

	@Override
	protected void beforeAction() {
		// nothing happens
	}

	@Override
	protected boolean concreteAction(final ICloudController controller,
			ScalingGroupRepository repository) throws Exception {

		newNode = controller.replicateNode(parent, originalNode);

		if (newNode == null) {
			state = ExecutionActionState.ABORTED;
			return false;
		} else {
			String newIpAddress = newNode.getIpAddress();
			CapManRealityMapper.addNode(newIpAddress);
			for (Application app : CapManRealityMapper.getApplicationsFromNode(ipAddress)) {
				String scalinggroupName = app.getScalinggroupName();
				ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);

				String pid = controller.startApplication(app, scalinggroup);
				if (!pid.equals("null")) {
					Application new_app = new Application();
					new_app.copyAttributs(app);
					new_app.setLastUsage(0);
					new_app.setParent(newNode);
					new_app.setScalinggroupName(scalinggroupName);
					scalinggroup.addApplication(new_app);
					CapManRealityMapper.addApplicationtoNode(newIpAddress, new_app);
				} else {
					return false;
				}

			}
			return true;
		}
	}

	@Override
	protected void afterAction() {
		parent.addNode(newNode);
		newNode.setCpuUtilization(0);
		newNode.setParent(parent);
	}

	@Override
	protected void finallyDo() {
		// nothing happens
	}

	@Override
	protected String getLoggingDescription() {

		return "replicating node: " + originalNode.getName() + "with IP: "
				+ originalNode.getIpAddress();
	}

	@Override
	protected ExecutionAction getCompensateAction() {
		return new NodeTerminateAction(newNode);
	}

	@Override
	protected boolean checkBeforeAction(ICloudController controller) {
		return (ExecutionOrganizer.maxRunningNodesLimit < controller.retrieveRunningNodeCount());

	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository)
			throws Exception {
		String newIpAddress = newNode.getIpAddress();
		if (controller.instanceExisting(newIpAddress)) {
			for (Application app : CapManRealityMapper.getApplicationsFromNode(newIpAddress)) {
				String scalinggroupName = app.getScalinggroupName();
				ScalingGroup scalinggroup = repository.getScalingGroupByName(scalinggroupName);
				controller.terminateApplication(app, scalinggroup);
				scalinggroup.removeApplication(app);
				CapManRealityMapper.removeApplicationFromNode(newIpAddress, app.getName());
			}

			if (controller.terminateNode(newNode)) {
				CapManRealityMapper.removeNode(newIpAddress);
			}
		}
	}
}
