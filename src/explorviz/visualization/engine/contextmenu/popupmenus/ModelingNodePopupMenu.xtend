package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.ModelingAddNewApplicationCommand
import explorviz.shared.model.Node

class ModelingNodePopupMenu extends PopupMenu {
	val newApplicationCommand = new ModelingAddNewApplicationCommand()
	
	new() {
		super()
		addNewEntry("Add new application", newApplicationCommand)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
	
	def void setCurrentNode(Node node) {
		newApplicationCommand.currentNode = node
	}
}