package explorviz.shared.model;

import java.util.ArrayList;
import java.util.List;

public class TestLandscapeBuilder {
	private final Landscape landscape = new Landscape();

	public Landscape createLandscape() {
		return landscape;
	}

	public TestLandscapeBuilder setTimestamp(final long timestamp) {
		landscape.setHash(timestamp);
		return this;
	}

	public TestLandscapeBuilder setSystems(final List<System> systems) {
		landscape.setSystems(systems);
		return this;
	}

	public static Landscape createStandardLandscape(final long timestamp) {
		final Landscape ls = new Landscape();
		ls.setHash(timestamp);

		final List<System> systems = new ArrayList<System>();
		systems.add(TestSystemBuilder.createStandardSystem());

		for (final System s : systems) {
			s.setParent(ls);
		}

		ls.setSystems(systems);

		return ls;
	}

}
