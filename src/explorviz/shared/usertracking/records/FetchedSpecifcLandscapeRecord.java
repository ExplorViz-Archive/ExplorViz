package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class FetchedSpecifcLandscapeRecord extends UsertrackingRecord {
	private String timestamp = "";

	protected FetchedSpecifcLandscapeRecord() {
	}

	public FetchedSpecifcLandscapeRecord(final String timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String csvSerialize() {
		return timestamp;
	}
}
