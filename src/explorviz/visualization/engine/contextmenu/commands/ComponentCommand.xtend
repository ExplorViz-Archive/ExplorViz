package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.shared.model.Component
import explorviz.visualization.engine.contextmenu.PopupService

abstract class ComponentCommand implements Command {
	protected var Component currentComponent

	def setCurrentComponent(Component compo) {
		currentComponent = compo
	}
	
	override void execute() {
		PopupService::hidePopupMenus
	}
}
