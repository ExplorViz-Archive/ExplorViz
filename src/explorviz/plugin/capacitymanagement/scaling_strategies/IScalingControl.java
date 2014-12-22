package explorviz.plugin.capacitymanagement.scaling_strategies;

import explorviz.shared.model.Node;
import explorviz.shared.model.NodeGroup;

/**
 * @author jgi, dtj Interface for starting/shutting down nodes
 */
public interface IScalingControl {
	/**
	 * Starts node/s
	 *
	 * @param scalingGroup
	 *            scalingGroup to start
	 */
	void startNode(NodeGroup nodeGroup);

	/**
	 * Shuts down node
	 *
	 * @param node
	 *            node to shutdown
	 */
	void shutDownNode(Node node);
}
