package explorviz.visualization.model

class CommunicationClazzClientSide {
	@Property int requests
	@Property float averageResponseTime
	
	@Property ClazzClientSide source
	@Property ClazzClientSide target
}