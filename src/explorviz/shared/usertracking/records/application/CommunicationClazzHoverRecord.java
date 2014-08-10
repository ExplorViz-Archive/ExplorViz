package explorviz.shared.usertracking.records.application;

import explorviz.shared.model.helper.CommunicationAppAccumulator;
import explorviz.shared.usertracking.UsertrackingRecord;

public class CommunicationClazzHoverRecord extends UsertrackingRecord {

	private String source;
	private String target;

	protected CommunicationClazzHoverRecord() {

	}

	public CommunicationClazzHoverRecord(final CommunicationAppAccumulator accumulator) {
		source = accumulator.getSource().getFullQualifiedName();
		target = accumulator.getTarget().getFullQualifiedName();

	}

	@Override
	public String csvSerialize() {
		return source + UsertrackingRecord.CSV_SEPERATOR + target;
	}
}
