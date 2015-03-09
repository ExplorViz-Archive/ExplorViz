package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.shared.model.helper.CommunicationTileAccumulator
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.modelingexchange.ModelingDialogJS

class ModelingConfigureCommunicationCommand implements Command {
	var CommunicationTileAccumulator currentCommunication

	def setCurrentCommunication(CommunicationTileAccumulator commu) {
		currentCommunication = commu
	}

	override execute() {
		PopupService::hidePopupMenus()

		if (!currentCommunication.communications.empty) {
			ModelingDialogJS::configureCommunication(currentCommunication)
		}
	}
}
