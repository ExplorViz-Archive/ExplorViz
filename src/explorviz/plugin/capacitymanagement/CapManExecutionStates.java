package explorviz.plugin.capacitymanagement;

import explorviz.shared.model.helper.IValue;

/**
 * Execution States of Capacity Management (starting, terminating, restarting or
 * none)
 *
 */
public enum CapManExecutionStates implements IValue {
	STARTING, TERMINATING, RESTARTING, NONE;
}
