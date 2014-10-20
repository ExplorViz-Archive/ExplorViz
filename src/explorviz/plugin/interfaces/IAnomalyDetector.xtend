package explorviz.plugin.interfaces

import explorviz.shared.model.Landscape

interface IAnomalyDetector {
	def void doAnomalyDetection(Landscape landscape)
}