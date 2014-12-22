//package explorviz.plugin.capacitymanagement.node.repository;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import org.junit.Test;
//
//import explorviz.plugin.capacitymanagement.TestConstants;
//import explorviz.plugin.capacitymanagement.configuration.CapManConfiguration;
//import explorviz.plugin.capacitymanagement.configuration.ScalingGroupsReader;
//import explorviz.plugin.capacitymanagement.node.repository.Node;
//import explorviz.plugin.capacitymanagement.node.repository.ScalingGroup;
//import explorviz.plugin.capacitymanagement.node.repository.ScalingGroupRepository;
//
//public class ScalingGroupTest {
//
//	@Test
//	public void testAddNode() throws Exception {
//		final ScalingGroupRepository scalingGroupRepository = new ScalingGroupRepository(
//				new CapManConfiguration(TestConstants.CONFIG_FILE));
//
//		ScalingGroupsReader.readInScalingGroups(scalingGroupRepository, TestConstants.CONFIG_FILE);
//		final ScalingGroup scalingGroup = scalingGroupRepository.getScalingGroupByName("jpetstore");
//
//		scalingGroup.addNode("0", "inst0", "host0");
//		assertEquals(1, scalingGroup.getNodesCount());
//		scalingGroup.addNode("1", "inst1", "host1");
//		assertEquals(2, scalingGroup.getNodesCount());
//
//		Node nodeByIndex = scalingGroup.getNode(0);
//		assertNotNull(nodeByIndex);
//		assertEquals("0", nodeByIndex.getPrivateIP());
//		nodeByIndex = scalingGroup.getNode(1);
//		assertNotNull(nodeByIndex);
//		assertEquals("1", nodeByIndex.getPrivateIP());
//
//		Node nodeByHostname = scalingGroup.getNodeByHostname("host0");
//		assertNotNull(nodeByHostname);
//
//		assertEquals("0", nodeByHostname.getPrivateIP());
//		assertEquals("inst0", nodeByHostname.getInstanceId());
//		assertNotNull(nodeByHostname.getCreationDate());
//		assertEquals("host0", nodeByHostname.getHostname());
//
//		assertTrue(nodeByHostname.isEnabled());
//
//		nodeByHostname = scalingGroup.getNodeByHostname("host1");
//		assertNotNull(nodeByHostname);
//
//		scalingGroup.removeNode(nodeByHostname);
//		assertEquals(1, scalingGroup.getNodesCount());
//	}
// }
