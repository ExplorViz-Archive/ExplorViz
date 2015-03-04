package explorviz.plugin_server.capacitymanagement.cloud_control.openstack;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration;
import explorviz.plugin_server.capacitymanagement.cloud_control.common.TerminalCommunication;
import explorviz.plugin_server.capacitymanagement.configuration.LoadBalancersReader;
import explorviz.shared.model.Node;

@Ignore
// Needs to be executed in the VPN and novaclient has to be installed.
public class OpenStackCloudControllerTest {

	private OpenStackCloudController controller;

	public void createOpenStackCloudControllerTest() throws Exception {
		String settingsFile = "./war/META-INF/explorviz.capacity_manager_test.default.properties";

		CapManConfiguration configuration;

		LoadBalancersReader.readInLoadBalancers(settingsFile);
		configuration = new CapManConfiguration(settingsFile);

		controller = new OpenStackCloudController(configuration);

	}

	@Test
	public void testGetFlavor() throws Exception {
		createOpenStackCloudControllerTest();
		final Node node = new Node();
		node.setIpAddress("10.50.0.19");
		node.setName("Honululu");
		final String flavor = controller.retrieveFlavorFromNode(node);
		System.out.println("Flavor: " + flavor);
		assertEquals("m1.small", flavor);
	}

	@Test
	public void testGetImage() throws Exception {
		createOpenStackCloudControllerTest();
		final Node node = new Node();
		node.setIpAddress("10.50.0.19");
		node.setName("Honululu");
		final String image = controller.retrieveImageFromNode(node);
		System.out.println("Image: " + image);
		assertEquals("Ubuntu-13.10", image);
	}

	@Test
	public void testGetImage2() throws Exception {
		createOpenStackCloudControllerTest();
		final Node node = new Node();
		node.setIpAddress("10.50.0.17");
		node.setName("Honululu");
		final String image = controller.retrieveImageFromNode(node);
		System.out.println("Image: " + image);
		assertEquals("Ubuntu-14.04", image);
	}

	@Test
	public void testGetId() throws Exception {
		createOpenStackCloudControllerTest();
		final Node node = new Node();
		node.setIpAddress("10.50.0.21");
		node.setName("Honululu");
		final String id = controller.retrieveIdFromNode(node);
		System.out.println("Id: " + id);
		assertEquals("51078790-94c5-4039-879d-2386bd57d14f", id);
	}

	@Test
	public void testGetHostname() throws Exception {
		createOpenStackCloudControllerTest();
		final Node node = new Node();
		node.setIpAddress("10.50.0.18");
		node.setName("Honulu");
		final String hostname = controller.retrieveHostnameFromNode(node);
		System.out.println("Hostname: " + hostname);
		assertEquals("NewTestServer", hostname);
	}

	@Test
	public void testGetHostname2() throws Exception {
		createOpenStackCloudControllerTest();
		final Node node = new Node();
		node.setIpAddress("10.50.0.10");
		node.setName("Honululu");
		final String hostname = controller.retrieveHostnameFromNode(node);
		System.out.println("Hostname: " + hostname);
		assertEquals("TestServer1.2", hostname);
	}

	@Test
	public void testGetStatus() throws Exception {
		createOpenStackCloudControllerTest();
		final Node node = new Node();
		node.setIpAddress("10.50.0.19");
		node.setName("NewTestServer2");
		final String status = controller.retrieveStatusOfInstance(node.getIpAddress());
		System.out.println("Status:" + status);
		assertEquals("ACTIVE", status);
	}

	@Test
	public void testCreateImage() throws Exception {
		createOpenStackCloudControllerTest();
		String image = controller.createImageFromInstance("NewTestServer2");
		System.out.println(TerminalCommunication.executeNovaCommand("image-list"));
		assertEquals("NewTestServer2Image", image);
	}

}
