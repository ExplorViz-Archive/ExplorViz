package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class CameraZoomedInRecord extends UsertrackingRecord {
	private Float newCameraZ = 0f;

	protected CameraZoomedInRecord() {
	}

	public CameraZoomedInRecord(final float newCameraZ) {
		this.newCameraZ = newCameraZ;
	}

	@Override
	public String csvSerialize() {
		return newCameraZ.toString();
	}
}
