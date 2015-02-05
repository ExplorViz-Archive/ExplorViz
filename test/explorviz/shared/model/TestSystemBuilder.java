package explorviz.shared.model;

import java.util.ArrayList;
import java.util.List;

public class TestSystemBuilder {
	private final System system = new System();

	public System createSystem() {
		return system;
	}

	public TestSystemBuilder setNodeGroups(final List<NodeGroup> nodeGroups) {
		system.setNodeGroups(nodeGroups);
		return this;
	}

	public TestSystemBuilder setLandscape(final Landscape landscape) {
		system.setParent(landscape);
		return this;
	}

	public static System createStandardSystem() {
		final System sys = new System();

		final List<NodeGroup> nodeGroups = new ArrayList<NodeGroup>();
		nodeGroups.add(TestNodeGroupBuilder.createStandardNodeGroup("ng1"));
		nodeGroups.add(TestNodeGroupBuilder.createStandardNodeGroup("ng2"));

		for (final NodeGroup ng : nodeGroups) {
			ng.setParent(sys);
		}

		sys.setNodeGroups(nodeGroups);

		return sys;
	}
}
