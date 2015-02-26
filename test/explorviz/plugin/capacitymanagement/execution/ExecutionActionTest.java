package explorviz.plugin.capacitymanagement.execution;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin.capacitymanagement.cloud_control.CloudControllerForTest;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.execution.*;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.*;

public class ExecutionActionTest {

	private ICloudController controller;
	private NodeGroup parent;
	private final ThreadGroup threadgroup = new ThreadGroup("action");
	private ScalingGroupRepository repository = new ScalingGroupRepository();

	@Before
	public void before() {
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
	public void testApplicationMigrateAction() throws Exception {
		final Node testNode = parent.getNodes().get(0);
		final Application testApp = TestApplicationBuilder.createStandardApplication(1,
				"App-Migration-Test");
		testApp.setParent(testNode);

		final ExecutionAction action = new ApplicationMigrateAction(testApp, testNode);

		action.execute(controller, threadgroup, repository);

		Thread.sleep(200);
		assertEquals(ExecutionActionState.SUCC_FINISHED, action.getState());
	}
}
