package explorviz.server.repository;

import java.util.Queue;

import explorviz.live_trace_processing.configuration.Configuration;
import explorviz.live_trace_processing.configuration.ConfigurationFactory;
import explorviz.live_trace_processing.filter.RecordArrayEvent;
import explorviz.live_trace_processing.filter.SinglePipeConnector;
import explorviz.live_trace_processing.main.FilterConfiguration;

public class RepositoryStarter {
	public void start(final LandscapeRepositoryModel model) {
		final SinglePipeConnector<RecordArrayEvent> modelConnector = new SinglePipeConnector<RecordArrayEvent>(
				16);

		new LandscapeRepositorySink(modelConnector, model).start();

		final Queue<RecordArrayEvent> sink = modelConnector.registerProducer();

		final Configuration configuration = ConfigurationFactory.createSingletonConfiguration();
		FilterConfiguration.configureAndStartFilters(configuration, sink);
	}
}
