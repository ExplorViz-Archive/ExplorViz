package explorviz.plugin_client.capacitymanagement.configuration;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import explorviz.plugin_server.capacitymanagement.configuration.InitialSetupReader;
import explorviz.plugin_server.capacitymanagement.execution.*;

public class InitialSetupITest {

	@Test
	public void testName() throws Exception {

		String configFile = "./war/META-INF/explorviz.capacity_manager.default.properties";
		CapManConfiguration config = new CapManConfiguration(configFile);

		String initialSetupFile = "./test/resources/test_initial_setup/integration_test.capacity_manager.initial_setup.properties";

		ArrayList<ExecutionAction> nodesToStart = InitialSetupReader
				.readInitialSetup(initialSetupFile);

		ExecutionOrganizer organizer = new ExecutionOrganizer(config);

		organizer.executeActionList(nodesToStart);

		Thread.sleep(500000); // time to finish

		for (ExecutionAction action : nodesToStart) {
			assertEquals(ExecutionActionState.SUCC_FINISHED, action.getState());
		}
	}

}
