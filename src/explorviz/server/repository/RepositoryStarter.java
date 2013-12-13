package explorviz.server.repository;

import explorviz.live_trace_processing.configuration.Configuration;
import explorviz.live_trace_processing.configuration.ConfigurationFactory;
import explorviz.live_trace_processing.filter.ITraceSink;
import explorviz.live_trace_processing.main.FilterConfiguration;

public class RepositoryStarter {
	public void start(final LandscapeRepositoryModel model) {
		final ITraceSink sink = new LandscapeRepositorySink(model);

		final Configuration configuration = ConfigurationFactory.createSingletonConfiguration();
		FilterConfiguration.configureAndStartFilters(configuration, sink);
	}
}
