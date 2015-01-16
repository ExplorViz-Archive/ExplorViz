package explorviz.shared.model;

import java.util.ArrayList;
import java.util.List;

public class TestNodeBuilder {

	private final Node node = new Node();

	public Node createNode() {
		return node;
	}

	public TestNodeBuilder setIP(final String ip) {
		node.setIpAddress(ip);
		return this;
	}

	public TestNodeBuilder setName(final String name) {
		node.setName(name);
		return this;
	}

	public TestNodeBuilder setParent(final NodeGroup parent) {
		node.setParent(parent);
		return this;
	}

	public TestNodeBuilder setApplications(final List<Application> apps) {
		node.setApplications(apps);
		return this;
	}

	public static Node createStandardNode(final String ip, final String[] appNames) {
		final Node standardNode = new Node();
		standardNode.setIpAddress(ip);
		final ArrayList<Application> applications = new ArrayList<Application>();
		for (int i = 0; i < appNames.length; i++) {
			applications.add(TestApplicationBuilder.createStandardApplication(i, appNames[i]));
		}
		standardNode.setApplications(applications);
		return standardNode;
	}

}
