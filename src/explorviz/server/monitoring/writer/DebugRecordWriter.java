package explorviz.server.monitoring.writer;

public class DebugRecordWriter implements IGWTRecordWriter {

	@Override
	public void writeBeforeRecord(long timestamp, long traceId, int orderId,
			String clazzname, String method) {
		System.out.println(timestamp + " " + traceId + " " + orderId + " " + clazzname + " " + method);
	}

	@Override
	public void writeAfterRecord(long timestamp, long traceId, int orderId, String clazzname, String method) {
		System.out.println(timestamp + " " + traceId + " " + orderId+ " " + clazzname + " " + method);
	}

}
