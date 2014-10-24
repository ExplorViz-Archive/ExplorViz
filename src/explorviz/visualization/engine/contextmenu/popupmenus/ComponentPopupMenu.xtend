package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.ComponentCommand
import java.util.List
import java.util.ArrayList
import com.google.gwt.user.client.Command
import explorviz.shared.model.Component

class ComponentPopupMenu extends PopupMenu {
	var List<ComponentCommand> menuEntries

	override init() {
		menuEntries = new ArrayList<ComponentCommand>()
		
		super.init()
	}

	override addNewEntry(String label, Command command) {
		if (command instanceof ComponentCommand) {
			menuEntries.add(command as ComponentCommand)
			super.addNewEntry(label, command)
		}
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
	
	def void setCurrentComponent(Component node) {
		for (entry : menuEntries) {
			entry.currentComponent = node
		}
	}
}