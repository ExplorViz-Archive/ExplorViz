package explorviz.server.monitoring.writer;

import kieker.common.record.flow.trace.operation.AfterOperationEvent;
import kieker.common.record.flow.trace.operation.BeforeOperationEvent;
import kieker.monitoring.core.controller.MonitoringController;

public class KiekerRecordWriter implements IGWTRecordWriter {

	@Override
	public void writeBeforeRecord(long timestamp, long traceId, int orderId,
			String clazzname, String method) {
		MonitoringController.getInstance().newMonitoringRecord(new BeforeOperationEvent(timestamp, traceId, orderId, method, clazzname));
	}

	@Override
	public void writeAfterRecord(long timestamp, long traceId, int orderId,
			String clazzname, String method) {
		MonitoringController.getInstance().newMonitoringRecord(new AfterOperationEvent(timestamp, traceId, orderId, method, clazzname));
		
	}

}
