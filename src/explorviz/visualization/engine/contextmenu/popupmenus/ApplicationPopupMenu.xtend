package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.DummyCommand

class ApplicationPopupMenu extends PopupMenu {
	
	new() {
		super()
		addNewEntry("Jump into", new DummyCommand())
		addSeperator()
		addNewEntry("Stop", new DummyCommand())
		addNewEntry("Restart", new DummyCommand())
		addSeperator()
		addNewEntry("Migrate", new DummyCommand())
		addNewEntry("Replicate", new DummyCommand())
		addSeperator()
		addNewEntry("Configure monitoring", new DummyCommand())
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
}
