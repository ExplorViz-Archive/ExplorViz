package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.shared.model.System
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager

class ModelingRemoveSystemCommand implements Command {
	var System currentSystem

	def setCurrentSystem(System system) {
		currentSystem = system
	}

	override execute() {
		PopupService::hidePopupMenus()
		
		val landscape = currentSystem.parent
		
		landscape.systems.remove(currentSystem)
		
		LandscapeExchangeManager.saveTargetModelIfInModelingMode(landscape)
		
		SceneDrawer::createObjectsFromLandscape(landscape, true)
	}
}
