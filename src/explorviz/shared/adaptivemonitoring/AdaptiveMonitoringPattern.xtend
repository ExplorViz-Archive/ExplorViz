package explorviz.shared.adaptivemonitoring

import com.google.gwt.user.client.rpc.IsSerializable

class AdaptiveMonitoringPattern implements IsSerializable {
	@Property String pattern
	@Property boolean active
}