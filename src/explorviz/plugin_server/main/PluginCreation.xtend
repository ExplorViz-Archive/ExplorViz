package explorviz.plugin_server.main

import explorviz.plugin_server.anomalydetection.OPADx
import explorviz.plugin_server.rootcausedetection.RanCorr
import explorviz.plugin_server.capacitymanagement.CapMan

class PluginCreation {
	def static void init() {
		new OPADx()
		new RanCorr()
		new CapMan()
	}
}