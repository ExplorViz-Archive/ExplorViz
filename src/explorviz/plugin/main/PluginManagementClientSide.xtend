package explorviz.plugin.main

import explorviz.plugin.anomalydetection.OPADxClientSide
import explorviz.plugin.capacitymanagement.CapManClientSide
import explorviz.plugin.interfaces.IPluginClientSide
import explorviz.shared.model.Node
import explorviz.visualization.main.PluginManagerClientSide
import java.util.ArrayList
import java.util.List
import explorviz.shared.model.Application
import explorviz.visualization.engine.main.SceneDrawer

class PluginManagementClientSide {
	val static List<IPluginClientSide> plugins = new ArrayList<IPluginClientSide>()

	def static void init() {
		plugins.add(new OPADxClientSide())
		plugins.add(new CapManClientSide())

		switchedToPerspective(Perspective::SYMPTOMS)
	}

	def static void switchedToPerspective(Perspective perspective) {
		SceneDrawer::redraw
		
		PluginManagerClientSide::clearNodePopup()
		PluginManagerClientSide::clearApplicationPopup()
		PluginManagerClientSide::clearComponentPopup()
		PluginManagerClientSide::clearClazzPopup()

		for (plugin : plugins) {
			plugin.switchedToPerspective(perspective)
		}
	}

	def static void popupMenuOpenedOn(Node node) {
		for (plugin : plugins) {
			plugin.popupMenuOpenedOn(node)
		}
	}
	
	def static void popupMenuOpenedOn(Application application) {
		for (plugin : plugins) {
			plugin.popupMenuOpenedOn(application)
		}
	}
}
