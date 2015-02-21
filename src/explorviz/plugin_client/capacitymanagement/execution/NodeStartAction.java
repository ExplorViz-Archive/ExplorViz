package explorviz.plugin_client.capacitymanagement.execution;

import java.util.List;

import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.shared.model.*;
import explorviz.shared.model.helper.GenericModelElement;

public class NodeStartAction extends ExecutionAction {

	private NodeGroup parent;
	private Node newNode;

	public NodeStartAction(String hostname, String flavor, String image, List<Application> apps,
			NodeGroup parent) {

		newNode = new Node();
		newNode.setApplications(apps);
		newNode.setHostname(hostname);
		newNode.setImage(image);
		newNode.setFlavor(flavor);

		this.parent = parent;
	}

	@Override
	protected GenericModelElement getActionObject() {

		return newNode;
	}

	@Override
	protected SyncObject synchronizeOn() {
		return newNode;
	}

	@Override
	protected void beforeAction() {

	}

	@Override
	protected boolean concreteAction(ICloudController controller) throws Exception {
		String ipAdress = controller.startNode(parent, newNode);
		if (ipAdress == "null") {
			state = ExecutionActionState.ABORTED;
			return false;
		} else {
			newNode.setIpAddress(ipAdress);
			// TODO jek: Wollen wir das ins Interface aufnehmen, um es nicht
			// unn�tig oft neu auslesen zu m�ssen?
			// newNode.setId(controller.retrieveIdFromNode(newNode));
			return true;
		}
	}

	@Override
	protected void afterAction() {
		newNode.setParent(parent);
		for (Application app : newNode.getApplications()) {
			ScalingGroup scalinggroup = app.getScalinggroup();
			LoadBalancersFacade.addApplication(app.getId(), newNode.getIpAddress(),
					scalinggroup.getName());
		}
	}

	@Override
	protected void finallyDo() {

	}

	@Override
	protected String getLoggingDescription() {
		return "starting node: " + newNode.getHostname();
	}

	@Override
	protected ExecutionAction getCompensateAction() {

		return new NodeTerminateAction(newNode);
	}

	@Override
	protected boolean checkBeforeAction(ICloudController controller) {
		return (ExecutionOrganizer.maxRunningNodesLimit > controller.retrieveRunningNodeCount());

	}
}
