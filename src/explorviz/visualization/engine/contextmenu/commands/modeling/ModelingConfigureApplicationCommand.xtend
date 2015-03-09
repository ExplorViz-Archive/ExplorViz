package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.shared.model.Application

class ModelingConfigureApplicationCommand implements Command {
	var Application currentApplication

	def setCurrentApplication(Application app) {
		currentApplication = app
	}

	override execute() {
		PopupService::hidePopupMenus()
		
		// TODO
		
		val landscape = currentApplication.parent.parent.parent.parent		
		
		LandscapeExchangeManager.saveTargetModelIfInModelingMode(landscape)
		
		SceneDrawer::createObjectsFromLandscape(landscape, true)
	}
}
