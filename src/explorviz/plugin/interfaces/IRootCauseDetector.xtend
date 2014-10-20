package explorviz.plugin.interfaces

import explorviz.shared.model.Landscape

interface IRootCauseDetector {
	
	def void doRootCauseDetection(Landscape landscape)
	
}