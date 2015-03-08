package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.Node
import explorviz.shared.model.System
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager

class ModelingAddNewNodeCommand implements Command {
	var System currentSystem

	def setCurrentSystem(System system) {
		currentSystem = system
	}

	override execute() {
		PopupService::hidePopupMenus()
		val node = new Node()
		val nodeGroup = new NodeGroup()
		node.parent = nodeGroup
		node.name = "<NEW-NODE>"
		node.ipAddress = "<NO-IPADDRESS>"
		
		nodeGroup.nodes.add(node)
		nodeGroup.setStartAndEndIpRangeAsName
		
		nodeGroup.opened = true
		nodeGroup.parent = currentSystem
		currentSystem.nodeGroups.add(nodeGroup)
		
		LandscapeExchangeManager.saveTargetModelIfInModelingMode(currentSystem.parent)
		
		SceneDrawer::createObjectsFromLandscape(currentSystem.parent, true)
	}
}
