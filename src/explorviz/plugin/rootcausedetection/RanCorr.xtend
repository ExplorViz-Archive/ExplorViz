package explorviz.plugin.rootcausedetection

import explorviz.plugin.interfaces.IRootCauseDetector
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape

class RanCorr implements IRootCauseDetector {

	new() {
		PluginManagerServerSide::registerAsRootCauseDetector(this)
	}

	override doRootCauseDetection(Landscape landscape) {
		// TODO
	}

}
