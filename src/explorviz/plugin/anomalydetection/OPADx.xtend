package explorviz.plugin.anomalydetection

import explorviz.plugin.interfaces.IAnomalyDetector
import explorviz.shared.model.Landscape
import explorviz.server.main.PluginManager

class OPADx implements IAnomalyDetector {

	new() {
		PluginManager::registerAsAnomalyDetector(this)
	}

	override doAnomalyDetection(Landscape landscape) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

}
