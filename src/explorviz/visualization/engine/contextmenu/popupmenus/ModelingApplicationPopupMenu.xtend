package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.shared.model.Application
import explorviz.visualization.engine.contextmenu.commands.ModelingAddNewCommunicationCommand

class ModelingApplicationPopupMenu extends PopupMenu {
	val newCommunicationCommand = new ModelingAddNewCommunicationCommand()
	
	new() {
		super()
		addNewEntry("Add new communication", newCommunicationCommand)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
	
	def void setCurrentApplication(Application app) {
		newCommunicationCommand.currentApplication = app
	}
}