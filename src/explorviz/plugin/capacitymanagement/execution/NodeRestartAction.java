package explorviz.plugin.capacitymanagement.execution;

import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeRestartAction extends ExecutionAction {

	private final Node node;

	public NodeRestartAction(final Node node) {
		this.node = node;
	}

	@Override
	protected GenericModelElement getActionObject() {
		return node;
	}

	@Override
	protected SyncObject synchronizeOn() {
		return node;
	}

	@Override
	protected void beforeAction() {
		LoadBalancersFacade.removeNode(node.getIpAddress(), node.getParent().getName());
	}

	@Override
	protected boolean concreteAction(final ICloudController controller) {
		return controller.restartNode(node);
	}

	@Override
	protected void afterAction() {
		LoadBalancersFacade.addNode(node.getIpAddress(), node.getParent().getName());
	}

	@Override
	protected void finallyDo() {
		// nothing happens
	}

	@Override
	protected String getLoggingDescription() {
		return "restarting node " + node.getName() + " with IP: " + node.getIpAddress();
	}

}
