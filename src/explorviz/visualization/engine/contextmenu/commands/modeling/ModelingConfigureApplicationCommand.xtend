package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.shared.model.Application
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.modelingexchange.ModelingDialogJS

class ModelingConfigureApplicationCommand implements Command {
	var Application currentApplication

	def setCurrentApplication(Application app) {
		currentApplication = app
	}

	override execute() {
		PopupService::hidePopupMenus()

		ModelingDialogJS::configureApplication(currentApplication)
	}
}
