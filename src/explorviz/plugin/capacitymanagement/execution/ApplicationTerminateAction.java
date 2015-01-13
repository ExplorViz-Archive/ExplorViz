package explorviz.plugin.capacitymanagement.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.attributes.IPluginKeys;
import explorviz.plugin.capacitymanagement.CapManExecutionStates;
import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.shared.model.Application;
import explorviz.shared.model.Node;
import explorviz.shared.model.helper.GenericModelElement;

public class ApplicationTerminateAction extends ExecutionAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationTerminateAction.class);

	private final Application app;

	public ApplicationTerminateAction(final Application app) {
		this.app = app;
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
						LOGGER.error("Error while terminating application" + app.getName() + ":");
						LOGGER.error(e.getMessage(), e);
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

						LOGGER.info("Terminated application " + app.getName());
					}
				}
			}

		}).start();

	}

	@Override
	protected GenericModelElement getActionObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SyncObject synchronizeOn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean beforeAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean concreteAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void afterAction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getLoggingDescription() {
		// TODO Auto-generated method stub
		return null;
	}
}
