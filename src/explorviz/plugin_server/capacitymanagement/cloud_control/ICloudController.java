package explorviz.plugin_server.capacitymanagement.cloud_control;

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

	Node cloneNode(NodeGroup nodegroup, Node originalNode);

	/**
	 * @param node
	 *            Nodeobject from Landscape.
	 */

	boolean shutdownNode(Node node);

	boolean restartNode(Node node);

	boolean restartApplication(Application application);

	boolean terminateApplication(Application application);

	boolean migrateApplication(Application application, Node node);

}
