package explorviz.shared.model;

import java.util.List;

public class TestLandscapeBuilder {
	private final Landscape landscape = new Landscape();

	public Landscape createLandscape() {
		return landscape;
	}

	public TestLandscapeBuilder setTimestamp(final long timestamp) {
		landscape.setTimestamp(timestamp);
		return this;
	}

	public TestLandscapeBuilder setSystems(final List<System> systems) {
		landscape.setSystems(systems);
		return this;
	}
}
