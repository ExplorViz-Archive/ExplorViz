package explorviz.plugin.capacitymanagement.execution;

import explorviz.plugin.capacitymanagement.cloud_control.ICloudController;

public abstract class ExecutionAction {

	protected ExecutionActionState state = ExecutionActionState.INITIAL;

	public abstract void execute(ICloudController controller) throws FailedExecutionException;

}
