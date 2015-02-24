package explorviz.plugin_client.capacitymanagement.configuration;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import explorviz.plugin.capacitymanagement.cloud_control.CloudControllerForTest;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.execution.ExecutionAction;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.TestNodeBuilder;

public class InitialSetupTest {

	@Test
	public void testName() throws Exception {

		ICloudController cloudController = new CloudControllerForTest(
				TestNodeBuilder.createStandardNode("111", new String[1]));
		ScalingGroupRepository repository = new ScalingGroupRepository();

		String initialSetupFile = "./test/resources/test.capacity_manager.initial_setup.properties";

		List<ExecutionAction> nodesToStart = InitialSetupReader.readInitialSetup(initialSetupFile);

		final ThreadGroup actionThreads = new ThreadGroup("actions");
		for (final ExecutionAction action : nodesToStart) {
			action.execute(cloudController, actionThreads, repository);
		}

		assertEquals(2, nodesToStart.size());
	}

}
