package explorviz.server.monitoring.writer;

public interface IGWTRecordWriter {
	public void writeBeforeRecord(long timestamp, long traceId, int orderId, String clazzname, String method);
	
	public void writeAfterRecord(long timestamp, long traceId, int orderId,String clazzname, String method);
}
