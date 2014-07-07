package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class CameraMovedYRecord extends UsertrackingRecord {
	private Float newY = 0f;

	protected CameraMovedYRecord() {
	}

	public CameraMovedYRecord(final float newY) {
		this.newY = newY;
	}

	@Override
	public String csvSerialize() {
		return newY.toString();
	}
}
