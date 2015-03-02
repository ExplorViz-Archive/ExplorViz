package explorviz.plugin_server.capacitymanagement.execution;

import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

/**
 * @author jgi At first the nodes we work on need to get locked. Next we
 *         terminate the application we work on and copy it from the source node
 *         to the target node. Then we start the application on the target node
 *         and set all necessary attributes. At last we remove the source
 *         application from the source nodes application list and unlock the
 *         nodes.
 */
public class ApplicationMigrateAction extends ExecutionAction {

	private final Application application;
	private final Node sourceNode;
	private final String name;
	private final String ipParent;
	private final String ipTarget;
	private final Node targetNode;

	public ApplicationMigrateAction(final Application app, final Node target) {
		application = app;
		sourceNode = application.getParent();
		targetNode = target;
		name = application.getName();
		ipParent = sourceNode.getIpAddress();
		ipTarget = targetNode.getIpAddress();
	}

	@Override
	protected GenericModelElement getActionObject() {
		return application;
	}

	@Override
	protected SyncObject synchronizeOn() {
		// Since we are working on application level,
		// only applications need to be synchronized.
		return application;
	}

	@Override
	protected void beforeAction() {
		// Locking both the source and the target node.
		// Does this lock all access but the migration action?
		lockingNodeForApplications(sourceNode);
		lockingNodeForApplications(targetNode);
	}

	@Override
	protected boolean concreteAction(final ICloudController controller,
			ScalingGroupRepository repository) throws Exception {
		// ScalingGroup is needed to work with the application.
		String scalinggroupName = application.getScalinggroupName();
		ScalingGroup scalingGroup = repository.getScalingGroupByName(scalinggroupName);
		// Run migrateApplication on OpenStackCloudController.
		return controller.migrateApplication(application, targetNode, scalingGroup);
	}

	@Override
	protected void afterAction() {
		// Remove application from source nodes application list.
		sourceNode.removeApplication(application.getId());
		// Add application to target nodes application list.
		targetNode.addApplication(application);
	}

	@Override
	protected void finallyDo() {
		// Unlock the locked nodes.
		unlockingNodeForApplications(sourceNode);
		unlockingNodeForApplications(targetNode);
	}

	@Override
	protected String getLoggingDescription() {
		return "migrating application " + application.getName() + " to node "
				+ targetNode.getName() + "with IP: " + targetNode.getIpAddress();
	}

	@Override
	protected ExecutionAction getCompensateAction() {

		return new ApplicationMigrateAction(application, sourceNode);
	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository) {
		// If the migration worked, but some other operations failed run
		// compensate.
		// The old application needs to be restarted and the new one to be
		// terminated.
		/*
		 * if (controller.checkApplicationIsRunning(ipTarget,
		 * application.getPid(), name)) { String scalinggroupName =
		 * application.getScalinggroupName(); ScalingGroup scalinggroup =
		 * repository.getScalingGroupByName(scalinggroupName);
		 * scalinggroup.removeApplication(application);
		 * CapManRealityMapper.removeApplicationFromNode(ipParent, name); }
		 */
	}
}
