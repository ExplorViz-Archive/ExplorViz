package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.shared.model.Application

class ModelingAddNewCommunicationCommand implements Command {
	var Application currentApp

	def setCurrentApplication(Application app) {
		currentApp = app
	}

	override execute() {
		PopupService::hidePopupMenus()

		// TODO
		LandscapeExchangeManager.saveTargetModelIfInModelingMode(currentApp.parent.parent.parent.parent)

		SceneDrawer::createObjectsFromLandscape(currentApp.parent.parent.parent.parent, true)
	}
}
