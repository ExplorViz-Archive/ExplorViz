package explorviz.plugin.capacitymanagement.cpu_utilization.reader;

import explorviz.live_trace_processing.record.misc.StringRegistryRecord;
import explorviz.live_trace_processing.writer.IStringRecordSender;

public class StringRegistrySenderDummy implements IStringRecordSender {

	@Override
	public void sendOutStringRecord(final StringRegistryRecord record) {
	}

}
