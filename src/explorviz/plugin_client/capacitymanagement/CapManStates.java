package explorviz.plugin_client.capacitymanagement;

import explorviz.shared.model.helper.IValue;

/**
 * States of Capacity Management (start_new, restart, terminate or none)
 *
 */
public enum CapManStates implements IValue {
	START_NEW, RESTART, TERMINATE, REPLICATE, NONE;
}
