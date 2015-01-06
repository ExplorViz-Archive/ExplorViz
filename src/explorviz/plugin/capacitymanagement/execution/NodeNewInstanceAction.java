package explorviz.plugin.capacitymanagement.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;

@SuppressWarnings("unused")
public class NodeNewInstanceAction implements ExecutionAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeNewInstanceAction.class);

	private final Node originalNode;

	public NodeNewInstanceAction(final Node originalNode) {
		this.originalNode = originalNode;
	}

	@Override
	public void execute(final ICloudController controller) {
		final NodeGroup parent = originalNode.getParent();
		synchronized (parent) {
			if ((LoadBalancersFacade.getNodeCount() < ExecutionOrganizer.maxRunningNodesLimit)
					&& !parent.isLockedUntilInstanceBootFinished()) {
				parent.setLockedUntilInstanceBootFinished(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						final String newScalingGroupName = null;
						// final Node originalNode = parent.getNode(0);
						try {
							// if ((scalingGroup.getDynamicScalingGroup() !=
							// null)
							// &&
							// !scalingGroup.getDynamicScalingGroup().equals(""))
							// {
							// final ScalingGroup dynamicScalingGroupPrototyp =
							// scalingGroupRepository
							// .getScalingGroupByName(scalingGroup
							// .getDynamicScalingGroup());
							//
							// final int newId =
							// getNewScalingGroupId(dynamicScalingGroupPrototyp);
							//
							// newScalingGroupName =
							// dynamicScalingGroupPrototyp.getName() + "-"
							// + newId;
							//
							// addNewScalingGroupLevel(scalingGroup,
							// newScalingGroupName,
							// dynamicScalingGroupPrototyp, newId);
							//
							// LoadBalancersFacade.addNode(originalNode.getPrivateIP(),
							// newScalingGroupName);
							// final Node startedNode = cloudController
							// .startNode(scalingGroupRepository
							// .getScalingGroupByName(newScalingGroupName));
							// startedNode.setLoadBalancerRemoveAfterStart(
							// originalNode.getPrivateIP(),
							// scalingGroup.getLoadReceiver());
							// scalingGroup.setLoadReceiver(newScalingGroupName);
							// // TODO remove master from this scalingGroup
							// } else {
							controller.startNode(parent);
							// }
						} catch (final Exception e) {
							LOGGER.error("Error while starting new node:");
							LOGGER.error(e.getMessage(), e);
						} finally {
							try {
								// if (newScalingGroupName != null) {
								// LoadBalancersFacade.removeNode(originalNode.getIpAddress(),
								// newScalingGroupName);
								// scalingGroupRepository.removeScalingGroup(newScalingGroupName);
								// }
								Thread.sleep(ExecutionOrganizer.waitTimeBeforeNewBootInMillis);
							} catch (final InterruptedException e) {
							} finally {
								// scalingGroup.setLockedUntilInstanceBootFinished(false);
							}
						}
					}

					// private int getNewScalingGroupId(final ScalingGroup
					// dynamicScalingGroupPrototyp) {
					// int newId = 1;
					// while (scalingGroupRepository
					// .getScalingGroupByName(dynamicScalingGroupPrototyp.getName()
					// + "-"
					// + newId) != null) {
					// newId++;
					// }
					// return newId;
					// }
					//
					// private void addNewScalingGroupLevel(final ScalingGroup
					// scalingGroup,
					// final String newScalingGroupName,
					// final ScalingGroup dynamicScalingGroupPrototyp, final int
					// newId) {
					// scalingGroupRepository.addScalingGroup(newScalingGroupName,
					// dynamicScalingGroupPrototyp.getApplicationFolder(),
					// dynamicScalingGroupPrototyp.getStartApplicationScript(),
					// dynamicScalingGroupPrototyp
					// .getWaitTimeForApplicationStartInMillis(),
					// dynamicScalingGroupPrototyp.getFlavor(),
					// dynamicScalingGroupPrototyp.getImage(),
					// dynamicScalingGroupPrototyp.getTemplateHostname() + "-" +
					// newId,
					// scalingGroup.getLoadReceiver(), null, true);
					// }
				}).start();
			}
		}

	}

}
