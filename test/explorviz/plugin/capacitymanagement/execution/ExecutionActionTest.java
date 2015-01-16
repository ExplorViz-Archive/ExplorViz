package explorviz.plugin.capacitymanagement.execution;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin.capacitymanagement.cloud_control.CloudControllerForTest;
import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.shared.model.*;

public class ExecutionActionTest {

	private ICloudController controller;
	private NodeGroup parent;

	@Before
	public void before() {
		final String[] apps = { "test1", "test2" };
		final Node testNode = TestNodeBuilder.createStandardNode("1234", apps);
		controller = new CloudControllerForTest(testNode);
		parent = TestNodeGroupBuilder.createStandardNodeGroup("group-test");

	}

	@Test
	public void testNodeNewInstanceAction() throws Exception {
		final ExecutionAction action = new NodeNewInstanceAction(parent);
		action.execute(controller);
		Thread.sleep(200); // allow Thread to finish
		assertEquals(ExecutionActionState.SUCC_FINISHED, action.getState());
	}

	@Test
	public void testNodeRestartAction() throws Exception {
		final ExecutionAction action = new NodeRestartAction(parent.getNodes().get(0));
		action.execute(controller);
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
		action.execute(controller);
		Thread.sleep(200);
		assertEquals(ExecutionActionState.SUCC_FINISHED, action.getState());
	}
}
