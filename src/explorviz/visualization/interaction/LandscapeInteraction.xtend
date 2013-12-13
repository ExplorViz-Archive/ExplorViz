package explorviz.visualization.interaction

import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.CommunicationClientSide
import explorviz.visualization.model.LandscapeClientSide
import explorviz.visualization.model.NodeClientSide
import explorviz.visualization.model.NodeGroupClientSide

class LandscapeInteraction {
	def static void clearInteraction(LandscapeClientSide landscape) {
		landscape.nodeGroups.forEach [
			it.clearAllHandlers()
			it.nodes.forEach [
				it.clearAllHandlers()
				it.applications.forEach [
					it.clearAllHandlers()
				]
			]			
		]
		landscape.applicationCommunication.forEach [
			it.clearAllHandlers()
		]
	}
	
	def static void createInteraction(LandscapeClientSide landscape) {
		landscape.nodeGroups.forEach[
			createNodeGroupInteraction(it)
		]
		
		landscape.applicationCommunication.forEach[
			createCommunicationInteraction(it)
		]
	}
	
	def static private createNodeGroupInteraction(NodeGroupClientSide nodeGroup) {
		nodeGroup.addMouseDoubleClickHandler(createNodeGroupMouseDoubleClickHandler())
		
		nodeGroup.nodes.forEach[
			createNodeInteraction(it)
		]
	}
	
	def static private MouseDoubleClickHandler createNodeGroupMouseDoubleClickHandler() {
		[
			val nodeGroup = (it.object as NodeGroupClientSide)
			Usertracking::trackNodeGroupDoubleClick(nodeGroup)
			nodeGroup.opened = !nodeGroup.opened
			SceneDrawer::createObjectsFromLandscape(nodeGroup.parent, true)
		]
	}
	
	def static private createNodeInteraction(NodeClientSide node) {
		node.addMouseClickHandler(createNodeMouseClickHandler())
		node.addMouseRightClickHandler(createNodeMouseRightClickHandler())
		
		node.applications.forEach[
			createApplicationInteraction(it)
		]
	}
	
	def static private MouseClickHandler createNodeMouseClickHandler() {
		[
			Usertracking::trackNodeClick(it.object as NodeClientSide)
		]
	}
	
	def static private MouseRightClickHandler createNodeMouseRightClickHandler() {
		[
			val node = it.object as NodeClientSide
//			Window::confirm("SLAstic suggests to start a new node with configuration 'Worker' and type 'm1.small'.\n\nStart the instance?")
//			Window::confirm("SLAstic suggests to shutdown the node with IP '10.0.0.4'.\n\nTerminate this instance?")
			Usertracking::trackNodeRightClick(node);
			PopupService::showNodePopupMenu(it.originalClickX, it.originalClickY, node)
		]
	}
	
	def static private createApplicationInteraction(ApplicationClientSide application) {
		application.addMouseClickHandler([])
		application.addMouseRightClickHandler(createApplicationMouseRightClickHandler())
		application.addMouseDoubleClickHandler(createApplicationMouseDoubleClickHandler())
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
	
	def static private createCommunicationInteraction(CommunicationClientSide communication) {
		communication.addMouseClickHandler(createCommunicationMouseClickHandler())
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