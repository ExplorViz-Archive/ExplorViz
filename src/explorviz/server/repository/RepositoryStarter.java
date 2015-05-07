package explorviz.server.repository;

import explorviz.live_trace_processing.Constants;
import explorviz.live_trace_processing.configuration.Configuration;
import explorviz.live_trace_processing.configuration.ConfigurationFactory;
import explorviz.live_trace_processing.filter.SinglePipeConnector;
import explorviz.live_trace_processing.filter.counting.RecordCountingFilter;
import explorviz.live_trace_processing.main.FilterConfiguration;
import explorviz.live_trace_processing.record.IRecord;

public class RepositoryStarter {
	public void start(final LandscapeRepositoryModel model) {
		final SinglePipeConnector<IRecord> modelConnector = new SinglePipeConnector<IRecord>(64);

		new LandscapeRepositorySink(modelConnector, model).start();

		final SinglePipeConnector<IRecord> recordCountingConnector = new SinglePipeConnector<IRecord>(
				Constants.TRACE_SUMMARIZATION_DISRUPTOR_SIZE);
		new RecordCountingFilter(recordCountingConnector, modelConnector.registerProducer())
				.start();

		final Configuration configuration = ConfigurationFactory.createSingletonConfiguration();
		FilterConfiguration.configureAndStartFilters(configuration,
				recordCountingConnector.registerProducer());
	}
}
