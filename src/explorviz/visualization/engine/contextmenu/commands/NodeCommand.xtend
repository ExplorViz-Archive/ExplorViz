package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.shared.model.Node

abstract class NodeCommand implements Command {
	protected var Node currentNode

	def setCurrentNode(Node node) {
		currentNode = node
	}
}
