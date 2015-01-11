package explorviz.plugin.capacitymanagement.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.attributes.IPluginKeys;
import explorviz.plugin.capacitymanagement.CapManExecutionStates;
import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;

public class NodeNewInstanceAction extends ExecutionAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeNewInstanceAction.class);

	private final NodeGroup parent;

	public NodeNewInstanceAction(final NodeGroup nodegroup) {
		parent = nodegroup;
	}

	@Override
	public void execute(final ICloudController controller) {

		if (LoadBalancersFacade.getNodeCount() >= ExecutionOrganizer.maxRunningNodesLimit) {
			state = ExecutionActionState.REJECTED;
		}
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
					Node newNode = null;
					try {
						newNode = controller.startNode(parent);
					} catch (final Exception e) {
						LOGGER.error("Error while starting new node:");
						LOGGER.error(e.getMessage(), e);
					} finally {
						if (newNode == null) {
							state = ExecutionActionState.ABORTED;
						} else {
							parent.addNode(newNode);
							LoadBalancersFacade.addNode(newNode.getIpAddress(), parent.getName());
							state = ExecutionActionState.SUCC_FINISHED;
							parent.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.NONE);
						}
						parent.setLockedUntilExecutionActionFinished(false);
						parent.notify();
					}
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

		}).start();
	}
}
