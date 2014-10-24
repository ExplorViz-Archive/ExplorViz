package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.shared.model.Node
import explorviz.visualization.engine.contextmenu.PopupService

abstract class NodeCommand implements Command {
	protected var Node currentNode

	def setCurrentNode(Node node) {
		currentNode = node
	}

	override void execute() {
		PopupService::hidePopupMenus
	}
}
