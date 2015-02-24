package explorviz.plugin_client.capacitymanagement;

import explorviz.shared.model.helper.IValue;

/**
 * @author jgi, dtj States of Capacity Management (start_new, restart,
 *         terminate, replicate or none)
 */
public enum CapManStates implements IValue {
	RESTART, TERMINATE, REPLICATE, NONE;
}
