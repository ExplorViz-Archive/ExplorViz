package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class CameraMovedLeftRecord extends UsertrackingRecord {
	private Float newCameraX = 0f;

	protected CameraMovedLeftRecord() {
	}

	public CameraMovedLeftRecord(final float newCameraX) {
		this.newCameraX = newCameraX;
	}

	@Override
	public String csvSerialize() {
		return newCameraX.toString();
	}
}
