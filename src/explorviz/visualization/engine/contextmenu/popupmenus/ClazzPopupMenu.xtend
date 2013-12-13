package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.commands.ShowSourceCodeCommand
import explorviz.visualization.engine.contextmenu.PopupMenu

class ClazzPopupMenu extends PopupMenu {
	new() {
		super()
		addNewEntry("Inspect source codeZ", new ShowSourceCodeCommand())
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
}