package explorviz.visualization.model

import explorviz.visualization.model.helper.Draw3DEdgeEntity

class CommunicationClazzClientSide extends Draw3DEdgeEntity {
	@Property int requestsPerSecond
	@Property float averageResponseTime
	
	@Property ClazzClientSide source
	@Property ClazzClientSide target
	
	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()
	}
	
}