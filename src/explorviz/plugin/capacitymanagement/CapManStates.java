package explorviz.plugin.capacitymanagement;

import explorviz.shared.model.helper.IValue;

/**
 * States of Capacity Management (restart, terminate or none)
 *
 */
public enum CapManStates implements IValue {
	RESTART, TERMINATE, NONE;
}
