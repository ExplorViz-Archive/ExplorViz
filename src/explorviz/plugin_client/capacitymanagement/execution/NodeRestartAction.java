package explorviz.plugin_client.capacitymanagement.execution;

import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
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

	}

	@Override
	protected boolean concreteAction(final ICloudController controller) throws Exception {
		return controller.restartNode(node);
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
		return "restarting node " + node.getName() + " with IP: " + node.getIpAddress();
	}

	@Override
	protected ExecutionAction getCompensateAction() {
		return null;
	}

}
