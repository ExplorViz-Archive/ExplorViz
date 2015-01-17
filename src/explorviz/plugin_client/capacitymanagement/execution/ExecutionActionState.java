package explorviz.plugin_client.capacitymanagement.execution;

public enum ExecutionActionState {
	INITIAL, // inital state before and during execution
	SUCC_FINISHED, // action successfully completed
	ABORTED, // action aborted with errors
	REJECTED; // action rejected because of exceeding maxNodeLimit

}
