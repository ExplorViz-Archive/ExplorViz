package explorviz.shared.usertracking.records.landscape;

import explorviz.shared.model.System;
import explorviz.shared.usertracking.UsertrackingRecord;

public class SystemRecord extends UsertrackingRecord {
	private String systemName;

	protected SystemRecord() {
	}

	public SystemRecord(final System system) {
		systemName = system.getName();
	}

	@Override
	public String csvSerialize() {
		return systemName;
	}

}
