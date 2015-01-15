package explorviz.plugin.capacitymanagement.execution;

import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeReplicateAction extends ExecutionAction {

	private final Node originalNode;
	private Node newNode;
	private final NodeGroup parent;

	public NodeReplicateAction(final Node originalNode) {
		this.originalNode = originalNode;
		parent = originalNode.getParent();
	}

	// @Override
	// public void execute(final ICloudController controller) {
	//
	// if (LoadBalancersFacade.getNodeCount() >=
	// ExecutionOrganizer.maxRunningNodesLimit) {
	// state = ExecutionActionState.REJECTED;
	// }
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// final NodeGroup parent = originalNode.getParent();
	// synchronized (parent) {
	// while (parent.isLockedUntilExecutionActionFinished()) {
	// try {
	// parent.wait();
	// } catch (final InterruptedException e) {
	//
	// }
	// }
	// parent.setLockedUntilExecutionActionFinished(true);
	// Node newNode = null;
	// try {
	// newNode = controller.cloneNode(parent, originalNode);
	// } catch (final Exception e) {
	// LOGGER.error("Error while cloning node:");
	// LOGGER.error(e.getMessage(), e);
	// } finally {
	// if (newNode == null) {
	// state = ExecutionActionState.ABORTED;
	// } else {
	// parent.addNode(newNode);
	// LoadBalancersFacade.addNode(newNode.getIpAddress(), parent.getName());
	// state = ExecutionActionState.SUCC_FINISHED;
	// originalNode.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
	// CapManExecutionStates.NONE);
	// LOGGER.info("Cloned node:" + originalNode.getName());
	// }
	// parent.setLockedUntilExecutionActionFinished(false);
	// parent.notify();
	// }
	// }
	//
	// }
	//
	// }).start();
	// }

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
		newNode = null;
	}

	@Override
	protected boolean concreteAction(final ICloudController controller) {
		newNode = controller.cloneNode(parent, originalNode);
		return (newNode != null);
	}

	@Override
	protected void afterAction() {
		parent.addNode(newNode);
		LoadBalancersFacade.addNode(newNode.getIpAddress(), parent.getName());
	}

	@Override
	protected String getLoggingDescription() {

		return "Replicate node:" + originalNode.getName() + "with IP:"
				+ originalNode.getIpAddress();
	}

	@Override
	protected void finallyDo() {

	}

}
