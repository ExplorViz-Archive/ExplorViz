package explorviz.plugin.interfaces

import explorviz.shared.model.Landscape

interface ICapacityManager {
	
	def void doCapacityManagement(Landscape landscape)
	
	def void receivedFinalCapacityAdaptationPlan(Landscape landscape)
	
}