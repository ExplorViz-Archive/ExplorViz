package explorviz.plugin.capacitymanagement.cloud_control;

import explorviz.plugin.capacitymanagement.node.repository.Node;
import explorviz.plugin.capacitymanagement.node.repository.ScalingGroup;

public interface ICloudController {

	/**
	 * @param scalingGroup
	 *            Arraylist of Nodes in Landscape.
	 * @return null or started node (within ScalingGroup)
	 * @throws Exception
	 *             If thrown shutdown Node and write error into Log.
	 */
	Node startNode(ScalingGroup scalingGroup) throws Exception;

	/**
	 * @param node
	 *            Nodeobject from Landscape.
	 */
	void shutdownNode(Node node);
}
