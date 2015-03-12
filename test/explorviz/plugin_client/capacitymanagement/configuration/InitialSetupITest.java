package explorviz.plugin_client.capacitymanagement.configuration;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import explorviz.plugin_server.capacitymanagement.configuration.InitialSetupReader;
import explorviz.plugin_server.capacitymanagement.execution.*;

public class InitialSetupITest {

	// This test requires a VPN-connection to the network with the
	// OpenStackCloud! Also it requires a file with Login-Data for the cloud in
	// the the specified resourceFolder and a cloudKey which is available for
	// the login user.
	// The tests starts 2 nodes in the cloud. It is recommended to delete them
	// after testing.
	// Since the SceneDrawer is used in ExecutionOrganzier, NPE occurs because
	// no lastLandscape exists. However, the nodes are started in the cloud.
	@Test
	@Ignore
	public void testName() throws Exception {

		// path has to be changed and login-data put.
		CapManConfiguration config = new CapManConfigurationForTest(
				"/home/johanna/explorviz_resources/", "test");

		String initialSetupFile = "./test/resources/test_initial_setup/integration_test.capacity_manager.initial_setup.properties";

		ArrayList<ExecutionAction> nodesToStart = InitialSetupReader
				.readInitialSetup(initialSetupFile);

		ExecutionOrganizer organizer = new ExecutionOrganizer(config,
				InitialSetupReader.getScalingGroupRepository());

		organizer.executeActionList(nodesToStart);

		Thread.sleep(10000); // time to finish

		for (ExecutionAction action : nodesToStart) {
			assertEquals(ExecutionActionState.SUCC_FINISHED, action.getState());
		}
	}

}
