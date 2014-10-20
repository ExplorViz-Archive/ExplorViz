package explorviz.plugin.rootcausedetection

import explorviz.plugin.interfaces.IRootCauseDetector
import explorviz.shared.model.Landscape
import explorviz.server.main.PluginManager

class RanCorr implements IRootCauseDetector {

	new() {
		PluginManager::registerAsRootCauseDetector(this)
	}

	override doRootCauseDetection(Landscape landscape) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

}
