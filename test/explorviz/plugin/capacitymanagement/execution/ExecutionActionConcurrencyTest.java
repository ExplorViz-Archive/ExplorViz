package explorviz.plugin.capacitymanagement.execution;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin.capacitymanagement.cloud_control.CloudControllerForConcurrencyTest;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.execution.*;
import explorviz.shared.model.*;

public class ExecutionActionConcurrencyTest {

	Node testNode;
	ThreadGroup threadgroup = new ThreadGroup("test-thread-group");

	@Before
	public void before() {
		final String[] apps = { "test1", "test2" };
		testNode = TestNodeBuilder.createStandardNode("1234", apps);

	}

	@Test
	public void testScenario1() throws Exception {

		ICloudController controller = new CloudControllerForConcurrencyTest(testNode,
				getRandomMillis(), getRandomMillis(), getRandomMillis(), getRandomMillis(),
				getRandomMillis(), getRandomMillis(), getRandomMillis());

		NodeGroup parent1 = TestNodeGroupBuilder.createStandardNodeGroup("group-test-1");
		NodeGroup parent2 = TestNodeGroupBuilder.createStandardNodeGroup("group-test-2");
		NodeGroup parent3 = TestNodeGroupBuilder.createStandardNodeGroup("group-test-3");

		List<Application> apps1 = new ArrayList<Application>();
		List<Application> apps2 = new ArrayList<Application>();
		List<Application> apps3 = new ArrayList<Application>();

		apps1.add(parent1.getNodes().get(0).getApplications().get(0));
		apps1.add(parent1.getNodes().get(0).getApplications().get(1));
		apps1.add(parent1.getNodes().get(1).getApplications().get(0));
		apps1.add(parent1.getNodes().get(1).getApplications().get(1));
		apps1.add(parent1.getNodes().get(2).getApplications().get(2));

		apps2.add(parent2.getNodes().get(1).getApplications().get(1));
		apps2.add(parent2.getNodes().get(2).getApplications().get(4));

		apps3.add(parent2.getNodes().get(0).getApplications().get(1));
		apps3.add(parent3.getNodes().get(0).getApplications().get(1));

		// TODO: jek: assert scalingGroup sizes
		// ScalingGroup scaling1 =
		// TestScalingGroupBuilder.createStandardScalingGroup("scaling1",
		// apps1);
		// ScalingGroup scaling2 =
		// TestScalingGroupBuilder.createStandardScalingGroup("scaling2",
		// apps2);
		// ScalingGroup scaling3 =
		// TestScalingGroupBuilder.createStandardScalingGroup("scaling3",
		// apps3);

		ExecutionAction action1 = new NodeReplicateAction(parent1.getNodes().get(0));
		ExecutionAction action2 = new NodeTerminateAction(parent2.getNodes().get(0));
		ExecutionAction action3 = new NodeRestartAction(parent3.getNodes().get(2));
		ExecutionAction action4 = new NodeReplicateAction(parent1.getNodes().get(2));
		ExecutionAction action5 = new NodeRestartAction(parent3.getNodes().get(0));

		ArrayList<ExecutionAction> actionList = new ArrayList<ExecutionAction>();
		actionList.add(action1);
		actionList.add(action2);
		actionList.add(action3);
		actionList.add(action4);
		actionList.add(action5);

		for (ExecutionAction action : actionList) {
			action.execute(controller, threadgroup);
		}
		Thread.sleep(30000);
		assertTrue(action1.getState() == ExecutionActionState.SUCC_FINISHED);
		assertTrue(action2.getState() == ExecutionActionState.SUCC_FINISHED);
		assertTrue(action3.getState() == ExecutionActionState.SUCC_FINISHED);
		assertTrue(action4.getState() == ExecutionActionState.SUCC_FINISHED);
		assertTrue(action5.getState() == ExecutionActionState.SUCC_FINISHED);

	}

	private int getRandomMillis() {
		return (int) Math.random() * 1000;
	}

}
