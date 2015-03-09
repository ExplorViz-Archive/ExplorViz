package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.shared.model.Node
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.shared.model.Application
import explorviz.shared.model.helper.ELanguage

class ModelingAddNewApplicationCommand implements Command {
	var Node currentNode

	def setCurrentNode(Node node) {
		currentNode = node
	}

	override execute() {
		PopupService::hidePopupMenus()
		val application = new Application()
		application.name = "<NEW-APPLICATION>"
		application.parent = currentNode
		application.id = 1
		application.database = false
		application.programmingLanguage = ELanguage.UNKNOWN
		currentNode.applications.add(application)
		
		LandscapeExchangeManager.saveTargetModelIfInModelingMode(currentNode.parent.parent.parent)
		
		SceneDrawer::createObjectsFromLandscape(currentNode.parent.parent.parent, true)
	}
}
