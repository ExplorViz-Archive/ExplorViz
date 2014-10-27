package explorviz.plugin.anomalydetection

import explorviz.plugin.interfaces.IPluginClientSide
import explorviz.plugin.main.Perspective
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.ClazzCommand
import explorviz.visualization.engine.contextmenu.commands.ComponentCommand
import explorviz.visualization.main.PluginManagerClientSide
import explorviz.shared.model.Node
import explorviz.shared.model.Application
import java.util.TreeMap
import java.util.Random
import java.util.Map

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

	def static void showTimeSeriesDialog(String elementName , Map<Long, Float> series ) {
		OPADxClientSideJS::showTimeSeriesDialog(elementName)

		// TODO dummy for now
		val result = new TreeMap<Long, Float>()

		val float randomNumber = new Random().nextInt(3000) / 3000f
		result.put(System.currentTimeMillis(), randomNumber)

		for (var int i = 1; i < 40; i++) {
			val float randomNumber2 = new Random().nextInt(3000) / 3000f
			result.put(System.currentTimeMillis() + (i * 20 * 1000), randomNumber2);
		}

		OPADxClientSideJS::updateAnomalyAndReponseTimesChart(result, result, result)

	}
}

class ShowTimeSeriesApplicationCommand extends ApplicationCommand {
	override execute() {
		OPADxClientSide::showTimeSeriesDialog(currentApp.name, null)
		super.execute()
	}
}

class ShowTimeSeriesComponentCommand extends ComponentCommand {
	override execute() {
		OPADxClientSide::showTimeSeriesDialog(currentComponent.name, null)
		super.execute()
	}
}

class ShowTimeSeriesClazzCommand extends ClazzCommand {
	override execute() {
		OPADxClientSide::showTimeSeriesDialog(currentClazz.name, null)
		super.execute()
	}
}
