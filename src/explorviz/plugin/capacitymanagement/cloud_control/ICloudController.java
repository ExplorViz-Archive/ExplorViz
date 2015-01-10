package explorviz.plugin.capacitymanagement.cloud_control;

import explorviz.shared.model.*;

public interface ICloudController {

	/**
	 * @param scalingGroup
	 *            Arraylist of Nodes in Landscape.
	 * @return null or started node (within NodeGroup)
	 * @throws Exception
	 *             If thrown shutdown Node and write error into Log.
	 */
	Node startNode(NodeGroup nodegroup) throws Exception;

	/**
	 * @param node
	 *            Nodeobject from Landscape.
	 */
	void shutdownNode(Node node);

	void restartNode(Node node);

	void restartApplication(Application application);

	void terminateApplication(Application application);

}
