package explorviz.visualization.engine.usertracking;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.usertracking.UsertrackingRecord;

/**
 * @author Maria Kosche
 *
 */
@RemoteServiceRelativePath("usertrackingrecord")
public interface UsertrackingRecordService extends RemoteService {
	boolean putUsertrackingRecord(UsertrackingRecord record);
}
