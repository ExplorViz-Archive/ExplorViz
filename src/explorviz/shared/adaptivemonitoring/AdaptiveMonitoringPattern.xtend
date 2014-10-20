package explorviz.shared.adaptivemonitoring

import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors

class AdaptiveMonitoringPattern implements IsSerializable {
	@Accessors String pattern
	@Accessors boolean active
}