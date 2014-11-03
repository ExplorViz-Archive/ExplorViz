package explorviz.plugin.anomalydetection

import explorviz.plugin.attributes.IPluginKeys
import explorviz.plugin.interfaces.IAnomalyDetector
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.plugin.attributes.TreeMapLongDoubleIValue

class OPADx implements IAnomalyDetector {

	new() {
		PluginManagerServerSide::registerAsAnomalyDetector(this)
	}

	override doAnomalyDetection(Landscape landscape) {

		// TODO
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						application.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)

						// TODO always false since new landscape model..
						var responseTimes = application.getGenericData(IPluginKeys::TIMESTAMP_TO_RESPONSE_TIME) as TreeMapLongDoubleIValue
						if (responseTimes == null) {
							responseTimes = new TreeMapLongDoubleIValue()
						}
						var predictedResponseTimes = application.getGenericData(IPluginKeys::TIMESTAMP_TO_PREDICTED_RESPONSE_TIME) as TreeMapLongDoubleIValue
						if (predictedResponseTimes == null) {
							predictedResponseTimes = new TreeMapLongDoubleIValue()
						}
						var anomalyScores = application.getGenericData(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE) as TreeMapLongDoubleIValue
						if (anomalyScores == null) {
							anomalyScores = new TreeMapLongDoubleIValue()
						}

						responseTimes.put(landscape.timestamp, 20d)
						predictedResponseTimes.put(landscape.timestamp, 10d)
						anomalyScores.put(landscape.timestamp, 0.2d)
						
						
						application.putGenericData(IPluginKeys::TIMESTAMP_TO_RESPONSE_TIME, responseTimes)
						application.putGenericData(IPluginKeys::TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
							predictedResponseTimes)
						application.putGenericData(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE, anomalyScores)
					}
				}
			}
		}
	}
}
