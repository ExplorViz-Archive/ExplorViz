package explorviz.shared.usertracking.records.codeviewer;

public class CodeviewerOpenFileRecord extends CodeviewerRecord {
	protected CodeviewerOpenFileRecord() {
	}

	public CodeviewerOpenFileRecord(final String project, final String filepath,
			final String filename) {
		super(project, filepath, filename);
	}

	@Override
	public String csvSerialize() {
		return super.csvSerialize();
	}
}
