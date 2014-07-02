package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.visualization.engine.contextmenu.commands.ShowSourceCodeCommand
import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.shared.model.Clazz

class ClazzPopupMenu extends PopupMenu {
	var Clazz currentClazz

	var ShowSourceCodeCommand showSourceCode

	new() {
		super()
		showSourceCode = new ShowSourceCodeCommand()
		addNewEntry("Inspect source code", showSourceCode)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}

	def void setCurrentClazz(Clazz clazz) {
		currentClazz = clazz
		showSourceCode.currentClazz = clazz
	}

}
