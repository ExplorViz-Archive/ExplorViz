package explorviz.plugin_client.capacitymanagement;

import explorviz.shared.model.helper.IValue;

/**
 * States of Capacity Management (restart, terminate, replicate, migrate or
 * none)
 */
public enum CapManStates implements IValue {
	RESTART, TERMINATE, REPLICATE, MIGRATE, NONE;
}
