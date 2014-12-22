package explorviz.plugin.capacitymanagement.cpu_utilization.reader;

import com.lmax.disruptor.EventFactory;

import explorviz.live_trace_processing.record.IRecord;
import explorviz.live_trace_processing.record.trace.HostApplicationMetaDataRecord;

/**
 * WARNING: This is a mutable object which will be recycled by the RingBuffer.
 * You must take a copy of data it holds before the framework recycles it.
 */
public final class RecordEvent {
	private IRecord value;
	private HostApplicationMetaDataRecord metadata;

	public final IRecord getValue() {
		return value;
	}

	public void setValue(final IRecord value) {
		this.value = value;
	}

	public HostApplicationMetaDataRecord getMetadata() {
		return metadata;
	}

	public void setMetadata(final HostApplicationMetaDataRecord metadata) {
		this.metadata = metadata;
	}

	public final static EventFactory<RecordEvent> EVENT_FACTORY = new EventFactory<RecordEvent>() {
		@Override
		public RecordEvent newInstance() {
			return new RecordEvent();
		}
	};
}