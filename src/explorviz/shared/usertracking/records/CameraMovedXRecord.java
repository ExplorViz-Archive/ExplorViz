package explorviz.shared.usertracking.records;

import explorviz.shared.usertracking.UsertrackingRecord;

public class CameraMovedXRecord extends UsertrackingRecord {
	private Float newX = 0f;

	protected CameraMovedXRecord() {
	}

	public CameraMovedXRecord(final float newX) {
		this.newX = newX;
	}

	@Override
	public String csvSerialize() {
		return newX.toString();
	}
}
