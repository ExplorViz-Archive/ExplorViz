package explorviz.plugin.main

import explorviz.plugin.anomalydetection.OPADxClientSide
import explorviz.plugin.capacitymanagement.CapManClientSide
import explorviz.visualization.main.PluginManagerClientSide

class PluginCreationClientSide {
	def static void init() {
		PluginManagerClientSide::clearNodePopup()
		PluginManagerClientSide::clearApplicationPopup()
		
		new OPADxClientSide()
		new CapManClientSide()
	}
}