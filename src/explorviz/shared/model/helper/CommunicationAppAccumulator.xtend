package explorviz.shared.model.helper

class CommunicationAppAccumulator extends Draw3DEdgeEntity {
	@Property Draw3DNodeEntity source
	@Property Draw3DNodeEntity target
	
	@Property int requests
	@Property float averageResponseTime
}