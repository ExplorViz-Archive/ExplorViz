package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.DummyCommand

class NodePopupMenu extends PopupMenu {
	new() {
		super()
		addNewEntry("Show details", new DummyCommand())
		addSeperator()
		addNewEntry("Restart", new DummyCommand())
		addNewEntry("Terminate", new DummyCommand())
		addSeperator()
		addNewEntry("Start new instance of same type", new DummyCommand())
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
}