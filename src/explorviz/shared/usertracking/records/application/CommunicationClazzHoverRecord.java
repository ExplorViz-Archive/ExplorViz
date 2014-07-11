package explorviz.shared.usertracking.records.application;

import explorviz.shared.model.helper.CommunicationAppAccumulator;
import explorviz.shared.usertracking.UsertrackingRecord;

public class CommunicationClazzHoverRecord extends UsertrackingRecord {

	private CommunicationAppAccumulator accumulator;

	protected CommunicationClazzHoverRecord() {

	}

	public CommunicationClazzHoverRecord(final CommunicationAppAccumulator accumulator) {
		this.accumulator = accumulator;

	}

	@Override
	public String csvSerialize() {
		return accumulator.getSource().getFullQualifiedName() + UsertrackingRecord.CSV_SEPERATOR
				+ accumulator.getTarget().getFullQualifiedName();
	}
}
