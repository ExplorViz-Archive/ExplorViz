package explorviz.plugin.capacitymanagement.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.attributes.IPluginKeys;
import explorviz.plugin.capacitymanagement.CapManExecutionStates;
import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;
import explorviz.shared.model.helper.GenericModelElement;

public abstract class ExecutionAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionAction.class);

	protected ExecutionActionState state = ExecutionActionState.INITIAL;

	public void execute(final ICloudController controller) throws FailedExecutionException {
		if (LoadBalancersFacade.getNodeCount() >= ExecutionOrganizer.maxRunningNodesLimit) {
			state = ExecutionActionState.REJECTED;
		}
		new Thread(new Runnable() {
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
						success = concreteAction();
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
						sync.setLockedUntilExecutionActionFinished(false);
						sync.notify();
					}
				}
			}

		}).start();

	}

	protected abstract GenericModelElement getActionObject();

	protected abstract SyncObject synchronizeOn();

	protected abstract boolean beforeAction();

	protected abstract boolean concreteAction();

	protected abstract void afterAction();

	protected abstract String getLoggingDescription();
}
