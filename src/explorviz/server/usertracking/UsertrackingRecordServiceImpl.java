package explorviz.server.usertracking;

import java.io.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.main.FileSystemHelper;
import explorviz.shared.usertracking.UsertrackingRecord;
import explorviz.visualization.engine.usertracking.UsertrackingRecordService;

public class UsertrackingRecordServiceImpl extends RemoteServiceServlet implements
		UsertrackingRecordService {
	private static final long serialVersionUID = 2022679088968123510L;

	@Override
	public boolean putUsertrackingRecord(final UsertrackingRecord record) {
		final String csvSerializedRecord = record.getClass().getSimpleName()
				+ UsertrackingRecord.CSV_SEPERATOR + record.getTimestamp()
				+ UsertrackingRecord.CSV_SEPERATOR + record.csvSerialize() + "\n";

		return writeCSVSerializedRecordToLogFile(csvSerializedRecord, record.getUserName());
	}

	public boolean writeCSVSerializedRecordToLogFile(final String csvSerializedRecord,
			final String username) {
		FileOutputStream logFileStream = null;

		try {
			logFileStream = new FileOutputStream(new File(FileSystemHelper.getExplorVizDirectory()
					+ "/" + username + "_tracking.log"), true);

			logFileStream.write(csvSerializedRecord.getBytes("UTF-8"));
			logFileStream.flush();
			logFileStream.close();
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}