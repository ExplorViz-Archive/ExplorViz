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
	Node startNode(NodeGroup nodegroup, Node node) throws Exception;

	Node replicateNode(NodeGroup nodegroup, Node originalNode);

	/**
	 * @param node
	 *            Nodeobject from Landscape.
	 */

	boolean terminateNode(Node node) throws Exception;

	boolean restartNode(Node node) throws Exception;

	boolean restartApplication(Application application) throws Exception;

	boolean terminateApplication(Application application) throws Exception;

	boolean migrateApplication(Application application, Node node) throws Exception;

}
