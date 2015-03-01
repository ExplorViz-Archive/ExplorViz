//package explorviz.plugin.capacitymanagement.loadbalancer;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
//import java.net.URL;
//
//import explorviz.plugin.capacitymanagement.TestConstants;
//import explorviz.plugin.capacitymanagement.configuration.CapManConfiguration;
//import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
//import explorviz.plugin.capacitymanagement.node.repository.Node;
//import explorviz.plugin.capacitymanagement.node.repository.ScalingGroup;
//import explorviz.plugin.capacitymanagement.node.repository.ScalingGroupRepository;
//
//TODO: jek: neuen Test bauen
//public class LoadBalancersFacadeTestManual {
//
//	public static void main(final String[] args) throws IOException {
//		LoadBalancersFacade.addLoadBalancerUrl("localhost", "10200");
//
//		LoadBalancersFacade.reset();
//
//		final ScalingGroupRepository groupRepository = new ScalingGroupRepository(
//				new CapManConfiguration(TestConstants.CONFIG_FILE));
//		groupRepository.addScalingGroup("jpetstore2", "jpetstore2", "x", 1000, "m1.small", "image",
//				"jpetstore2", "jpetstore2", null, true);
//		final ScalingGroup scalingGroup = groupRepository.getScalingGroup(0);
//
//		scalingGroup.addNode("0", "1", "host");
//		final Node node1 = scalingGroup.getNode(0);
//		LoadBalancersFacade.addNode(node1.getPrivateIP(), node1.getScalingGroup().getName());
//
//		assertEquals("0", fetchNewIP());
//		assertEquals("0", fetchNewIP());
//		assertEquals("0", fetchNewIP());
//
//		scalingGroup.addNode("1", "2", "host2");
//		final Node node2 = scalingGroup.getNode(1);
//		LoadBalancersFacade.addNode(node2.getPrivateIP(), node2.getScalingGroup().getName());
//
//		assertEquals("0", fetchNewIP());
//		assertEquals("1", fetchNewIP());
//		assertEquals("0", fetchNewIP());
//		assertEquals("1", fetchNewIP());
//
//		node1.disable();
//
//		assertEquals("1", fetchNewIP());
//		assertEquals("1", fetchNewIP());
//		assertEquals("1", fetchNewIP());
//	}
//
//	private static String fetchNewIP() throws IOException, UnsupportedEncodingException {
//		final BufferedReader in = new BufferedReader(new InputStreamReader(new URL(
//				"http://localhost:10200?group=jpetstore2").openStream(), "UTF-8"));
//		final String newIP = in.readLine();
//		in.close();
//		return newIP;
//	}
//
// }
