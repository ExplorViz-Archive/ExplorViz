package explorviz.plugin.capacitymanagement.cloud_control;

import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.shared.model.*;

public class CloudControllerForTest implements ICloudController {

	Node testNode;

	public CloudControllerForTest(final Node node) {
		testNode = node;
	}

	@Override
	public String startNode(final NodeGroup nodegroup, Node node) throws Exception {
		java.lang.System.out.println("Node started in nodegroup " + nodegroup.getName());
		return "testNode";
	}

	@Override
	public Node replicateNode(final NodeGroup nodegroup, final Node originalNode) {
		java.lang.System.out.println("Node cloned in nodegroup " + nodegroup.getName());
		return testNode;
	}

	@Override
	public boolean terminateNode(final Node node) {
		java.lang.System.out.println("Node terminated: " + node.getName());
		return true;
	}

	@Override
	public boolean restartNode(final Node node) {
		java.lang.System.out.println("Node restarted: " + node.getName());
		return true;
	}

	@Override
	public boolean restartApplication(final Application application, ScalingGroup scalingGroup) {
		java.lang.System.out.println("Application restarted: " + application.getName());
		return true;
	}

	@Override
	public boolean terminateApplication(final Application application, ScalingGroup scalingGroup) {
		java.lang.System.out.println("Application terminated: " + application.getName());
		return true;
	}

	@Override
	public boolean migrateApplication(final Application application, final Node node) {
		java.lang.System.out.println("Application migrated: " + application.getName());
		return true;
	}

	@Override
	public String startApplication(Application app, ScalingGroup scalingGroup) throws Exception {
		return "42";
	}

	@Override
	public int retrieveRunningNodeCount() {
		return 42;
	}

	@Override
	public String retrieveIdFromNode(Node Node) {
		return "defaultID";
	}

	@Override
	public void copyApplicationToInstance(String privateIP, Application app,
			ScalingGroup scalingGroup) throws Exception {

	}

	@Override
	public boolean checkApplicationIsRunning(String privateIP, String pid, String name) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean instanceExisting(String name) {
		// TODO Auto-generated method stub
		return true;
	}

}
