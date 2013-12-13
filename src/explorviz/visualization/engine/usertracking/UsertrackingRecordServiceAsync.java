package explorviz.visualization.engine.usertracking;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.usertracking.UsertrackingRecord;

public interface UsertrackingRecordServiceAsync {
	void putUsertrackingRecord(UsertrackingRecord record, AsyncCallback<Boolean> callback);
}
