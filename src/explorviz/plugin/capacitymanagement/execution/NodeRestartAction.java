package explorviz.plugin.capacitymanagement.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.attributes.IPluginKeys;
import explorviz.plugin.capacitymanagement.CapManExecutionStates;
import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;

public class NodeRestartAction extends ExecutionAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeNewInstanceAction.class);

	private final Node node;

	public NodeRestartAction(final Node node) {
		this.node = node;
	}

	@Override
	public void execute(final ICloudController controller) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO: synchronize on NodeGroup?
				final NodeGroup parent = node.getParent();
				synchronized (parent) {
					while (parent.isLockedUntilExecutionActionFinished()) {
						try {
							parent.wait();
						} catch (final InterruptedException e) {

						}
					}
					parent.setLockedUntilExecutionActionFinished(true);
					LoadBalancersFacade.removeNode(node.getIpAddress(), node.getParent().getName());
					boolean success = false;
					try {
						success = controller.restartNode(node);
					} catch (final Exception e) {
						LOGGER.error("Error while starting new node:");
						LOGGER.error(e.getMessage(), e);
						state = ExecutionActionState.ABORTED;
					} finally {
						if (success) {
							LoadBalancersFacade.addNode(node.getIpAddress(), parent.getName());
							state = ExecutionActionState.SUCC_FINISHED;
						}
						node.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
								CapManExecutionStates.NONE);
						parent.setLockedUntilExecutionActionFinished(false);
						parent.notify();
					}
				}
				// scalingGroup.setLoadReceiver(newScalingGroupName);
				// // TODO remove master from this scalingGroup
				// }
			}

		}).start();
	}

}
