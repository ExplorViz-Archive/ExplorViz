package explorviz.shared.usertracking.records.application;

import explorviz.shared.model.Clazz;
import explorviz.shared.usertracking.UsertrackingRecord;

public class ClazzRecord extends UsertrackingRecord {
	private String name;

	protected ClazzRecord() {
	}

	public ClazzRecord(final Clazz clazz) {
		setName(clazz.getName());
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String csvSerialize() {
		return getName();
	}

}
