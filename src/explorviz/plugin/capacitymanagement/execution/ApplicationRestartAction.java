package explorviz.plugin.capacitymanagement.execution;

import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;
import explorviz.shared.model.Application;
import explorviz.shared.model.helper.GenericModelElement;

public class ApplicationRestartAction extends ExecutionAction {

	Application application;

	public ApplicationRestartAction(final Application app) {
		application = app;
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
	protected void beforeAction() {
		synchronized (application.getParent()) {
			// TODO get Lock on Node, if first Application
		}

	}

	@Override
	protected boolean concreteAction(final ICloudController controller) {
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

	@Override
	protected void finallyDo() {
		// TODO Auto-generated method stub

	}

}
