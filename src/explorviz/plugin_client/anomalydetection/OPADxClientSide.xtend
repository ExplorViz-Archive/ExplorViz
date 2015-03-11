package explorviz.plugin_client.anomalydetection

import explorviz.plugin_client.attributes.IPluginKeys
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue
import explorviz.plugin_client.interfaces.IPluginClientSide
import explorviz.plugin_client.main.Perspective
import explorviz.shared.model.Application
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.shared.model.helper.GenericModelElement
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.ClazzCommand
import explorviz.visualization.engine.contextmenu.commands.ComponentCommand
import explorviz.visualization.main.PluginManagerClientSide
import java.util.Map

class OPADxClientSide implements IPluginClientSide {
	static var currentlyShowingPopup = false

	override switchedToPerspective(Perspective perspective) {
		PluginManagerClientSide::addApplicationPopupSeperator
		PluginManagerClientSide::addApplicationPopupEntry("Show time series", new ShowTimeSeriesApplicationCommand())

		PluginManagerClientSide::addComponentPopupEntry("Show time series", new ShowTimeSeriesComponentCommand())

		PluginManagerClientSide::addClazzPopupSeperator
		PluginManagerClientSide::addClazzPopupEntry("Show time series", new ShowTimeSeriesClazzCommand())
	}

	override popupMenuOpenedOn(Node node) {
		// empty
	}

	override popupMenuOpenedOn(Application app) {
		// empty
	}

	def static void showTimeSeriesDialog(String elementName, Map<Long, Double> responseTimes,
		Map<Long, Double> predictedResponseTimes, Map<Long, Double> anomalyScores) {
		OPADxClientSideJS::showTimeSeriesDialog(elementName)

		if (responseTimes != null && predictedResponseTimes != null && anomalyScores != null) {
			OPADxClientSideJS::updateAnomalyAndReponseTimesChart(anomalyScores, responseTimes, predictedResponseTimes)
		}
	}

	override newLandscapeReceived(Landscape landscape) {
		if (currentlyShowingPopup) {
			// TODO update the data in the opened popup
			//				OPADxClientSideJS::updateAnomalyAndReponseTimesChart(anomalyScore, responseTimes,
			//					predictedResponseTimes)
		}
	}

	def static void showTimeSeriesGeneric(GenericModelElement entity, String name) {
		if (entity.isGenericDataPresent(IPluginKeys::TIMESTAMP_TO_RESPONSE_TIME) &&
			entity.isGenericDataPresent(IPluginKeys::TIMESTAMP_TO_PREDICTED_RESPONSE_TIME) &&
			entity.isGenericDataPresent(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE)) {

			val responseTimes = entity.getGenericData(IPluginKeys::TIMESTAMP_TO_RESPONSE_TIME) as TreeMapLongDoubleIValue
			val predictedResponseTimes = entity.getGenericData(IPluginKeys::TIMESTAMP_TO_PREDICTED_RESPONSE_TIME) as TreeMapLongDoubleIValue
			val anomalyScores = entity.getGenericData(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE) as TreeMapLongDoubleIValue
			OPADxClientSide::showTimeSeriesDialog(name, responseTimes, predictedResponseTimes, anomalyScores)
		}
	}
}

class ShowTimeSeriesApplicationCommand extends ApplicationCommand {
	override execute() {
		OPADxClientSide::showTimeSeriesGeneric(currentApp, currentApp.name)
		super.execute()
	}
}

class ShowTimeSeriesComponentCommand extends ComponentCommand {
	override execute() {
		OPADxClientSide::showTimeSeriesGeneric(currentComponent, currentComponent.name)
		super.execute()
	}
}

class ShowTimeSeriesClazzCommand extends ClazzCommand {
	override execute() {
		OPADxClientSide::showTimeSeriesGeneric(currentClazz, currentClazz.name)
		super.execute()
	}
}
