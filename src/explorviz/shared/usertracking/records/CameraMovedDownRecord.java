package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class CameraMovedDownRecord extends UsertrackingRecord {
	private Float newCameraY = 0f;

	protected CameraMovedDownRecord() {
	}

	public CameraMovedDownRecord(final float newCameraY) {
		this.newCameraY = newCameraY;
	}

	@Override
	public String csvSerialize() {
		return newCameraY.toString();
	}
}
