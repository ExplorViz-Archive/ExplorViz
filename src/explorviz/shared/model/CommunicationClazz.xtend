package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable
import java.util.Set

class CommunicationClazz implements IsSerializable {
	@Property int requestsPerSecond
	@Property float averageResponseTime
	@Property String methodSignature
	@Property Set<Long> traceIds
	
	@Property Clazz source
	@Property Clazz target
}