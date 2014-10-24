package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.shared.model.Clazz
import explorviz.visualization.engine.contextmenu.PopupService

abstract class ClazzCommand implements Command {
	protected var Clazz currentClazz

	def setCurrentClazz(Clazz clazz) {
		currentClazz = clazz
	}
	
	override void execute() {
		PopupService::hidePopupMenus
	}
}
