package explorviz.plugin.capacitymanagement.execution;

public interface SyncObject {
	public boolean isLockedUntilExecutionActionFinished();

	public void setLockedUntilExecutionActionFinished(boolean locked);
}
