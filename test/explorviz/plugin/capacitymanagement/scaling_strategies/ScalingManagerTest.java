//package explorviz.plugin.capacitymanagement.scaling_strategies;
//
//import static org.junit.Assert.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Test;
//
//import explorviz.plugin.capacitymanagement.TestConstants;
//import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
//import explorviz.plugin.capacitymanagement.configuration.CapManConfiguration;
//import explorviz.plugin.capacitymanagement.configuration.ScalingGroupsReader;
//import explorviz.plugin.capacitymanagement.node.repository.ScalingGroup;
//import explorviz.plugin.capacitymanagement.node.repository.ScalingGroupRepository;
//import explorviz.shared.model.Node;
//
//public class ScalingManagerTest {
//
//	protected int nodesStarted = 1;
//	protected int counter = 1;
//
//	@Test
//	public void testNewCPUUtilizationAverage() throws Exception {
//		final CapManConfiguration configuration = new CapManConfiguration(TestConstants.CONFIG_FILE);
//		final ScalingGroupRepository scalingGroupRepository = new ScalingGroupRepository(
//				configuration);
//		ScalingGroupsReader.readInScalingGroups(scalingGroupRepository, TestConstants.CONFIG_FILE);
//		final ScalingGroup scalingGroup = scalingGroupRepository.getScalingGroupByName("jpetstore");
//
//		final ScalingManager scalingManager = new ScalingManager(configuration,
//				scalingGroupRepository, new ICloudController() {
//			@Override
//			public Node startNode(final ScalingGroup scalingGroup) throws Exception {
//				final String hostname = "host" + counter;
//				scalingGroup.addNode(String.valueOf(counter), "inst" + counter, hostname);
//				counter++;
//				nodesStarted++;
//				return scalingGroup.getNodeByHostname(hostname);
//			}
//
//			@Override
//			public void shutdownNode(final Node node) {
//				node.getScalingGroup().removeNode(node);
//				nodesStarted--;
//			}
//		});
//
//		// add first node
//		scalingGroup.addNode("0", "inst0", "host0");
//		final Node host0 = scalingGroup.getNodeByHostname("host0");
//
//		assertEquals(1, nodesStarted);
//
//		final Map<Node, Double> averageCPUUtilizations = new HashMap<Node, Double>();
//		averageCPUUtilizations.put(host0, 0.9);
//		scalingManager.newCPUUtilizationAverage(averageCPUUtilizations);
//
//		Thread.sleep(80);
//		assertEquals(2, nodesStarted);
//		final Node host1 = scalingGroup.getNodeByHostname("host1");
//
//		averageCPUUtilizations.clear();
//		averageCPUUtilizations.put(host0, 0.9);
//		averageCPUUtilizations.put(host1, 0.8);
//		scalingManager.newCPUUtilizationAverage(averageCPUUtilizations);
//
//		Thread.sleep(80);
//		assertEquals(3, nodesStarted);
//		final Node host2 = scalingGroup.getNodeByHostname("host2");
//
//		averageCPUUtilizations.clear();
//		averageCPUUtilizations.put(host0, 0.9);
//		averageCPUUtilizations.put(host1, 0.8);
//		averageCPUUtilizations.put(host2, 0.0);
//		scalingManager.newCPUUtilizationAverage(averageCPUUtilizations);
//
//		Thread.sleep(80);
//		assertEquals(3, nodesStarted);
//
//		averageCPUUtilizations.clear();
//		averageCPUUtilizations.put(host0, 0.09);
//		averageCPUUtilizations.put(host1, 0.08);
//		averageCPUUtilizations.put(host2, 0.07);
//		scalingManager.newCPUUtilizationAverage(averageCPUUtilizations);
//
//		Thread.sleep(80);
//		assertEquals(2, scalingGroup.getActiveNodesCount());
//		assertEquals(2, nodesStarted);
//
//		averageCPUUtilizations.clear();
//		averageCPUUtilizations.put(host0, 0.02);
//		averageCPUUtilizations.put(host1, 0.01);
//		scalingManager.newCPUUtilizationAverage(averageCPUUtilizations);
//
//		Thread.sleep(80);
//		assertEquals(1, scalingGroup.getActiveNodesCount());
//		assertEquals(1, nodesStarted);
//
//		averageCPUUtilizations.clear();
//		averageCPUUtilizations.put(host0, 0.01);
//		scalingManager.newCPUUtilizationAverage(averageCPUUtilizations);
//
//		assertEquals(1, scalingGroup.getActiveNodesCount());
//	}
// }
