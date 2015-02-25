package explorviz.visualization.engine.usertracking;

import com.google.gwt.core.shared.GWT;

import explorviz.shared.usertracking.UsertrackingRecord;

public class UsertrackingService {

	private static UsertrackingRecordServiceAsync usertrackingRecordSvc;
	private static UsertrackingRecordCallback<Boolean> callback;

	static {
		usertrackingRecordSvc = GWT.create(UsertrackingRecordService.class);
		callback = new UsertrackingRecordCallback<Boolean>();
	}

	public static void putUsertrackingRecord(final UsertrackingRecord record) {
		usertrackingRecordSvc.putUsertrackingRecord(record, callback);
	}

}