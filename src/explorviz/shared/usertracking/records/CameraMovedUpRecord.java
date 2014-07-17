package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class CameraMovedUpRecord extends UsertrackingRecord {
	private Float newCameraY = 0f;

	protected CameraMovedUpRecord() {
	}

	public CameraMovedUpRecord(final float newCameraY) {
		this.newCameraY = newCameraY;
	}

	@Override
	public String csvSerialize() {
		return newCameraY.toString();
	}
}
