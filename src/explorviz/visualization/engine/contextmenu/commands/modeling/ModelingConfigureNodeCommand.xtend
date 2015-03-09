package explorviz.visualization.engine.contextmenu.commands.modeling

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.shared.model.Node

class ModelingConfigureNodeCommand implements Command {
	var Node currentNode

	def setCurrentNode(Node node) {
		currentNode = node
	}

	override execute() {
		PopupService::hidePopupMenus()
		
		val nodeGroup = currentNode.parent
		val system = nodeGroup.parent
		val landscape = system.parent
		
		// TODO
		
		LandscapeExchangeManager.saveTargetModelIfInModelingMode(landscape)
		
		SceneDrawer::createObjectsFromLandscape(landscape, true)
	}
}
