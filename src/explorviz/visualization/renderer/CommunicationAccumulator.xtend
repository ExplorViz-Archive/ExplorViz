package explorviz.visualization.renderer

import explorviz.visualization.model.helper.Draw3DNodeEntity

class CommunicationAccumulator {
	@Property Draw3DNodeEntity source
	@Property Draw3DNodeEntity target
	
	@Property int requestCount
	@Property float averageResponseTime
	@Property int count
}