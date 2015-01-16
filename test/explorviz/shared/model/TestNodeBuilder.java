package explorviz.shared.model;

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

	public TestNodeBuilder setParent(final NodeGroup parent) {
		node.setParent(parent);
		return this;
	}

	public TestNodeBuilder setApplications(final List<Application> apps) {
		node.setApplications(apps);
		return this;
	}

	public static Node createStandardNode(final String ip) {
		final Node standardNode = new Node();
		standardNode.setIpAddress(ip);
		// TODO: implement TestNodeGroupBuilder, TestApplicationBuilder
		// standardNode.setParent(TestNodeGroupBuilder.createStandardNodeGroup());
		// standardNode.setApplications(TestApplicationBuilder.createStandardApplication());
		return standardNode;
	}

}
