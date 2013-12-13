package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable

class Communication implements IsSerializable {
	@Property int requestsPerSecond
	
	@Property Application source
	@Property Application target
}