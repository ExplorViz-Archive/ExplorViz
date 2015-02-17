package explorviz.plugin_client.capacitymanagement.execution;

import java.util.List;

import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.LoadBalancersFacade;
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
		// TODO:worauf hier synchronisieren?
		return newNode;
	}

	@Override
	protected void beforeAction() {

	}

	@Override
	protected boolean concreteAction(ICloudController controller) throws Exception {
		Node startedNode = controller.startNode(parent, newNode);
		if (startedNode == null) {
			state = ExecutionActionState.ABORTED;
			return false;
		} else {
			return true;
		}
	}

	// TODO: jek/jkr: hat newNode schon ip? was ist die Appl.-ID nach dem Worker
	// in der LandschafT?
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

	// TODO: jek/jkr: NodeStartAction is used for initial setup --> compensate
	// action = TerminateNode?
	@Override
	protected ExecutionAction getCompensateAction() {
		// StartNode itself is just thought as a compensate of terminate, so its
		// compensateAction would never be called
		return null;
	}

}
