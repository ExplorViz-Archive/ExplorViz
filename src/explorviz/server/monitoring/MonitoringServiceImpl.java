package explorviz.server.monitoring;

import java.util.concurrent.ConcurrentHashMap;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.monitoring.writer.IGWTRecordWriter;
import explorviz.server.monitoring.writer.KiekerRecordWriter;
import explorviz.visualization.monitoring.MonitoringService;

public class MonitoringServiceImpl extends RemoteServiceServlet implements MonitoringService {

	private static final long serialVersionUID = -1474770740583197159L;

	IGWTRecordWriter writer = new KiekerRecordWriter();

	private static ConcurrentHashMap<Integer, String> stringRegistry = new ConcurrentHashMap<Integer, String>();

	@Override
	public void sendRecordBundle(final String recordBundle) {

		final String[] splitRecords = recordBundle.split(",");
		for (final String record : splitRecords) {
			final String[] splitRecord = record.split(";");

			// TODO always throw away trace 0?

			final String recordId = splitRecord[0];

			if (recordId.equals("1")) { // BEFORE record
				writer.writeBeforeRecord(convertTimestampToNano(splitRecord[1]),
						Long.parseLong(splitRecord[2]), Integer.parseInt(splitRecord[3]),
						convertClassname(splitRecord[4]), convertMethod(splitRecord[5]) + "()");
			} else if (recordId.equals("3")) { // AFTER record
				writer.writeAfterRecord(convertTimestampToNano(splitRecord[1]),
						Long.parseLong(splitRecord[2]), Integer.parseInt(splitRecord[3]),
						convertClassname(splitRecord[4]), convertMethod(splitRecord[5]) + "()");
			} else if (recordId.equals("4")) { // String record
				stringRegistry.put(Integer.parseInt(splitRecord[1]), splitRecord[2]);
			}
		}
	}

	private final long convertTimestampToNano(final String timestampInMsec) {
		final double timestampInDouble = Double.parseDouble(timestampInMsec);
		return Math.round(timestampInDouble * 1000 * 1000);
	}

	private final String convertClassname(final String clazzFilename) {
		return stringRegistry.get(Integer.parseInt(clazzFilename));
	}

	private final String convertMethod(final String method) {
		String methodname = stringRegistry.get(Integer.parseInt(method));

		if (methodname.lastIndexOf("_") >= 0) {
			methodname = methodname.substring(0, methodname.lastIndexOf("_"));
		} else {
			return methodname;
		}

		if (methodname.lastIndexOf("_") >= 0) {
			return methodname.substring(0, methodname.lastIndexOf("_"));
		} else {
			return methodname;
		}
	}

}
