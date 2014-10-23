package explorviz.plugin.anomalydetection

import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.NodeCommand
import explorviz.visualization.main.PluginManagerClientSide

class OPADxClientSide {
	new() {
		PluginManagerClientSide::addNodePopupEntry("Show Time Series", new ShowTimeSeriesNodeCommand())
		
		PluginManagerClientSide::addApplicationPopupSeperator
		PluginManagerClientSide::addApplicationPopupEntry("Show Time Series", new ShowTimeSeriesApplicationCommand())
	}
}

class ShowTimeSeriesNodeCommand extends NodeCommand {
	override execute() {
	}
}

class ShowTimeSeriesApplicationCommand extends ApplicationCommand {
	override execute() {
	}
}