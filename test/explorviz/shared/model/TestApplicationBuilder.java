package explorviz.shared.model;

import java.util.ArrayList;
import java.util.List;

public class TestApplicationBuilder {

	private final Application application = new Application();

	public Application createApplication() {
		return application;
	}

	public TestApplicationBuilder setName(final String name) {
		application.setName(name);
		return this;
	}

	public TestApplicationBuilder setId(final int id) {
		application.setId(id);
		return this;
	}

	public static Application createStandardApplication(final int id, final String name) {
		final Application app = new Application();
		app.setName(name);
		app.setId(id);

		// Classes: clazz
		List<Clazz> clazzes = new ArrayList<>();
		Clazz clazz = new Clazz();
		clazzes.add(clazz);

		// Component: c1
		List<Component> components = new ArrayList<>();
		Component c1 = new Component();

		// Component: c2 (parent: c1, included classes: clazz)
		List<Component> subcomponents = new ArrayList<>();
		Component c2 = new Component();
		c2.setClazzes(clazzes);
		subcomponents.add(c2);
		c1.setChildren(subcomponents);

		components.add(c1);
		app.setComponents(components);

		// one CommunicationClazz (in app, source: clazz, target: clazz)
		List<CommunicationClazz> comClazzes = new ArrayList<>();
		CommunicationClazz comClazz = new CommunicationClazz();
		comClazz.setSource(clazz);
		comClazz.setTarget(clazz);
		comClazzes.add(comClazz);
		app.setCommunications(comClazzes);

		return app;
	}

}
