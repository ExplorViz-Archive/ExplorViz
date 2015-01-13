package explorviz.plugin.capacitymanagement.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.attributes.IPluginKeys;
import explorviz.plugin.capacitymanagement.CapManExecutionStates;
import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeReplicateAction extends ExecutionAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeReplicateAction.class);

	private final Node originalNode;

	public NodeReplicateAction(final Node originalNode) {
		this.originalNode = originalNode;
	}

	@Override
	public void execute(final ICloudController controller) {

		if (LoadBalancersFacade.getNodeCount() >= ExecutionOrganizer.maxRunningNodesLimit) {
			state = ExecutionActionState.REJECTED;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				final NodeGroup parent = originalNode.getParent();
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
						newNode = controller.cloneNode(parent, originalNode);
					} catch (final Exception e) {
						LOGGER.error("Error while cloning node:");
						LOGGER.error(e.getMessage(), e);
					} finally {
						if (newNode == null) {
							state = ExecutionActionState.ABORTED;
						} else {
							parent.addNode(newNode);
							LoadBalancersFacade.addNode(newNode.getIpAddress(), parent.getName());
							state = ExecutionActionState.SUCC_FINISHED;
							originalNode.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.NONE);
							LOGGER.info("Cloned node:" + originalNode.getName());
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

	@Override
	protected GenericModelElement getActionObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SyncObject synchronizeOn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean beforeAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean concreteAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void afterAction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getLoggingDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
