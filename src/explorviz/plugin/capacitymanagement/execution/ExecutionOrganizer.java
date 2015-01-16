package explorviz.plugin.capacitymanagement.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.configuration.CapManConfiguration;
import explorviz.plugin.capacitymanagement.configuration.LoadBalancersReader;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.plugin.capacitymanagement.scaling_strategies.IScalingControl;
import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;

@SuppressWarnings("unused")
public class ExecutionOrganizer implements IScalingControl {

	private static final Logger LOG = LoggerFactory.getLogger(ExecutionOrganizer.class);

	private final ICloudController cloudController;

	static int maxRunningNodesLimit = 5; // for test. in live overwritten by
											// configuration

	private final int shutdownDelayInMillis;
	static int waitTimeBeforeNewBootInMillis;

	// TODO: Reihenfolge der Aktionen organisieren
	// je Applikation nur 1 Aktion zur Zeit
	// je Node nur eine Node-Applikation zur Zeit
	// je Node aber mehrere Applikations Aktionen möglich, aber nicht
	// gleichzeitig mit Node-Aktion!

	public ExecutionOrganizer(final CapManConfiguration configuration) throws Exception {

		// TODO: Refactoring: settingsfile schon in CapMan
		// TODO: Refactoring: LoadBalancer Initialisierung
		final String settingsFile = "./META-INF/explorviz" + ".capacity_manager.default.properties";
		LoadBalancersReader.readInLoadBalancers(settingsFile);
		LoadBalancersFacade.reset();

		shutdownDelayInMillis = configuration.getShutdownDelayInMillis();
		waitTimeBeforeNewBootInMillis = configuration.getWaitTimeBeforeNewBootInMillis();
		maxRunningNodesLimit = configuration.getCloudNodeLimit();

		cloudController = createCloudController(configuration);
	}

	// public void startNode(final ScalingGroup scalingGroup) {
	// synchronized (scalingGroup) {
	// if ((scalingGroupRepository.getAllNodesCount() < maxRunningNodesLimit)
	// && !scalingGroup.isLockedUntilInstanceBootFinished()) {
	// scalingGroup.setLockedUntilInstanceBootFinished(true);
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// String newScalingGroupName = null;
	// final Node originalNode = scalingGroup.getNode(0);
	// try {
	// if ((scalingGroup.getDynamicScalingGroup() != null)
	// && !scalingGroup.getDynamicScalingGroup().equals("")) {
	// final ScalingGroup dynamicScalingGroupPrototyp = scalingGroupRepository
	// .getScalingGroupByName(scalingGroup
	// .getDynamicScalingGroup());
	//
	// final int newId = getNewScalingGroupId(dynamicScalingGroupPrototyp);
	//
	// newScalingGroupName = dynamicScalingGroupPrototyp.getName() + "-"
	// + newId;
	//
	// addNewScalingGroupLevel(scalingGroup, newScalingGroupName,
	// dynamicScalingGroupPrototyp, newId);
	//
	// LoadBalancersFacade.addNode(originalNode.getPrivateIP(),
	// newScalingGroupName);
	// final Node startedNode = cloudController
	// .startNode(scalingGroupRepository
	// .getScalingGroupByName(newScalingGroupName));
	// startedNode.setLoadBalancerRemoveAfterStart(
	// originalNode.getPrivateIP(), scalingGroup.getLoadReceiver());
	// scalingGroup.setLoadReceiver(newScalingGroupName);
	// // TODO remove master from this scalingGroup
	// } else {
	// cloudController.startNode(scalingGroup);
	// }
	// } catch (final Exception e) {
	// LOG.error("Error while starting new node:");
	// LOG.error(e.getMessage(), e);
	// } finally {
	// try {
	// if (newScalingGroupName != null) {
	// LoadBalancersFacade.removeNode(originalNode.getPrivateIP(),
	// newScalingGroupName);
	// scalingGroupRepository.removeScalingGroup(newScalingGroupName);
	// }
	// Thread.sleep(waitTimeBeforeNewBootInMillis);
	// } catch (final InterruptedException e) {
	// } finally {
	// scalingGroup.setLockedUntilInstanceBootFinished(false);
	// }
	// }
	// }
	//
	// private int getNewScalingGroupId(final ScalingGroup
	// dynamicScalingGroupPrototyp) {
	// int newId = 1;
	// while (scalingGroupRepository
	// .getScalingGroupByName(dynamicScalingGroupPrototyp.getName() + "-"
	// + newId) != null) {
	// newId++;
	// }
	// return newId;
	// }
	//
	// private void addNewScalingGroupLevel(final ScalingGroup scalingGroup,
	// final String newScalingGroupName,
	// final ScalingGroup dynamicScalingGroupPrototyp, final int newId) {
	// scalingGroupRepository.addScalingGroup(newScalingGroupName,
	// dynamicScalingGroupPrototyp.getApplicationFolder(),
	// dynamicScalingGroupPrototyp.getStartApplicationScript(),
	// dynamicScalingGroupPrototyp
	// .getWaitTimeForApplicationStartInMillis(),
	// dynamicScalingGroupPrototyp.getFlavor(),
	// dynamicScalingGroupPrototyp.getImage(),
	// dynamicScalingGroupPrototyp.getTemplateHostname() + "-" + newId,
	// scalingGroup.getLoadReceiver(), null, true);
	// }
	// }).start();
	// }
	// }
	// }
	//
	// public void shutDownNode(final Node node) {
	// synchronized (node.getScalingGroup()) {
	// if ((node.getScalingGroup().getActiveNodesCount() > 1)
	// && !node.getScalingGroup().isLockedUntilInstanceShutdownFinished()) {
	// final Node nodeFromRepository = node.getScalingGroup().getNodeByHostname(
	// node.getHostname());
	// node.getScalingGroup().setLockedUntilInstanceShutdownFinished(true);
	// nodeFromRepository.disable();
	// LOG.info("Disabled node " + node.getPrivateIP());
	//
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// Thread.sleep(shutdownDelayInMillis);
	// } catch (final InterruptedException e) {
	// } finally {
	// node.getScalingGroup().setLockedUntilInstanceShutdownFinished(false);
	// }
	//
	// LOG.info("Shutting down node " + nodeFromRepository.getPrivateIP());
	// cloudController.shutdownNode(nodeFromRepository);
	// }
	// }).start();
	// }
	// }
	// }

	private static ICloudController createCloudController(final CapManConfiguration configuration)
			throws Exception {
		final Class<?> clazz = Class.forName(configuration.getCloudProvider());
		final ICloudController cloudManager = (ICloudController) clazz.getConstructor(
				CapManConfiguration.class).newInstance(configuration);
		return cloudManager;
	}

	// TODO: throw Exception?
	public void startNode(final NodeGroup nodeGroup) {
		try {
			cloudController.startNode(nodeGroup);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void shutDownNode(final Node node) {
	}
}
