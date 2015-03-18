package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.shared.model.Node
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.modelingexchange.ModelingDialogJS

class ModelingConfigureNodeCommand implements Command {
	var Node currentNode

	def setCurrentNode(Node node) {
		currentNode = node
	}

	override execute() {
		PopupService::hidePopupMenus()

		ModelingDialogJS::configureNode(currentNode)
	}
}
