package explorviz.visualization.engine.contextmenu.popupmenus.modeling

import explorviz.shared.model.System
import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingAddNewNodeCommand
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingConfigureSystemCommand
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingRemoveSystemCommand

class ModelingSystemPopupMenu extends PopupMenu {
	val newNodeCommand = new ModelingAddNewNodeCommand()
	val configureCommand = new ModelingConfigureSystemCommand()
	val removeCommand = new ModelingRemoveSystemCommand()

	new() {
		super()
		addNewEntry("Add new node", newNodeCommand)
		addSeperator()
		addNewEntry("Configure", configureCommand)
		addSeperator()
		addNewEntry("Remove", removeCommand)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}

	def void setCurrentSystem(System system) {
		newNodeCommand.currentSystem = system
		configureCommand.currentSystem = system
		removeCommand.currentSystem = system
	}
}
