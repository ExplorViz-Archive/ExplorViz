package explorviz.plugin_server.capacitymanagement.execution;

import java.net.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.capacitymanagement.CapManExecutionStates;
import explorviz.plugin_client.capacitymanagement.execution.SyncObject;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

/**
 *
 * Structure for execution actions. In a thread, the concreteAction() of the
 * subclasses is executed which access through the ICloudController the Cloud.
 *
 * @author jek, jkr
 *
 */
public abstract class ExecutionAction {

	// TODO: doch nur eine Applikation je Knoten bearbeiten

	// TODO: jkr/jek: bei kopierten/neuen Knoten/Applikationen saemtliche
	// Attribute des Originals setzen

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionAction.class);

	protected ExecutionActionState state = ExecutionActionState.INITIAL;

	public ExecutionActionState getState() {
		return state;
	}

	/**
	 *
	 * Executes the concreteAction inside a thread.
	 *
	 * @param controller
	 *            cloud controller
	 * @param group
	 *            thread group of the action list
	 * @param repository
	 *            scalinggroups
	 */
	public void execute(final ICloudController controller, final ThreadGroup group,
			final ScalingGroupRepository repository) {
		if (!checkBeforeAction(controller)) {
			state = ExecutionActionState.REJECTED;
			return;
		}
		new Thread(group, new Runnable() {
			@Override
			public void run() {
				final SyncObject sync = synchronizeOn();
				synchronized (sync) {
					while (sync.isLockedUntilExecutionActionFinished()) {
						try {
							sync.wait();
						} catch (final InterruptedException e) {

						}
					}
					sync.setLockedUntilExecutionActionFinished(true);
					beforeAction();

					boolean success = false;
					try {

						LOGGER.info("Try " + getLoggingDescription());
						for (int i = 0; (success == false)
								&& (i < ExecutionOrganizer.MAX_TRIES_FOR_CLOUD); i++) {
							success = concreteAction(controller, repository);
							LOGGER.info("concrete Action exited with: " + success);
							if (!success) {
								Thread.sleep(100000);
							}
						}
					} catch (ConnectException ce) {
						LOGGER.error("Loadbalancererror while " + getLoggingDescription());
					} catch (final Exception e) {

						LOGGER.info("Error while " + getLoggingDescription());
						LOGGER.error(e.getMessage(), e);
						if (state != ExecutionActionState.REJECTED) {
							state = ExecutionActionState.ABORTED;
						}
					} finally {
						if (success) {

							state = ExecutionActionState.SUCC_FINISHED;
							getActionObject().putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.NONE);
							afterAction();
							LOGGER.info("Action successfully finished: " + getLoggingDescription());
						} else {
							LOGGER.info("Action unsuccessful");

							state = ExecutionActionState.ABORTED;
							compensate(controller, repository);

						}
						finallyDo();
						sync.setLockedUntilExecutionActionFinished(false);
						sync.notify();
					}
				}
			}

		}).start();

	}

	protected abstract GenericModelElement getActionObject();

	/**
	 * Returns synchronize-object on which is synchronized to prevent race
	 * conditions.
	 *
	 * @return object to synchronize on
	 */
	protected abstract SyncObject synchronizeOn();

	/**
	 * Method invoked before concreteAction().
	 */
	protected abstract void beforeAction();

	/**
	 * Concrete action of the subclass (e.g. start Node in the cloud).
	 *
	 * @param controller
	 *            cloud controller
	 * @param repository
	 *            all scalinggroups
	 * @return if action is successfully finished
	 * @throws Exception
	 */
	protected abstract boolean concreteAction(ICloudController controller,
			ScalingGroupRepository repository) throws Exception;

	/**
	 * Method invoked after successful concreteAction().
	 */
	protected abstract void afterAction();

	/**
	 * Method finally invoked, after eventually compensation.
	 */
	protected abstract void finallyDo();

	protected abstract String getLoggingDescription();

	/**
	 * Locking node. No other NodeExecutionAction possible, but other
	 * ApplicationExecutionActions.
	 *
	 * @param parent
	 */
	protected void lockingNodeForApplications(final Node parent) {
		synchronized (parent) {
			// Am I the first Application Action on this node?
			if (parent.readRunningApplications() == 0) {
				// then get lock on node
				while (parent.isLockedUntilExecutionActionFinished()) {
					try {
						parent.wait();
					} catch (final InterruptedException e) {
					}
				}
				parent.setLockedUntilExecutionActionFinished(true);
			}
			parent.incrementRunningApplications();
		}
	}

	/**
	 * Unlocking node if no application needs the lock anymore.
	 *
	 * @param parent
	 */
	protected void unlockingNodeForApplications(final Node parent) {
		synchronized (parent) {
			// Am I the last running ApplicationExecutionAction on this node?
			if (parent.readRunningApplications() == 1) {
				// then release lock
				parent.setLockedUntilExecutionActionFinished(false);
			}
			parent.decrementRunningApplications();
		}
	}

	/**
	 * Check before concreteAction().
	 *
	 * @param controller
	 *            cloudcontroller
	 * @return check result
	 */
	protected boolean checkBeforeAction(ICloudController controller) {
		return true;
	}

	/**
	 * Creates ExecutionAction that reverses the given action. It is assumed,
	 * that this action itself has finished successfully while another action in
	 * the same plan has failed.
	 *
	 * @return Reverse {@link ExecutionAction}.
	 */
	protected abstract ExecutionAction getCompensateAction();

	/**
	 * This method will be executed if this action itself fails. The main
	 * purpose is to ensure that the mapping of the landscape and the
	 * scalingroups are adjusted to the real state of the system. Additionally,
	 * it might be useful/possible to "rollback" this action to the initial
	 * state.
	 *
	 * @param controller
	 *            ICloudcontroller needed for access to the cloud.
	 * @param repository
	 *            Scalinggrouprepository to fetch scalinggroup for applications.
	 * @throws Exception
	 */
	protected abstract void compensate(ICloudController controller,
			ScalingGroupRepository repository);

	/**
	 * Waits for dependent nodes and adds their IP to argument list.
	 *
	 * @param controller
	 * @param app
	 * @throws Exception
	 */
	protected void waitForDependendNodes(ICloudController controller, Application app)
			throws Exception {
		// check if servers which are mandatory already exist.
		for (String dependOn : app.getDependendOn()) {
			if ((dependOn != null) && !dependOn.equals("")) {
				String ip = "";
				for (int i = 0; (i < 5) && ip.equals(""); i++) {
					try {
						ip = controller.retrievePrivateIPFromInstance(dependOn);
						Thread.sleep(10000);
					} catch (Exception e) {
						LOGGER.info("IP of " + dependOn + " was " + ip);
						// try again to get IP
					}
				}
				if (ip.equals("")) {
					state = ExecutionActionState.REJECTED;
					throw new Exception("Dependend node " + dependOn + " not reachable!");
				}
				app.getArguments().add(ip);
			}
		}
	}
}
