package explorviz.shared.usertracking.records.landscape;

import explorviz.shared.usertracking.UsertrackingRecord;
import explorviz.visualization.model.ApplicationClientSide;

public class ApplicationRecord extends UsertrackingRecord {
	private int id;
	private String name;

	protected ApplicationRecord() {
	}

	public ApplicationRecord(final ApplicationClientSide app) {
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
