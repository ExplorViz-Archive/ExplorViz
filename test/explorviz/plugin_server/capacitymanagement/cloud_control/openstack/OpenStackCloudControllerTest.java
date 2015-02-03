package explorviz.plugin_server.capacitymanagement.cloud_control.openstack;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import explorviz.shared.model.Node;

public class OpenStackCloudControllerTest {

	@Test
	@Ignore
	public void testGetFlavor() throws Exception {
		final Node node = new Node();
		node.setIpAddress("10.50.0.21");
		node.setName("Honululu");
		final String flavor = OpenStackCloudController.getFlavorFromNode(node);
		System.out.println("Flavor: " + flavor);
		assertEquals("m1.small", flavor);
	}

	@Test
	@Ignore
	public void testGetImage() throws Exception {
		final Node node = new Node();
		node.setIpAddress("10.50.0.21");
		node.setName("Honululu");
		final String image = OpenStackCloudController.getImageFromNode(node);
		System.out.println("Image: " + image);
		assertEquals("Test-Server 2 - Kopie", image);
	}

	@Ignore
	@Test
	public void testGetImage2() throws Exception {
		final Node node = new Node();
		node.setIpAddress("10.50.0.19");
		node.setName("Honululu");
		final String image = OpenStackCloudController.getImageFromNode(node);
		System.out.println("Image: " + image);
		assertEquals("Ubuntu-14.04", image);
	}

	@Ignore
	@Test
	public void testGetId() throws Exception {
		final Node node = new Node();
		node.setIpAddress("10.50.0.21");
		node.setName("Honululu");
		final String id = OpenStackCloudController.getIdFromNode(node);
		System.out.println("Id: " + id);
		assertEquals("1b23dea1-d807-4628-845f-fa3070dec495", id);
	}

	@Ignore
	@Test
	public void testGetHostname() throws Exception {
		final Node node = new Node();
		node.setIpAddress("10.50.0.18");
		node.setName("Honulu");
		final String hostname = OpenStackCloudController.getHostnameFromNode(node);
		System.out.println("Hostname: " + hostname);
		assertEquals("TestServer", hostname);
	}

	@Ignore
	@Test
	public void testGetHostname2() throws Exception {
		final Node node = new Node();
		node.setIpAddress("10.50.0.19");
		node.setName("Honululu");
		final String hostname = OpenStackCloudController.getHostnameFromNode(node);
		System.out.println("Hostname: " + hostname);
		assertEquals("Test-Server 2", hostname);
	}

}
