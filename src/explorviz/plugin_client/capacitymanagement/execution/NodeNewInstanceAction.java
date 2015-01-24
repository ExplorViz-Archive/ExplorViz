package explorviz.plugin_client.capacitymanagement.execution;

import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeNewInstanceAction extends ExecutionAction {

	private final NodeGroup parent;
	private Node newNode;

	public NodeNewInstanceAction(final NodeGroup nodegroup) {
		parent = nodegroup;
	}

	@Override
	public void execute(final ICloudController controller, final ThreadGroup group) {

		if (LoadBalancersFacade.getNodeCount() >= ExecutionOrganizer.maxRunningNodesLimit) {
			state = ExecutionActionState.REJECTED;
			return;
		}
		super.execute(controller, group);
	}

	@Override
	protected GenericModelElement getActionObject() {
		return newNode;
	}

	@Override
	protected SyncObject synchronizeOn() {
		// TODO: jek/jkr: synchronize only on list? on dummy object?
		return parent;
	}

	@Override
	protected void beforeAction() {
		// nothing happens
	}

	@Override
	protected boolean concreteAction(final ICloudController controller) throws Exception {
		newNode = controller.startNode(parent);
		if (newNode == null) {
			state = ExecutionActionState.ABORTED;
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected void afterAction() {
		parent.addNode(newNode);
		LoadBalancersFacade.addNode(newNode.getIpAddress(), parent.getName());
	}

	@Override
	protected void finallyDo() {
		// nothing happens
	}

	@Override
	protected String getLoggingDescription() {
		return "instantiating new node in nodegroup " + parent.getName();
	}

	@Override
	protected ExecutionAction getCompensateAction() {

		return new NodeTerminateAction(newNode);
	}

	// TODO: jek/jkr: nur bei dynamischen NodeGroups notwendig?
	// kann weg?
	// startedNode.setLoadBalancerRemoveAfterStart(
	// originalNode.getPrivateIP(),
	// scalingGroup.getLoadReceiver());

	// scalingGroup.setLoadReceiver(newScalingGroupName);
	// // TODO remove master from this scalingGroup
	// }

	// TODO: jek/jkr: soll von CloudController gesteuert werden
	// Thread.sleep(ExecutionOrganizer.waitTimeBeforeNewBootInMillis);
}
