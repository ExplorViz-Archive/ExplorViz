package explorviz.plugin.anomalydetection

import explorviz.plugin.attributes.IPluginKeys
import explorviz.plugin.attributes.TreeMapLongDoubleIValue
import explorviz.plugin.interfaces.IPluginClientSide
import explorviz.plugin.main.Perspective
import explorviz.shared.model.Application
import explorviz.shared.model.Node
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.ClazzCommand
import explorviz.visualization.engine.contextmenu.commands.ComponentCommand
import explorviz.visualization.main.PluginManagerClientSide
import java.util.Map
import java.util.Random
import java.util.TreeMap

class OPADxClientSide implements IPluginClientSide {
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

		if (responseTimes == null) {

			// TODO dummy for now
			val result = new TreeMap<Long, Double>()

			val double randomNumber = new Random().nextInt(3000) / 3000d
			result.put(System.currentTimeMillis(), randomNumber)

			for (var int i = 1; i < 40; i++) {
				val double randomNumber2 = new Random().nextInt(3000) / 3000d
				result.put(System.currentTimeMillis() + (i * 20 * 1000), randomNumber2);
			}
			OPADxClientSideJS::updateAnomalyAndReponseTimesChart(result, result, result)
		} else {
			OPADxClientSideJS::updateAnomalyAndReponseTimesChart(anomalyScores, responseTimes, predictedResponseTimes)
		}
	}
}

class ShowTimeSeriesApplicationCommand extends ApplicationCommand {
	override execute() {
		if (currentApp.isGenericDataPresent(IPluginKeys::TIMESTAMP_TO_RESPONSE_TIME) &&
			currentApp.isGenericDataPresent(IPluginKeys::TIMESTAMP_TO_PREDICTED_RESPONSE_TIME) &&
			currentApp.isGenericDataPresent(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE)) {
			val responseTimes = currentApp.getGenericData(IPluginKeys::TIMESTAMP_TO_RESPONSE_TIME) as TreeMapLongDoubleIValue
			val predictedResponseTimes = currentApp.getGenericData(IPluginKeys::TIMESTAMP_TO_PREDICTED_RESPONSE_TIME) as TreeMapLongDoubleIValue
			val anomalyScore = currentApp.getGenericData(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE) as TreeMapLongDoubleIValue
			OPADxClientSide::showTimeSeriesDialog(currentApp.name, responseTimes, predictedResponseTimes, anomalyScore)
		}
		super.execute()
	}
}

class ShowTimeSeriesComponentCommand extends ComponentCommand {
	override execute() {
		OPADxClientSide::showTimeSeriesDialog(currentComponent.name, null, null, null)
		super.execute()
	}
}

class ShowTimeSeriesClazzCommand extends ClazzCommand {
	override execute() {
		OPADxClientSide::showTimeSeriesDialog(currentClazz.name, null, null, null)
		super.execute()
	}
}
