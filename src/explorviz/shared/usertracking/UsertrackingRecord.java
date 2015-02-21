package explorviz.shared.usertracking;

import com.google.gwt.user.client.rpc.IsSerializable;

import explorviz.visualization.services.AuthorizationService;

/**
 * @author Maria Kosche
 *
 */
public abstract class UsertrackingRecord implements IsSerializable {

	public static final String CSV_SEPERATOR = ";";

	private long timestamp = System.currentTimeMillis();
	private String userName;

	public UsertrackingRecord() {
		setUserName(AuthorizationService.getCurrentUsername());
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public abstract String csvSerialize();
}