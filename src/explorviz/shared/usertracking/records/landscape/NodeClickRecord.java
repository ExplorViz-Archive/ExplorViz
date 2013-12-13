package explorviz.shared.usertracking.records.landscape;

import explorviz.visualization.model.NodeClientSide;

public class NodeClickRecord extends NodeRecord {

	protected NodeClickRecord() {
	}

	public NodeClickRecord(final NodeClientSide node) {
		super(node);
	}
}
