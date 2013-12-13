package explorviz.shared.usertracking.records.landscape;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.usertracking.UsertrackingRecord;
import explorviz.visualization.model.ApplicationClientSide;
import explorviz.visualization.model.NodeClientSide;

public class NodeRecord extends UsertrackingRecord {
	private String ipAddress;
	private List<String> applicationNameAndIds = new ArrayList<String>();

	protected NodeRecord() {
	}

	public NodeRecord(final NodeClientSide node) {
		setIpAddress(node.getIpAddress());

		for (final ApplicationClientSide a : node.getApplications()) {
			getApplicationNameAndIds().add(a.getName() + "-" + a.getId());
		}
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public List<String> getApplicationNameAndIds() {
		return applicationNameAndIds;
	}

	public void setApplicationNameAndIds(final List<String> applicationNameAndIds) {
		this.applicationNameAndIds = applicationNameAndIds;
	}

	@Override
	public String csvSerialize() {
		String str = getIpAddress() + UsertrackingRecord.CSV_SEPERATOR;
		str += "[";
		for (final String s : getApplicationNameAndIds()) {
			str += s + ",";
		}

		str = str.substring(0, str.length() - 1) + "]";
		return str;
	}

}
