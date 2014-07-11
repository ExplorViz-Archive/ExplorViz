package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable

class RuntimeInformation implements IsSerializable {
	@Property int calledTimes
	@Property int requests
	@Property float averageResponseTime
}