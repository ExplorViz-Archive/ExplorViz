package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.commands.ShowSourceCodeCommand
import explorviz.visualization.engine.contextmenu.PopupMenu

class ComponentPopupMenu extends PopupMenu {
	new() {
		super()
		addNewEntry("Inspect source code", new ShowSourceCodeCommand())
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
}