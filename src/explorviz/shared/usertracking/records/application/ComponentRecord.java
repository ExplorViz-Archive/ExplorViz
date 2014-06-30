package explorviz.shared.usertracking.records.application;

import explorviz.shared.model.Component;
import explorviz.shared.usertracking.UsertrackingRecord;

public class ComponentRecord extends UsertrackingRecord {
	private String name;

	protected ComponentRecord() {
	}

	public ComponentRecord(final Component compo) {
		setName(compo.getName());
	}

	@Override
	public String csvSerialize() {
		return getName();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
}
