package explorviz.plugin.capacitymanagement

import explorviz.plugin.interfaces.ICapacityManager
import explorviz.shared.model.Landscape
import explorviz.server.main.PluginManager

class CapMan implements ICapacityManager {

	new() {
		PluginManager::registerAsCapacityManager(this)
	}

	override doCapacityManagement(Landscape landscape) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

}
