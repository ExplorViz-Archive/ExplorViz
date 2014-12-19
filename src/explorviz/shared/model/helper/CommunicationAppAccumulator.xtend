package explorviz.shared.model.helper

import java.util.ArrayList
import explorviz.shared.model.CommunicationClazz
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

class CommunicationAppAccumulator extends Draw3DEdgeEntity {
	@Accessors Draw3DNodeEntity source
	@Accessors Draw3DNodeEntity target
	
	@Accessors int requests
//	@Accessors float averageResponseTime
	
	@Accessors val transient List<CommunicationClazz> aggregatedCommunications = new ArrayList<CommunicationClazz> 
	
	def void clearAllPrimitiveObjects() {
		primitiveObjects.clear()
	}
}