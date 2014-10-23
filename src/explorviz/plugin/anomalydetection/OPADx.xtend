package explorviz.plugin.anomalydetection

import explorviz.plugin.interfaces.IAnomalyDetector
import explorviz.shared.model.Landscape
import explorviz.server.main.PluginManagerServerSide

class OPADx implements IAnomalyDetector {

	new() {
		PluginManagerServerSide::registerAsAnomalyDetector(this)
	}

	override doAnomalyDetection(Landscape landscape) {
		// TODO
	}

}
