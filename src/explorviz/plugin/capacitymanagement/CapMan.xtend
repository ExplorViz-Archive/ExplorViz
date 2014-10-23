package explorviz.plugin.capacitymanagement

import explorviz.plugin.interfaces.ICapacityManager
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape

class CapMan implements ICapacityManager {

	new() {
		PluginManagerServerSide::registerAsCapacityManager(this)
	}

	override doCapacityManagement(Landscape landscape) {
		// TODO
	}

}
