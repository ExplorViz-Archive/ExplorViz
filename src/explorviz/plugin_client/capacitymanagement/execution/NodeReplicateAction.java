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
			parent.addNode(newNode);
			newNode.setCpuUtilization(0);
			newNode.setParent(parent);

			for (Application app : originalNode.getApplications()) {
				ScalingGroup scalinggroup = app.getScalinggroup();
				String pid = controller.startApplicationOnInstance(newNode.getIpAddress(),
						scalinggroup, app.getName());
				if (!pid.equals("null")) {
					Application new_app = new Application();
					new_app.setName(app.getName());
					new_app.setCommunications(app.getCommunications());
					new_app.setComponents(app.getComponents());
					new_app.setIncomingCommunications(app.getIncomingCommunications());
					new_app.setOutgoingCommunications(app.getOutgoingCommunications());
					new_app.setLastUsage(0);
					new_app.setParent(newNode);
					new_app.setScalinggroup(scalinggroup);
					scalinggroup.addApplication(new_app);
				} else {
					return false;
				}

			}
			return true;
		}
	}

	@Override
	protected void afterAction() {

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
