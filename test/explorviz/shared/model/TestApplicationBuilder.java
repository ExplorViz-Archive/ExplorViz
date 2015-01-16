package explorviz.shared.model;

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
		return app;
	}

}
