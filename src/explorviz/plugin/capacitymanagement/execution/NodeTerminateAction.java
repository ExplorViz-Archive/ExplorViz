package explorviz.plugin.capacitymanagement.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.attributes.IPluginKeys;
import explorviz.plugin.capacitymanagement.CapManExecutionStates;
import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;

public class NodeTerminateAction extends ExecutionAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeTerminateAction.class);

	private final Node node;

	public NodeTerminateAction(final Node node) {
		this.node = node;
	}

	@Override
	public void execute(final ICloudController controller) {
		final NodeGroup parent = node.getParent();
		if (LoadBalancersFacade.getNodeCount() > 1) {

			new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized (parent) {
						while (parent.isLockedUntilExecutionActionFinished()) {
							try {
								parent.wait();
							} catch (final InterruptedException e) {

							}
						}
						parent.setLockedUntilExecutionActionFinished(true);
						// final Node nodeFromRepository =
						// node.getScalingGroup().getNodeByHostname(
						// node.getHostname());
						boolean success = false;
						try {
							success = controller.shutdownNode(node);
						} catch (final Exception e) {
							LOGGER.error("Error while terminating node" + node.getName() + ":");
							LOGGER.error(e.getMessage(), e);
							state = ExecutionActionState.ABORTED;
						} finally {
							if (success) {
								LoadBalancersFacade.removeNode(node.getIpAddress(), node
										.getParent().getName());
								parent.removeNode(node.getIpAddress());
								state = ExecutionActionState.SUCC_FINISHED;
							}

							parent.setLockedUntilExecutionActionFinished(false);
							parent.notify();
							node.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.NONE);

							LOGGER.info("Shut down node " + node.getName());

						}
					}
				}
			}).start();
		} else {
			state = ExecutionActionState.REJECTED;
		}
	}

}
