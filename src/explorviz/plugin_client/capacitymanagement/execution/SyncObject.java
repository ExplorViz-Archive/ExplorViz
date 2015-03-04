package explorviz.plugin_client.capacitymanagement.execution;

/**
 * interface to lock on objects during execution of an action (e.g. starting
 * node)
 */
public interface SyncObject {
	public boolean isLockedUntilExecutionActionFinished();

	public void setLockedUntilExecutionActionFinished(boolean locked);
}
