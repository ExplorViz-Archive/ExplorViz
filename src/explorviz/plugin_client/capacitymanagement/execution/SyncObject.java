package explorviz.plugin_client.capacitymanagement.execution;

public interface SyncObject {
	public boolean isLockedUntilExecutionActionFinished();

	public void setLockedUntilExecutionActionFinished(boolean locked);
}
