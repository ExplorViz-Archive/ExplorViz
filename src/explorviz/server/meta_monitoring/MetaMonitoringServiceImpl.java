package explorviz.server.meta_monitoring;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.visualization.meta_monitoring.MetaMonitoringService;

public class MetaMonitoringServiceImpl extends RemoteServiceServlet implements
		MetaMonitoringService {

	private static final long serialVersionUID = -1749714560477424650L;

	@Override
	public void sendRecordBundle(final String recordBundle) {
		final String[] splitRecords = recordBundle.split(",");
		for (final String record : splitRecords) {
			if (record.contains(";")) {
				// BEFORE record
				final String[] beforeRecordSplit = record.split(";");
				String clazzName = beforeRecordSplit[0].substring(0,
						beforeRecordSplit[0].lastIndexOf(".") - 1);
				clazzName = clazzName.replace("/", ".");
			} else {
				// AFTER record
			}
		}

		System.out.println(splitRecords.length);
	}

}
