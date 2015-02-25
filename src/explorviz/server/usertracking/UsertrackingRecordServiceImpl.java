package explorviz.server.usertracking;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.main.FileSystemHelper;
import explorviz.shared.usertracking.UsertrackingRecord;
import explorviz.visualization.engine.usertracking.UsertrackingRecordService;

public class UsertrackingRecordServiceImpl extends RemoteServiceServlet implements
		UsertrackingRecordService {
	private static final long serialVersionUID = 2022679088968123510L;
	private static final Map<String, FileOutputStream> openFileHandlesPerUser = new HashMap<String, FileOutputStream>();

	@Override
	public boolean putUsertrackingRecord(final UsertrackingRecord record) {
		final String csvSerializedRecord = record.getClass().getSimpleName()
				+ UsertrackingRecord.CSV_SEPERATOR + record.getTimestamp()
				+ UsertrackingRecord.CSV_SEPERATOR + record.csvSerialize() + "\n";

		return writeCSVSerializedRecordToLogFile(csvSerializedRecord, record.getUserName());
	}

	public boolean writeCSVSerializedRecordToLogFile(final String csvSerializedRecord,
			final String username) {
		FileOutputStream fileOutputStream = openFileHandlesPerUser.get(username);

		if (fileOutputStream == null) {
			try {
				fileOutputStream = new FileOutputStream(
						new File(FileSystemHelper.getExplorVizDirectory() + "/" + username
								+ "_tracking.log"), true);
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				return false;
			}
			openFileHandlesPerUser.put(username, fileOutputStream);
		}

		try {
			fileOutputStream.write(csvSerializedRecord.getBytes("UTF-8"));
			fileOutputStream.flush();
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}