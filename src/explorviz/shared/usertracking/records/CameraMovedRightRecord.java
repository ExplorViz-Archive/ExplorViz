package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class CameraMovedRightRecord extends UsertrackingRecord {
	private Float newCameraX = 0f;

	protected CameraMovedRightRecord() {
	}

	public CameraMovedRightRecord(final float newCameraX) {
		this.newCameraX = newCameraX;
	}

	@Override
	public String csvSerialize() {
		return newCameraX.toString();
	}
}
