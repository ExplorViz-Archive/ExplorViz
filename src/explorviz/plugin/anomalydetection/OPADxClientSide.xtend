package explorviz.plugin.anomalydetection

import explorviz.plugin.interfaces.IPluginClientSide
import explorviz.plugin.main.Perspective
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.ClazzCommand
import explorviz.visualization.engine.contextmenu.commands.ComponentCommand
import explorviz.visualization.engine.contextmenu.commands.NodeCommand
import explorviz.visualization.main.PluginManagerClientSide
import explorviz.shared.model.Node
import explorviz.shared.model.Application

class OPADxClientSide implements IPluginClientSide {
	override switchedToPerspective(Perspective perspective) {
		PluginManagerClientSide::addNodePopupEntry("Show time series", new ShowTimeSeriesNodeCommand())

		PluginManagerClientSide::addApplicationPopupSeperator
		PluginManagerClientSide::addApplicationPopupEntry("Show time series", new ShowTimeSeriesApplicationCommand())

		PluginManagerClientSide::addComponentPopupEntry("Show time series", new ShowTimeSeriesComponentCommand())
		
		PluginManagerClientSide::addClazzPopupSeperator
		PluginManagerClientSide::addClazzPopupEntry("Show time series", new ShowTimeSeriesClazzCommand())
	}
	
	override popupMenuOpenedOn(Node node) {
	}
	
	override popupMenuOpenedOn(Application app) {
	}

}

class ShowTimeSeriesNodeCommand extends NodeCommand {
	override execute() {
		super.execute()
	}
}

class ShowTimeSeriesApplicationCommand extends ApplicationCommand {
	override execute() {
		super.execute()
	}
}

class ShowTimeSeriesComponentCommand extends ComponentCommand {
	override execute() {
		super.execute()
	}
}

class ShowTimeSeriesClazzCommand extends ClazzCommand {
	override execute() {
		super.execute()
	}
}
