package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.shared.model.System
import explorviz.visualization.modelingexchange.ModelingDialogJS

class ModelingConfigureSystemCommand implements Command {
	var System currentSystem

	def setCurrentSystem(System system) {
		currentSystem = system
	}

	override execute() {
		PopupService::hidePopupMenus()
		
		ModelingDialogJS::configureSystem(currentSystem)
	}
}
