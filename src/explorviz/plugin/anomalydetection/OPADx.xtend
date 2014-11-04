package explorviz.plugin.anomalydetection

import explorviz.plugin.attributes.IPluginKeys
import explorviz.plugin.attributes.TreeMapLongDoubleIValue
import explorviz.plugin.interfaces.IAnomalyDetector
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.shared.model.helper.GenericModelElement

class OPADx implements IAnomalyDetector {

	new() {
		PluginManagerServerSide::registerAsAnomalyDetector(this)
	}

	override doAnomalyDetection(Landscape landscape) {
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						application.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, false)
						application.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, false)

						annotateTimeSeriesAndAnomalyScore(application, landscape.timestamp)

					// TODO go down to components and classes
					}
				}
			}
		}
	}

	def void annotateTimeSeriesAndAnomalyScore(GenericModelElement element, long timestamp) {
		var responseTimes = element.getGenericData(IPluginKeys::TIMESTAMP_TO_RESPONSE_TIME) as TreeMapLongDoubleIValue
		if (responseTimes == null) {
			responseTimes = new TreeMapLongDoubleIValue()
		}
		var predictedResponseTimes = element.getGenericData(IPluginKeys::TIMESTAMP_TO_PREDICTED_RESPONSE_TIME) as TreeMapLongDoubleIValue
		if (predictedResponseTimes == null) {
			predictedResponseTimes = new TreeMapLongDoubleIValue()
		}
		var anomalyScores = element.getGenericData(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE) as TreeMapLongDoubleIValue
		if (anomalyScores == null) {
			anomalyScores = new TreeMapLongDoubleIValue()
		}

		// TODO delete values before Configuration::TIMESHIFT_INTERVAL_IN_MINUTES (default is 10 min)
		
		// TODO calculate and get real values
		responseTimes.put(timestamp, 20d)
		predictedResponseTimes.put(timestamp, 10d)
		anomalyScores.put(timestamp, 0.2d)

		element.putGenericData(IPluginKeys::TIMESTAMP_TO_RESPONSE_TIME, responseTimes)
		element.putGenericData(IPluginKeys::TIMESTAMP_TO_PREDICTED_RESPONSE_TIME, predictedResponseTimes)
		element.putGenericData(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE, anomalyScores)
	}
}
