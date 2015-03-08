package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.shared.model.helper.CommunicationTileAccumulator

class ModelingCommunicationPopupMenu extends PopupMenu {
	new() {
		super()
//		addNewEntry("Add new application", newApplicationCommand)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
	
	def void setCurrentCommunication(CommunicationTileAccumulator commu) {
//		newApplicationCommand.currentNode = node
	}
}