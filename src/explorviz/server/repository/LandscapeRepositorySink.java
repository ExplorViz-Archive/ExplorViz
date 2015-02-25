package explorviz.server.repository;

import explorviz.live_trace_processing.filter.AbstractSink;
import explorviz.live_trace_processing.filter.ITraceSink;
import explorviz.live_trace_processing.record.IRecord;

public final class LandscapeRepositorySink extends AbstractSink implements ITraceSink {
	private final LandscapeRepositoryModel model;

	public LandscapeRepositorySink(final LandscapeRepositoryModel model) {
		super();
		this.model = model;
	}

	@Override
	protected void processRecord(final IRecord record) {
		model.insertIntoModel(record);
	}
}
