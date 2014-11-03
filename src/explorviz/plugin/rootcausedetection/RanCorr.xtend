package explorviz.plugin.rootcausedetection

import explorviz.plugin.interfaces.IRootCauseDetector
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.plugin.attributes.IPluginKeys

class RanCorr implements IRootCauseDetector {

	new() {
		PluginManagerServerSide::registerAsRootCauseDetector(this)
	}

	override doRootCauseDetection(Landscape landscape) {

		// TODO
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						// TODO ROOT CAUSE RATING > 0.5 leads to WARNING
						// TODO ROOT CAUSE RATING > 0.7 leads to ERROR
						application.putGenericBooleanData(IPluginKeys::ERROR_ROOTCAUSE, true)
					}
				}
			}
		}
	}

}
