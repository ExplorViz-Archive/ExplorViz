package explorviz.shared.usertracking.records.landscape;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;
import explorviz.shared.usertracking.UsertrackingRecord;

public class NodeGroupRecord extends UsertrackingRecord {

	private List<String> nodeIps = new ArrayList<String>();

	protected NodeGroupRecord() {
	}

	public NodeGroupRecord(final NodeGroup nodeGroup) {
		for (final Node n : nodeGroup.getNodes()) {
			getNodeIps().add(n.getIpAddress());
		}
	}

	public List<String> getNodeIps() {
		return nodeIps;
	}

	public void setNodeIps(final List<String> nodeIps) {
		this.nodeIps = nodeIps;
	}

	@Override
	public String csvSerialize() {
		String str = "[";

		for (final String s : getNodeIps()) {
			str += s + ",";
		}
		str = str.substring(0, str.length() - 1) + "]";

		return str;
	}

}
