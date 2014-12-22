package explorviz.plugin.capacitymanagement.scaling_strategies;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.configuration.Configuration;
import explorviz.plugin.capacitymanagement.cpu_utilization.IAverageCPUUtilizationReceiver;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.plugin.capacitymanagement.node.repository.*;

/**
 * Calculates average cpu utilizations, starts/shuts down nodes
 *
 * @author jgi, dtj
 */
public class ScalingManager implements IAverageCPUUtilizationReceiver, IScalingControl {

	private static final Logger LOG = LoggerFactory.getLogger(ScalingManager.class);

	private final ScalingGroupRepository scalingGroupRepository;
	private final ICloudController cloudController;

	private final IScalingStrategy strategy;

	private final int maxRunningNodesLimit;

	private final int shutdownDelayInMillis;
	private final int waitTimeBeforeNewBootInMillis;

	/**
	 * @param configuration
	 *            configuration file
	 * @param scalingGroupRepository
	 *            Repository of scaling groups
	 * @param cloudController
	 *            cloud controller
	 * @throws Exception
	 *             if scaling strategy can't be loaded
	 */
	public ScalingManager(final Configuration configuration,
			final ScalingGroupRepository scalingGroupRepository,
			final ICloudController cloudController) throws Exception {
		shutdownDelayInMillis = configuration.getShutdownDelayInMillis();
		waitTimeBeforeNewBootInMillis = configuration.getWaitTimeBeforeNewBootInMillis();
		maxRunningNodesLimit = configuration.getCloudNodeLimit();

		this.scalingGroupRepository = scalingGroupRepository;
		this.cloudController = cloudController;

		final Class<?> strategyClazz = Class
				.forName("explorviz.capacity_manager.capacitymgt.scaling.strategies."
						+ configuration.getScalingStrategy());
		// loads strategy to analyze nodes that is determined in the
		// configuration file
		strategy = (IScalingStrategy) strategyClazz.getConstructor(IScalingControl.class,
				Configuration.class).newInstance(this, configuration);
	}

	@Override
	public void newCPUUtilizationAverage(final Map<Node, Double> averageCPUUtilizations) {
		if (!averageCPUUtilizations.isEmpty()) {
			strategy.analyze(averageCPUUtilizations);
		}
	}

	@Override
	public void startNode(final ScalingGroup scalingGroup) {
		synchronized (scalingGroup) {
			if ((scalingGroupRepository.getAllNodesCount() < maxRunningNodesLimit)
					&& !scalingGroup.isLockedUntilInstanceBootFinished()) {
				scalingGroup.setLockedUntilInstanceBootFinished(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						String newScalingGroupName = null;
						final Node originalNode = scalingGroup.getNode(0);
						try {
							if ((scalingGroup.getDynamicScalingGroup() != null)
									&& !scalingGroup.getDynamicScalingGroup().equals("")) {
								final ScalingGroup dynamicScalingGroupPrototyp = scalingGroupRepository
										.getScalingGroupByName(scalingGroup
												.getDynamicScalingGroup());

								final int newId = getNewScalingGroupId(dynamicScalingGroupPrototyp);

								newScalingGroupName = dynamicScalingGroupPrototyp.getName() + "-"
										+ newId;

								addNewScalingGroupLevel(scalingGroup, newScalingGroupName,
										dynamicScalingGroupPrototyp, newId);

								LoadBalancersFacade.addNode(originalNode.getPrivateIP(),
										newScalingGroupName);
								final Node startedNode = cloudController
										.startNode(scalingGroupRepository
												.getScalingGroupByName(newScalingGroupName));
								startedNode.setLoadBalancerRemoveAfterStart(
										originalNode.getPrivateIP(), scalingGroup.getLoadReceiver());
								scalingGroup.setLoadReceiver(newScalingGroupName);
								// TODO remove master from this scalingGroup
							} else {
								cloudController.startNode(scalingGroup);
							}
						} catch (final Exception e) {
							LOG.error("Error while starting new node:");
							LOG.error(e.getMessage(), e);
						} finally {
							try {
								if (newScalingGroupName != null) {
									LoadBalancersFacade.removeNode(originalNode.getPrivateIP(),
											newScalingGroupName);
									scalingGroupRepository.removeScalingGroup(newScalingGroupName);
								}
								Thread.sleep(waitTimeBeforeNewBootInMillis);
							} catch (final InterruptedException e) {
							} finally {
								scalingGroup.setLockedUntilInstanceBootFinished(false);
							}
						}
					}

					private int getNewScalingGroupId(final ScalingGroup dynamicScalingGroupPrototyp) {
						int newId = 1;
						while (scalingGroupRepository
								.getScalingGroupByName(dynamicScalingGroupPrototyp.getName() + "-"
										+ newId) != null) {
							newId++;
						}
						return newId;
					}

					private void addNewScalingGroupLevel(final ScalingGroup scalingGroup,
							final String newScalingGroupName,
							final ScalingGroup dynamicScalingGroupPrototyp, final int newId) {
						scalingGroupRepository.addScalingGroup(newScalingGroupName,
								dynamicScalingGroupPrototyp.getApplicationFolder(),
								dynamicScalingGroupPrototyp.getStartApplicationScript(),
								dynamicScalingGroupPrototyp
										.getWaitTimeForApplicationStartInMillis(),
								dynamicScalingGroupPrototyp.getFlavor(),
								dynamicScalingGroupPrototyp.getImage(),
								dynamicScalingGroupPrototyp.getTemplateHostname() + "-" + newId,
								scalingGroup.getLoadReceiver(), null, true);
					}
				}).start();
			}
		}
	}

	@Override
	public void shutDownNode(final Node node) {
		synchronized (node.getScalingGroup()) {
			if ((node.getScalingGroup().getActiveNodesCount() > 1)
					&& !node.getScalingGroup().isLockedUntilInstanceShutdownFinished()) {
				final Node nodeFromRepository = node.getScalingGroup().getNodeByHostname(
						node.getHostname());
				node.getScalingGroup().setLockedUntilInstanceShutdownFinished(true);
				nodeFromRepository.disable();
				LOG.info("Disabled node " + node.getPrivateIP());

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(shutdownDelayInMillis);
						} catch (final InterruptedException e) {
						} finally {
							node.getScalingGroup().setLockedUntilInstanceShutdownFinished(false);
						}

						LOG.info("Shutting down node " + nodeFromRepository.getPrivateIP());
						cloudController.shutdownNode(nodeFromRepository);
					}
				}).start();
			}
		}
	}

}
