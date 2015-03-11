package explorviz.visualization.engine.contextmenu.popupmenus.modeling

import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.shared.model.Application
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingAddNewCommunicationCommand
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingConfigureApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingRemoveApplicationCommand

class ModelingApplicationPopupMenu extends PopupMenu {
	val newCommunicationCommand = new ModelingAddNewCommunicationCommand()
	val configureCommand = new ModelingConfigureApplicationCommand()
	val removeCommand = new ModelingRemoveApplicationCommand()
	
	new() {
		super()
		addNewEntry("Add new communication", newCommunicationCommand)
		addSeperator()
		addNewEntry("Configure", configureCommand)
		addSeperator()
		addNewEntry("Remove", removeCommand)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
	
	def void setCurrentApplication(Application app) {
		newCommunicationCommand.currentApplication = app
		configureCommand.currentApplication = app
		removeCommand.currentApplication = app
	}
}