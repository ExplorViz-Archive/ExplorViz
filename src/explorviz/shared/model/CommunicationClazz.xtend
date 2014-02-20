package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable

class CommunicationClazz implements IsSerializable {
	@Property int requestsPerSecond
	@Property float averageResponseTime
	
	@Property Clazz source
	@Property Clazz target
}