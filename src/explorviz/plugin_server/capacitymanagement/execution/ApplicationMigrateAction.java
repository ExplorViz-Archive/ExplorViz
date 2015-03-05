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
	private final String ipSource;
	private final String ipTarget;
	private final Node targetNode;

	public ApplicationMigrateAction(final Application app, final Node target) {
		application = app;
		sourceNode = application.getParent();
		targetNode = target;
		name = application.getName();
		ipSource = sourceNode.getIpAddress();
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

		// Lock the sourceNode so the application data can't be changed.
		// Does this lock all access but the migration action?
		lockingNodeForApplications(sourceNode);
	}

	@Override
	protected boolean concreteAction(final ICloudController controller,
			ScalingGroupRepository repository) throws Exception {
		// Check if targetNode is the sourceNode. In this case no action is
		// needed.
		if (targetNode.getId().equals(sourceNode.getId())) {
			return true;
		}
		// Check if targetNode is not under heavy load.
		// Value may need adjustment. Is CpuUtil useful for this purpose?
		if (targetNode.getCpuUtilization() > 0.9) {
			return false;
		}
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
		// Unlock the locked node when the migration is finished.
		unlockingNodeForApplications(sourceNode);
	}

	@Override
	protected String getLoggingDescription() {
		return "Migrating application " + application.getName() + " to node "
				+ targetNode.getName() + "with IP: " + targetNode.getIpAddress() + ".";
	}

	@Override
	protected ExecutionAction getCompensateAction() {

		return new ApplicationMigrateAction(application, sourceNode);
	}

	@Override
	protected void compensate(ICloudController controller, ScalingGroupRepository repository) {
		// If the migration failed run a rollback.
		// The old application needs to be restarted and the new one to be
		// terminated.
		Node currentParent = application.getParent();
		String scalinggroupName = application.getScalinggroupName();
		ScalingGroup scalingGroup = repository.getScalingGroupByName(scalinggroupName);

		if (currentParent.equals(targetNode)) {
			if (!controller.checkApplicationIsRunning(ipTarget, application.getPid(), name)) {
				application.setParent(sourceNode);
				try {
					String oldPid = controller.startApplication(application, scalingGroup);
					application.setPid(oldPid);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			if (!controller.checkApplicationIsRunning(ipSource, application.getPid(), name)) {
				try {
					String oldPid = controller.startApplication(application, scalingGroup);
					application.setPid(oldPid);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}
}
