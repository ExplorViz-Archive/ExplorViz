package explorviz.shared.usertracking.records.landscape;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.usertracking.UsertrackingRecord;
import explorviz.visualization.model.NodeClientSide;
import explorviz.visualization.model.NodeGroupClientSide;

public class NodeGroupRecord extends UsertrackingRecord {

	private List<String> nodeIps = new ArrayList<String>();

	protected NodeGroupRecord() {
	}

	public NodeGroupRecord(final NodeGroupClientSide nodeGroup) {
		for (final NodeClientSide n : nodeGroup.getNodes()) {
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
