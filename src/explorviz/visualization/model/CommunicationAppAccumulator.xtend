package explorviz.visualization.model

import explorviz.visualization.model.helper.Draw3DNodeEntity
import explorviz.visualization.model.helper.Draw3DEdgeEntity

class CommunicationAppAccumulator extends Draw3DEdgeEntity {
	@Property Draw3DNodeEntity source
	@Property Draw3DNodeEntity target
	
	@Property int requestCount
	@Property float averageResponseTime
	
	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()
	}
}