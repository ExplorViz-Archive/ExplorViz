package explorviz.plugin.capacitymanagement.scaling_strategies;

import explorviz.plugin.capacitymanagement.node.repository.Node;
import explorviz.plugin.capacitymanagement.node.repository.ScalingGroup;

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
	void startNode(ScalingGroup scalingGroup);

	/**
	 * Shuts down node
	 * 
	 * @param node
	 *            node to shutdown
	 */
	void shutDownNode(Node node);
}
