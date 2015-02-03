package explorviz.plugin_client.capacitymanagement.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.capacitymanagement.CapManExecutionStates;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public abstract class ExecutionAction {

	// TODO: jkr/jek: bei kopierten/neuen Knoten/Applikationen sämtliche
	// Attribute des Originals setzen

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionAction.class);
	private static final int MAX_TRIES = 10;

	protected ExecutionActionState state = ExecutionActionState.INITIAL;

	public ExecutionActionState getState() {
		return state;
	}

	public void execute(final ICloudController controller, final ThreadGroup group) /*
	 * throws
	 * FailedExecutionException
	 */{
		if (LoadBalancersFacade.getNodeCount() >= ExecutionOrganizer.maxRunningNodesLimit) {
			state = ExecutionActionState.REJECTED;
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
						// TODO: überdenken
						LOGGER.info("Try " + getLoggingDescription());
						for (int i = 0; (success == false) && (i < MAX_TRIES); i++) {
							success = concreteAction(controller);
						}
					} catch (final Exception e) {
						LOGGER.error("Error while " + getLoggingDescription());
						LOGGER.error(e.getMessage(), e);
						state = ExecutionActionState.ABORTED;
					} finally {
						if (success) {
							afterAction();
							state = ExecutionActionState.SUCC_FINISHED;
							getActionObject().putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.NONE);
							LOGGER.info("Action successfully finished: " + getLoggingDescription());
						}
						finallyDo();
						sync.setLockedUntilExecutionActionFinished(false);
						sync.notify();
					}
				}
			}

		}).start();

	}

	// TODO: jek, jkr: ist syncObject und actionObject immer gleich?
	protected abstract GenericModelElement getActionObject();

	// TODO: jek, jkr: Nodes brauchen 1 Lock und 1 Counter für
	// ApplicationActions
	// je Knoten darf entweder 1 NodeAction oder 1-n ApplicationActions parallel
	// ausgeführt werden.
	protected abstract SyncObject synchronizeOn();

	protected abstract void beforeAction();

	protected abstract boolean concreteAction(ICloudController controller) throws Exception;

	protected abstract void afterAction();

	protected abstract void finallyDo();

	protected abstract String getLoggingDescription();

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

	// TODO: jek/jkr: abstrakte checkBeforeAction() Methode?
	// --> maxRunningNodCount / ApplicationScript überprüfen
	// TODO: jkr/jek: wollen wir das so strikt?
	protected boolean checkApplicationScriptsExist(final Node node) {
		for (final Application app : node.getApplications()) {
			if ((app.getStartScript() == null) || app.getStartScript().equals("")) {
				return false;
			}
		}
		return true;
	}

	protected abstract ExecutionAction getCompensateAction();
}
