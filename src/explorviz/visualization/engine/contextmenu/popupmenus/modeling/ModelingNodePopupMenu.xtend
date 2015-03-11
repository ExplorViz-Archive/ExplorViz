package explorviz.visualization.engine.contextmenu.popupmenus.modeling

import explorviz.shared.model.Node
import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingAddNewApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingConfigureNodeCommand
import explorviz.visualization.engine.contextmenu.commands.modeling.ModelingRemoveNodeCommand

class ModelingNodePopupMenu extends PopupMenu {
	val newApplicationCommand = new ModelingAddNewApplicationCommand()
	val configureCommand = new ModelingConfigureNodeCommand()
	val removeCommand = new ModelingRemoveNodeCommand()

	new() {
		super()
		addNewEntry("Add new application", newApplicationCommand)
		addSeperator()
		addNewEntry("Configure", configureCommand)
		addSeperator()
		addNewEntry("Remove", removeCommand)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}

	def void setCurrentNode(Node node) {
		newApplicationCommand.currentNode = node
		configureCommand.currentNode = node
		removeCommand.currentNode = node
	}
}
