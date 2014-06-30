package explorviz.shared.usertracking.records.landscape;

import explorviz.shared.model.Application;
import explorviz.shared.usertracking.UsertrackingRecord;

public class ApplicationRecord extends UsertrackingRecord {
	private int id;
	private String name;

	protected ApplicationRecord() {
	}

	public ApplicationRecord(final Application app) {
		id = app.getId();
		name = app.getName();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String csvSerialize() {
		return getName() + UsertrackingRecord.CSV_SEPERATOR + getId();
	}
}
