package explorviz.shared.model

import java.util.Set
import explorviz.shared.model.helper.Draw3DEdgeEntity
import java.util.HashSet

class CommunicationClazz extends Draw3DEdgeEntity {
	@Property int requests
	@Property float averageResponseTime
	
	@Property String methodName
	@Property Set<Long> traceIds = new HashSet<Long>
	
	@Property Clazz source
	@Property Clazz target
}