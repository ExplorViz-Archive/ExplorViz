package explorviz.plugin.anomalydetection

import explorviz.plugin.interfaces.IAnomalyDetector
import explorviz.shared.model.Landscape
import explorviz.server.main.PluginManagerServerSide
import explorviz.plugin.attributes.IPluginKeys

class OPADx implements IAnomalyDetector {

	new() {
		PluginManagerServerSide::registerAsAnomalyDetector(this)
	}

	override doAnomalyDetection(Landscape landscape) {
		// TODO
		
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups)  {
				for (node : nodeGroup.nodes)  {
					for (application : node.applications) {
						application.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)
						application.putGenericDoubleData(IPluginKeys::ANOMALY_SCORE, 0.2)
					}
				}
			}
		}
	}
}
