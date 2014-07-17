package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class CameraZoomedOutRecord extends UsertrackingRecord {
	private Float newCameraZ = 0f;

	protected CameraZoomedOutRecord() {
	}

	public CameraZoomedOutRecord(final float newCameraZ) {
		this.newCameraZ = newCameraZ;
	}

	@Override
	public String csvSerialize() {
		return newCameraZ.toString();
	}
}
