package explorviz.plugin_server.interfaces

import explorviz.shared.model.Landscape

interface ICapacityManager {
	
	def void doCapacityManagement(Landscape landscape)
	
	def void receivedFinalCapacityAdaptationPlan(Landscape landscape)
	
	def void cancelButton(Landscape landscape)
	
}