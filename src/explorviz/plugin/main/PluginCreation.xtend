package explorviz.plugin.main

import explorviz.plugin.anomalydetection.OPADx
import explorviz.plugin.rootcausedetection.RanCorr
import explorviz.plugin.capacitymanagement.CapMan

class PluginCreation {
	def static void init() {
		new OPADx()
		new RanCorr()
		new CapMan()
	}
}