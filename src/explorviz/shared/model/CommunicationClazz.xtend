package explorviz.shared.model

import java.util.Set
import explorviz.shared.model.helper.Draw3DEdgeEntity

class CommunicationClazz extends Draw3DEdgeEntity {
	@Property int requests
	@Property float averageResponseTime
	@Property String methodSignature
	@Property Set<Long> traceIds
	
	@Property Clazz source
	@Property Clazz target
}