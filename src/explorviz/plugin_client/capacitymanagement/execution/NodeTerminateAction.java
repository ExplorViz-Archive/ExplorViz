package explorviz.plugin_client.capacitymanagement.execution;

import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeTerminateAction extends ExecutionAction {

	private final Node node;

	public NodeTerminateAction(final Node node) {
		this.node = node;
	}

	// TODO: jek/jkr: brauchen wir das?
	// if (LoadBalancersFacade.getNodeCount() > 1) {

	@Override
	protected GenericModelElement getActionObject() {
		// TODO jek/jkr: sinnvoll? Problem? da durchgef�hrt wenn success,
		// --> node sollte nicht mehr existieren (aber als Objekt vllt schon?)
		return node;
	}

	@Override
	protected SyncObject synchronizeOn() {
		return node;
	}

	@Override
	protected void beforeAction() {
		// nothing happens
	}

	@Override
	protected boolean concreteAction(final ICloudController controller) throws Exception {
		return controller.shutdownNode(node);
	}

	@Override
	protected void afterAction() {
		LoadBalancersFacade.removeNode(node.getIpAddress(), node.getParent().getName());
		node.getParent().removeNode(node.getIpAddress());
	}

	@Override
	protected void finallyDo() {
		// nothing happens
	}

	@Override
	protected String getLoggingDescription() {
		return "terminating node: " + node.getName() + " with IP: " + node.getIpAddress();
	}
}
