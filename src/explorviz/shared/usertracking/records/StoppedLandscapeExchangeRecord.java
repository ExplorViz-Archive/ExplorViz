package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class StoppedLandscapeExchangeRecord extends UsertrackingRecord {
	private String timestamp = "";

	protected StoppedLandscapeExchangeRecord() {
	}

	public StoppedLandscapeExchangeRecord(final String timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String csvSerialize() {
		return timestamp;
	}
}
