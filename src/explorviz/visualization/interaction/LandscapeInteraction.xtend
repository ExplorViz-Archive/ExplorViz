package explorviz.visualization.interaction

import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseHoverHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.engine.popover.PopoverService
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.CommunicationClientSide
import explorviz.visualization.model.LandscapeClientSide
import explorviz.visualization.model.NodeClientSide
import explorviz.visualization.model.NodeGroupClientSide
import explorviz.visualization.model.SystemClientSide

class LandscapeInteraction {
	static val MouseDoubleClickHandler systemMouseDblClick = createSystemMouseDoubleClickHandler()

	static val MouseDoubleClickHandler nodeGroupMouseDblClick = createNodeGroupMouseDoubleClickHandler()

	static val MouseClickHandler nodeMouseClick = createNodeMouseClickHandler()
	static val MouseDoubleClickHandler nodeMouseDblClick = createNodeMouseDoubleClickHandler()
	static val MouseRightClickHandler nodeRightMouseClick = createNodeMouseRightClickHandler()
	static val MouseHoverHandler nodeMouseHoverClick = createNodeMouseHoverHandler()

	static val MouseClickHandler applicationMouseClick = createApplicationMouseClickHandler()
	static val MouseRightClickHandler applicationMouseRightClick = createApplicationMouseRightClickHandler()
	static val MouseDoubleClickHandler applicationMouseDblClick = createApplicationMouseDoubleClickHandler()
	static val MouseHoverHandler applicationMouseHoverClick = createApplicationMouseHoverHandler()

	static val MouseClickHandler communicationMouseClickHandler = createCommunicationMouseClickHandler()

	def static void clearInteraction(LandscapeClientSide landscape) {
		ObjectPicker::clear()

		landscape.systems.forEach [ system |
			system.clearAllHandlers()
			system.nodeGroups.forEach [
				it.clearAllHandlers()
				it.nodes.forEach [
					it.clearAllHandlers()
					it.applications.forEach [
						it.clearAllHandlers()
					]
				]
			]
		]
		landscape.applicationCommunication.forEach [
			it.clearAllHandlers()
		]
	}

	def static void createInteraction(LandscapeClientSide landscape) {
		landscape.systems.forEach [
			createSystemInteraction(it)
		]

		landscape.applicationCommunication.forEach [
			createCommunicationInteraction(it)
		]
	}

	def static private createSystemInteraction(SystemClientSide system) {
		system.setMouseDoubleClickHandler(systemMouseDblClick)

		system.nodeGroups.forEach [
			createNodeGroupInteraction(it)
		]
	}

	def static private createNodeGroupInteraction(NodeGroupClientSide nodeGroup) {
		nodeGroup.setMouseDoubleClickHandler(nodeGroupMouseDblClick)

		nodeGroup.nodes.forEach [
			createNodeInteraction(it)
		]
	}

	def static private MouseDoubleClickHandler createSystemMouseDoubleClickHandler() {
		[
			val system = (it.object as SystemClientSide)
			Usertracking::trackSystemDoubleClick(system)
			system.opened = !system.opened
			SceneDrawer::createObjectsFromLandscape(system.parent, true)
		]
	}

	def static private MouseDoubleClickHandler createNodeGroupMouseDoubleClickHandler() {
		[
			val nodeGroup = (it.object as NodeGroupClientSide)
			Usertracking::trackNodeGroupDoubleClick(nodeGroup)
			nodeGroup.opened = !nodeGroup.opened
			SceneDrawer::createObjectsFromLandscape(nodeGroup.parent.parent, true)
		]
	}

	def static private createNodeInteraction(NodeClientSide node) {
		node.setMouseClickHandler(nodeMouseClick)
		node.setMouseDoubleClickHandler(nodeMouseDblClick)
		node.setMouseRightClickHandler(nodeRightMouseClick)
		node.setMouseHoverHandler(nodeMouseHoverClick)

		node.applications.forEach [
			createApplicationInteraction(it)
		]
	}

	def static private MouseClickHandler createNodeMouseClickHandler() {
		[
			Usertracking::trackNodeClick(it.object as NodeClientSide)
		]
	}

	def static private MouseDoubleClickHandler createNodeMouseDoubleClickHandler() {
		[
			//			val node = (it.object as NodeClientSide)
		]
	}

	def static private MouseRightClickHandler createNodeMouseRightClickHandler() {
		[
			val node = it.object as NodeClientSide
			//						Window::confirm("Warning The software landscape violates its requirements for response times.\nCountermeasure\nIt is suggested to start a new node of type 'm1.small' with the application 'Neo4J' on it.\n\nAfter the change, the response time is improved and the operating costs increase by 5 Euro per hour.\n\nStart the instance?")
			//						Window::confirm("SLAstic suggests to shutdown the node with IP '10.0.0.4'.\n\nTerminate this instance?")
			Usertracking::trackNodeRightClick(node);
			PopupService::showNodePopupMenu(it.originalClickX, it.originalClickY, node)
		]
	}

	def static private MouseHoverHandler createNodeMouseHoverHandler() {
		[
			val node = it.object as NodeClientSide
			// TODO
			//			Usertracking::trackNodeRightClick(node);
			PopoverService::showPopover(node.ipAddress + " Information", it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>CPU Utilization:</td><td>' + Math.round(node.cpuUtilization * 100) +
					'%</td></tr><tr><td>Total RAM:</td><td>' + getTotalRAMInGB(node) +
					' GB</td></tr><tr><td>Free RAM:</td><td>' + getFreeRAMInPercent(node) + '%</td></tr></table>')
		]
	}

	private def static getTotalRAMInGB(NodeClientSide node) {
		Math.round((node.usedRAM + node.freeRAM) / (1024f * 1024f))
	}

	private def static getFreeRAMInPercent(NodeClientSide node) {
		val totalRAM = node.usedRAM + node.freeRAM
		if (totalRAM > 0) {
		Math.round(node.freeRAM / totalRAM)
		} else {
			0
		}
	}

	def static private createApplicationInteraction(ApplicationClientSide application) {
		application.setMouseClickHandler(applicationMouseClick)
		application.setMouseRightClickHandler(applicationMouseRightClick)
		application.setMouseDoubleClickHandler(applicationMouseDblClick)
		application.setMouseHoverHandler(applicationMouseHoverClick)
	}

	def static MouseClickHandler createApplicationMouseClickHandler() {
		[]
	}

	def static MouseRightClickHandler createApplicationMouseRightClickHandler() {
		[
			val app = it.object as ApplicationClientSide
			Usertracking::trackApplicationRightClick(app);
			PopupService::showApplicationPopupMenu(it.originalClickX, it.originalClickY, app)
		]
	}

	def static MouseDoubleClickHandler createApplicationMouseDoubleClickHandler() {
		[
			val app = it.object as ApplicationClientSide
			Usertracking::trackApplicationDoubleClick(app);
			SceneDrawer::createObjectsFromApplication(app, true)
		]
	}
	
	def static private MouseHoverHandler createApplicationMouseHoverHandler() {
		[
			val application = it.object as ApplicationClientSide
			// TODO
			//			Usertracking::trackNodeRightClick(node);
			PopoverService::showPopover(application.name + " Information", it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>None</td><td>' + '' + '</td></tr></table>')
		]
	}

	def static private createCommunicationInteraction(CommunicationClientSide communication) {
		communication.setMouseClickHandler(communicationMouseClickHandler)
	}

	def static private MouseClickHandler createCommunicationMouseClickHandler() {
		[
			//			val communication = (it.object as CommunicationClientSide)
			//			Window::alert(
			//				"Clicked communication between " + communication.source.name + " and " + communication.target.name +
			//					" with requests per second: " + communication.requestsPerSecond)
		]
	}
}
