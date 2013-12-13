package explorviz.shared.usertracking.records.codeviewer;

import explorviz.shared.usertracking.UsertrackingRecord;

public class CodeviewerRecord extends UsertrackingRecord {
	private String project;
	private String filepath;
	private String filename;

	protected CodeviewerRecord() {
	}

	public CodeviewerRecord(final String project, final String filepath, final String filename) {
		this.project = project;
		this.filepath = filepath;
		this.filename = filename;
	}

	public String getProject() {
		return project;
	}

	public void setProject(final String project) {
		this.project = project;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(final String filepath) {
		this.filepath = filepath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	@Override
	public String csvSerialize() {
		return getProject() + UsertrackingRecord.CSV_SEPERATOR + getFilepath()
				+ UsertrackingRecord.CSV_SEPERATOR + getFilename();
	}

}
