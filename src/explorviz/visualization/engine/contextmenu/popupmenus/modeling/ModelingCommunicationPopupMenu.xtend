package explorviz.visualization.engine.contextmenu.popupmenus.modeling

import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.shared.model.helper.CommunicationTileAccumulator
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingRemoveCommunicationCommand
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingConfigureCommunicationCommand

class ModelingCommunicationPopupMenu extends PopupMenu {
	val configureCommand = new ModelingConfigureCommunicationCommand()
	val removeCommand = new ModelingRemoveCommunicationCommand()
	
	new() {
		super()
		addNewEntry("Configure", configureCommand)
		addSeperator()
		addNewEntry("Remove", removeCommand)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
	
	def void setCurrentCommunication(CommunicationTileAccumulator commu) {
		configureCommand.currentCommunication = commu
		removeCommand.currentCommunication = commu
	}
}