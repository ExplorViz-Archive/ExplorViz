package explorviz.plugin_server.anomalydetection

import explorviz.plugin_client.attributes.IPluginKeys
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue
import explorviz.plugin_server.interfaces.IAnomalyDetector
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.shared.model.helper.GenericModelElement
import explorviz.shared.model.CommunicationClazz
import explorviz.shared.model.RuntimeInformation
import explorviz.plugin_server.anomalydetection.aggregation.TraceAggregator
import java.util.HashMap
import java.util.Collections
import explorviz.plugin_server.anomalydetection.forecast.AbstractForecaster
import explorviz.plugin_server.anomalydetection.anomalyscore.CalculateAnomalyScore
import explorviz.plugin_server.anomalydetection.anomalyscore.InterpreteAnomalyScore
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.Application

class OPADx implements IAnomalyDetector {

	new() {
		PluginManagerServerSide::registerAsAnomalyDetector(this)
	}

	override doAnomalyDetection(Landscape landscape) {
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						//TODO flags eventuell erst nach der berechnung unten setzn
						//setzen (siehe TODO unten); damit würde ein eventuelles 
						//blinken verhindert werden und jedes flag wird in jedem Durchlauf nur einmal gesetzt
						application.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, false)
						application.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, false)
						for (component : application.components) {
							component.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, false)
							component.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, false)
							recursiveComponentForking(component)
						}
						for (communication : application.communications) {
							communication.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, false)
							communication.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, false)
							annotateTimeSeriesAndAnomalyScore(communication, landscape.timestamp)
						}

					//annotateTimeSeriesAndAnomalyScore(application, landscape.timestamp)
					}
				}
			}
		}
	}

	// TODO Name der Methode ändern
	def void recursiveComponentForking(Component component) {
		for (clazz : component.clazzes) {
			clazz.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, false)
			clazz.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, false)
		}
		for (childComponent : component.children) {
			childComponent.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, false)
			childComponent.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, false)
			recursiveComponentForking(childComponent)
		}
	}

	def void annotateTimeSeriesAndAnomalyScore(GenericModelElement element, long timestamp) {

		/* 
		 * TODO wenn noch keine responsetimes da sind,
		 * anomalyscore für timestamp mit 0 abspeichern 
		 * und als forecastresponsetime die responsetime setzen
		 * TODO sobald zwei responsetime existiert gewünschten forecaster benutzen
		 * (drauf achten das genug Werte da sind) 
		*/
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
		var communicationClazz = element as CommunicationClazz
		var traceIdToRuntimeMap = communicationClazz.traceIdToRuntimeMap as HashMap<Long, RuntimeInformation>
		var responseTime = new TraceAggregator().aggregateTraces(traceIdToRuntimeMap)
		responseTimes.put(timestamp, responseTime)

		var delimitedResponseTimes = delimitTreeMap(responseTimes) as TreeMapLongDoubleIValue
		var delimitedPredictedResponseTimes = delimitTreeMap(predictedResponseTimes)
		var predictedResponseTime = AbstractForecaster.forecast(delimitedResponseTimes, delimitedPredictedResponseTimes)
		predictedResponseTimes.put(timestamp, predictedResponseTime)

		var anomalyScore = new CalculateAnomalyScore().getAnomalyScore(responseTime, predictedResponseTime)
		anomalyScores.put(timestamp, anomalyScore)
		val boolean[] errorWarning = new InterpreteAnomalyScore().interprete(anomalyScore)
		if (errorWarning.get(1)) {
			element.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)
			element.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, true)
			annotateParentHierachy(element as CommunicationClazz, true)
		} else if (errorWarning.get(0)) {
			element.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)
			annotateParentHierachy(element as CommunicationClazz, false)
		}// TODO von oben hier unten

		element.putGenericData(IPluginKeys::TIMESTAMP_TO_RESPONSE_TIME, responseTimes)
		element.putGenericData(IPluginKeys::TIMESTAMP_TO_PREDICTED_RESPONSE_TIME, predictedResponseTimes)
		element.putGenericData(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE, anomalyScores)
	}

	def TreeMapLongDoubleIValue delimitTreeMap(TreeMapLongDoubleIValue map) {
		var newMap = new TreeMapLongDoubleIValue()
		for (var i = 0; i < Configuration.TIME_SERIES_WINDOW_SIZE; i++) {
			var key = Collections.max(map.keySet)
			newMap.put(key, map.get(key))
			map.remove(key)
		}
		return newMap
	}

	def void annotateParentHierachy(CommunicationClazz element, boolean warningOrError) {
		var Clazz clazz = element.target;
		if (warningOrError) {
			clazz.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)
			clazz.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, true)
		} else {
			clazz.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)
		}

		annotateParentComponent(clazz.parent, warningOrError)
	}

	def void annotateParentComponent(Component component, boolean warningOrError) {
		var Component parentComponent = component.parentComponent;
		if (warningOrError) {
			parentComponent.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)
			parentComponent.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, true)
		} else {
			parentComponent.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)
		}

		//TODO kann parentComponent.parentComponent null sein?
		if (parentComponent.parentComponent != null) {
			annotateParentComponent(parentComponent.parentComponent, warningOrError)
		} else {
			var Application application = parentComponent.belongingApplication
			if (warningOrError) {
				application.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)
				application.putGenericBooleanData(IPluginKeys::ERROR_ANOMALY, true)
			} else {
				application.putGenericBooleanData(IPluginKeys::WARNING_ANOMALY, true)
			}
		}
	}

}
