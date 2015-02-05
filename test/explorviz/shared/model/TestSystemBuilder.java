package explorviz.shared.model;

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
}
