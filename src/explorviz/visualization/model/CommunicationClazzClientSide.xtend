package explorviz.visualization.model

class CommunicationClazzClientSide {
	@Property int requestsPerSecond
	@Property float averageResponseTime
	
	@Property ClazzClientSide source
	@Property ClazzClientSide target
}