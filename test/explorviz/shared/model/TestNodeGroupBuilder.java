package explorviz.shared.model;

import java.util.ArrayList;
import java.util.List;

public class TestNodeGroupBuilder {

	private final NodeGroup nodegroup = new NodeGroup();

	public NodeGroup createNodeGroup() {
		return nodegroup;
	}

	public TestNodeGroupBuilder setName(final String name) {
		nodegroup.setName(name);
		return this;
	}

	public TestNodeGroupBuilder setNodes(final List<Node> nodes) {
		nodegroup.setNodes(nodes);
		for (final Node n : nodes) {
			n.setParent(nodegroup);
		}
		return this;
	}

	public static NodeGroup createStandardNodeGroup(final String name) {
		final NodeGroup ng = new NodeGroup();
		ng.setName(name);

		final List<Node> nodes = new ArrayList<Node>();
		final String[] apps1 = { "application1.1", "application1.2", "application1.3" };
		nodes.add(TestNodeBuilder.createStandardNode("11111", apps1));
		final String[] apps2 = { "application2.1", "application2.2" };
		nodes.add(TestNodeBuilder.createStandardNode("22222", apps2));
		final String[] apps3 = { "app3.1", "app3.2", "app3.3", "app3.4", "app3.4" };
		nodes.add(TestNodeBuilder.createStandardNode("33333", apps3));

		for (final Node n : nodes) {
			n.setParent(ng);
		}

		ng.setNodes(nodes);
		return ng;
	}

}
