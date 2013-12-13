package explorviz.server.usertracking;

import java.io.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.shared.usertracking.UsertrackingRecord;
import explorviz.visualization.engine.usertracking.UsertrackingRecordService;

public class UsertrackingRecordServiceImpl extends RemoteServiceServlet implements
		UsertrackingRecordService {
	private static final long serialVersionUID = 2022679088968123510L;

	private static final String LOG_FILENAME = "Usertracking.log";

	private static FileOutputStream logFileStream;

	static {
		try {
			logFileStream = new FileOutputStream(new File(LOG_FILENAME), true);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean putUsertrackingRecord(final UsertrackingRecord record) {
		final String csvSerializedRecord = record.getClass().getSimpleName()
				+ UsertrackingRecord.CSV_SEPERATOR + record.getTimestamp()
				+ UsertrackingRecord.CSV_SEPERATOR + record.getUserName()
				+ UsertrackingRecord.CSV_SEPERATOR + record.csvSerialize() + "\n";

		return writeCSVSerializedRecordToLogFile(csvSerializedRecord);
	}

	public boolean writeCSVSerializedRecordToLogFile(final String csvSerializedRecord) {
		synchronized (this) {
			try {
				logFileStream.write(csvSerializedRecord.getBytes("UTF-8"));
				logFileStream.flush();
			} catch (final IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}