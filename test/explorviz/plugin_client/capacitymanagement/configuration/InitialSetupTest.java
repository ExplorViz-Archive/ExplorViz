package explorviz.plugin_client.capacitymanagement.configuration;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import explorviz.plugin.capacitymanagement.cloud_control.CloudControllerForTest;
import explorviz.plugin_client.capacitymanagement.execution.ExecutionAction;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.shared.model.TestNodeBuilder;

public class InitialSetupTest {

	@Test
	public void testName() throws Exception {

		ICloudController cloudController = new CloudControllerForTest(
				TestNodeBuilder.createStandardNode("111", new String[1]));

		String initialSetupFile = "./test/resources/test.capacity_manager.initial_setup.properties";

		List<ExecutionAction> nodesToStart = InitialSetupReader.readInitialSetup(initialSetupFile);

		final ThreadGroup actionThreads = new ThreadGroup("actions");
		for (final ExecutionAction action : nodesToStart) {
			action.execute(cloudController, actionThreads);
		}

		assertEquals(2, nodesToStart.size());
	}

}
