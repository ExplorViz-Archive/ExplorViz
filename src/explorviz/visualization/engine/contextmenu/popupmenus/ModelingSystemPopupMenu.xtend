package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.ModelingAddNewNodeCommand
import explorviz.shared.model.System

class ModelingSystemPopupMenu extends PopupMenu {
	val newNodeCommand = new ModelingAddNewNodeCommand()
	
	new() {
		super()
		addNewEntry("Add new node", newNodeCommand)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
	
	def void setCurrentSystem(System system) {
		newNodeCommand.currentSystem = system
	}
}