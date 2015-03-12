package explorviz.plugin.capacitymanagement.execution;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin.capacitymanagement.cloud_control.CloudControllerForTest;
import explorviz.plugin_client.capacitymanagement.configuration.CapManConfigurationForTest;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.execution.*;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.plugin_server.capacitymanagement.loadbalancer.TestScalingGroupBuilder;
import explorviz.shared.model.*;

public class ExecutionActionTest {

	private ICloudController controller;
	private NodeGroup parent;
	private final ThreadGroup threadgroup = new ThreadGroup("action");
	private ScalingGroupRepository repository = new ScalingGroupRepository();

	@Before
	public void before() throws Exception {
		@SuppressWarnings("unused")
		// needed to set static values
		ExecutionOrganizer organizer = new ExecutionOrganizer(new CapManConfigurationForTest(null,
				null), repository);

		repository.addScalingGroup(TestScalingGroupBuilder.createStandardScalingGroup());

		final String[] apps = { "test1", "test2" };
		final Node testNode = TestNodeBuilder.createStandardNode("1234", apps);
		controller = new CloudControllerForTest(testNode);
		parent = TestNodeGroupBuilder.createStandardNodeGroup("group-test");

	}

	@Test
	public void testNodeRestartAction() throws Exception {
		final ExecutionAction action = new NodeRestartAction(parent.getNodes().get(0));

		action.execute(controller, threadgroup, repository);

		Thread.sleep(200);
		assertEquals(ExecutionActionState.SUCC_FINISHED, action.getState());
	}

	@Test
	public void testApplicationTerminateAction() throws Exception {
		final Node testNode = parent.getNodes().get(0);
		final Application testApp = testNode.getApplications().get(0);

		final ExecutionAction action = new ApplicationTerminateAction(testApp);

		action.execute(controller, threadgroup, repository);

		Thread.sleep(200);
		assertEquals(ExecutionActionState.SUCC_FINISHED, action.getState());
	}
}
