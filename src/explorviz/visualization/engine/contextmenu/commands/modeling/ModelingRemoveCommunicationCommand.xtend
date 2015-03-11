package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.shared.model.helper.CommunicationTileAccumulator

class ModelingRemoveCommunicationCommand implements Command {
	var CommunicationTileAccumulator currentCommunication

	def setCurrentCommunication(CommunicationTileAccumulator commu) {
		currentCommunication = commu
	}

	override execute() {
		PopupService::hidePopupMenus()

		if (!currentCommunication.communications.empty) {
			val landscape = currentCommunication.communications.get(0).source.parent.parent.parent.parent
			
			for (commu : currentCommunication.communications) {
				landscape.applicationCommunication.remove(commu)
			}

			LandscapeExchangeManager.saveTargetModelIfInModelingMode(landscape)

			SceneDrawer::createObjectsFromLandscape(landscape, true)
		}
	}
}
