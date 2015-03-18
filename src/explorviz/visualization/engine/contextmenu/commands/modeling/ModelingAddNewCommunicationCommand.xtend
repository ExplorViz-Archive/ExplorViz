package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.shared.model.Application
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.modelingexchange.ModelingDialogJS

class ModelingAddNewCommunicationCommand implements Command {
	var Application currentApp

	def setCurrentApplication(Application app) {
		currentApp = app
	}

	override execute() {
		PopupService::hidePopupMenus()

		ModelingDialogJS::addNewCommunication(currentApp)
	}
}
