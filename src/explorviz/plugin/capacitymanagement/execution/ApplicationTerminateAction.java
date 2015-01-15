package explorviz.plugin.capacitymanagement.execution;

import explorviz.plugin.attributes.IPluginKeys;
import explorviz.plugin.capacitymanagement.CapManExecutionStates;
import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public class ApplicationTerminateAction extends ExecutionAction {

	private final Application app;
	private final Node parent;

	public ApplicationTerminateAction(final Application app) {
		this.app = app;
		parent = app.getParent();
	}

	@Override
	public void execute(final ICloudController controller) {
		final Node parent = app.getParent();

		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (parent) {
					while (parent.isLockedUntilExecutionActionFinished()) {
						try {
							parent.wait();
						} catch (final InterruptedException e) {

						}
					}
					parent.setLockedUntilExecutionActionFinished(true);
					boolean success = false;
					try {
						success = controller.terminateApplication(app);
					} catch (final Exception e) {
						// LOGGER.error("Error while terminating application" +
						// app.getName() + ":");
						// LOGGER.error(e.getMessage(), e);
						state = ExecutionActionState.ABORTED;
					} finally {
						if (success) {
							parent.removeApplication(app.getId());
							state = ExecutionActionState.SUCC_FINISHED;
						}

						parent.setLockedUntilExecutionActionFinished(false);
						parent.notify();
						app.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
								CapManExecutionStates.NONE);

						// LOGGER.info("Terminated application " +
						// app.getName());
					}
				}
			}

		}).start();

	}

	@Override
	protected GenericModelElement getActionObject() {

		return app;
	}

	@Override
	protected SyncObject synchronizeOn() {

		return app;
	}

	@Override
	protected void beforeAction() {
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

	@Override
	protected boolean concreteAction(final ICloudController controller) {

		return controller.terminateApplication(app);
	}

	@Override
	protected void afterAction() {

		parent.removeApplication(app.getId());
	}

	@Override
	protected String getLoggingDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void finallyDo() {
		synchronized (parent) {
			// Am I the last running ApplicationExecutionAction on this node?
			if (parent.readRunningApplications() == 1) {
				// then release lock
				parent.setLockedUntilExecutionActionFinished(false);
			}
			parent.decrementRunningApplications();
		}
	}
}
