package explorviz.shared.usertracking.records.application;

import explorviz.shared.usertracking.UsertrackingRecord;
import explorviz.visualization.model.ClazzClientSide;

public class ClazzRecord extends UsertrackingRecord {
	private String name;

	protected ClazzRecord() {
	}

	public ClazzRecord(final ClazzClientSide clazz) {
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
