package explorviz.visualization.engine.contextmenu.popupmenus

import com.google.gwt.user.client.Command
import explorviz.shared.model.Node
import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.NodeCommand
import java.util.ArrayList
import java.util.List

class NodePopupMenu extends PopupMenu {
	var List<NodeCommand> menuEntries

	override init() {
		menuEntries = new ArrayList<NodeCommand>()
		super.init()
	}

	override addNewEntry(String label, Command command) {
		if (command instanceof NodeCommand) {
			menuEntries.add(command as NodeCommand)
			super.addNewEntry(label, command)
		}
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}

	def void setCurrentNode(Node node) {
		for (entry : menuEntries) {
			entry.currentNode = node
		}
	}
}
