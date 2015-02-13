package explorviz.plugin_client.capacitymanagement.execution;

import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.shared.model.*;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeReplicateAction extends ExecutionAction {

	private final Node originalNode;
	private Node newNode;
	private final NodeGroup parent;

	public NodeReplicateAction(final Node originalNode) {
		this.originalNode = originalNode;
		parent = originalNode.getParent();
	}

	@Override
	public void execute(final ICloudController controller, final ThreadGroup group) {
		// if (LoadBalancersFacade.getNodeCount() >=
		// ExecutionOrganizer.maxRunningNodesLimit) {
		// state = ExecutionActionState.REJECTED;
		// return;
		// }
		super.execute(controller, group);
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
	protected boolean concreteAction(final ICloudController controller) throws Exception {

		newNode = controller.replicateNode(parent, originalNode);

		if (newNode == null) {
			state = ExecutionActionState.ABORTED;
			return false;
		} else {
			return true;
		}

	}

	@Override
	protected void afterAction() {
		// replicate actions for landscapemodel
		parent.addNode(newNode);
		newNode.setCpuUtilization(0);
		newNode.setParent(parent);

		for (Application app : originalNode.getApplications()) {
			Application new_app = new Application();
			new_app.setCommunications(app.getCommunications());
			new_app.setComponents(app.getComponents());
			new_app.setIncomingCommunications(app.getIncomingCommunications());
			new_app.setLastUsage(0);
			new_app.setOutgoingCommunications(app.getOutgoingCommunications());
			ScalingGroup scalinggroup = app.getScalinggroup();
			new_app.setScalinggroup(scalinggroup);
			scalinggroup.addApplication(new_app); // internally maps app to
			// corresponding
			// loadbalancer
		}
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

}
