package explorviz.visualization.engine.contextmenu.popupmenus

import com.google.gwt.user.client.Command
import explorviz.shared.model.Clazz
import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.ClazzCommand
import explorviz.visualization.engine.contextmenu.commands.ShowSourceCodeCommand
import java.util.ArrayList
import java.util.List

class ClazzPopupMenu extends PopupMenu {
	var List<ClazzCommand> menuEntries

	override init() {
		menuEntries = new ArrayList<ClazzCommand>()
		super.init()
		addNewEntry("Inspect source code", new ShowSourceCodeCommand())
	}

	override addNewEntry(String label, Command command) {
		if (command instanceof ClazzCommand) {
			menuEntries.add(command as ClazzCommand)
			super.addNewEntry(label, command)
		}
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}

	def void setCurrentClazz(Clazz node) {
		for (entry : menuEntries) {
			entry.currentClazz = node
		}
	}
}
