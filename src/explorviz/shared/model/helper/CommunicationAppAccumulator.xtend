package explorviz.shared.model.helper

import java.util.ArrayList
import explorviz.shared.model.CommunicationClazz
import java.util.List

class CommunicationAppAccumulator extends Draw3DEdgeEntity {
	@Property Draw3DNodeEntity source
	@Property Draw3DNodeEntity target
	
	@Property int requests
//	@Property float averageResponseTime
	
	@Property val List<CommunicationClazz> aggregatedCommunications = new ArrayList<CommunicationClazz> 
	
	def void clearAllPrimitiveObjects() {
		primitiveObjects.clear()
	}
}