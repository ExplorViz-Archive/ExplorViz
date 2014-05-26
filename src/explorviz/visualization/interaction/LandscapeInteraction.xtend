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
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.model.SystemClientSide
import explorviz.visualization.experiment.Experiment

class LandscapeInteraction {
	static val MouseDoubleClickHandler systemMouseDblClick = createSystemMouseDoubleClickHandler()
	
	static val MouseDoubleClickHandler nodeGroupMouseDblClick = createNodeGroupMouseDoubleClickHandler()

	static val MouseClickHandler nodeMouseClick = createNodeMouseClickHandler()
	static val MouseDoubleClickHandler nodeMouseDblClick = createNodeMouseDoubleClickHandler()
	static val MouseRightClickHandler nodeRightMouseClick = createNodeMouseRightClickHandler()

	static val MouseClickHandler applicationMouseClick = createApplicationMouseClickHandler()
	static val MouseRightClickHandler applicationMouseRightClick = createApplicationMouseRightClickHandler()
	static val MouseDoubleClickHandler applicationMouseDblClick = createApplicationMouseDoubleClickHandler()

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
		if(!Experiment::tutorial){
			system.setMouseDoubleClickHandler(systemMouseDblClick)
	
			system.nodeGroups.forEach [
				createNodeGroupInteraction(it)
			]
		}else{ //Tutorialmodus active, only set the correct handler, otherwise go further into the system
			if(!Experiment::getStep().isConnection && Experiment::getStep().source.equals(system.name)){
				system.setMouseDoubleClickHandler(systemMouseDblClick)
			}else{
				system.nodeGroups.forEach [
					createNodeGroupInteraction(it)
				]
			}
		}
		
	}
	
	def static private createNodeGroupInteraction(NodeGroupClientSide nodeGroup) {
		if(!Experiment::tutorial){
			nodeGroup.setMouseDoubleClickHandler(nodeGroupMouseDblClick)
	
			nodeGroup.nodes.forEach [
				createNodeInteraction(it)
			]
		}else{//Tutorialmodus active, only set correct handler, otherwise go further into the nodegroup
			if(!Experiment::getStep().isConnection && Experiment::getStep().source.equals(nodeGroup.name)){
				nodeGroup.setMouseDoubleClickHandler(nodeGroupMouseDblClick)
			}else{
				nodeGroup.nodes.forEach [
					createNodeInteraction(it)
				]
			}
		}
	}

	def static private MouseDoubleClickHandler createSystemMouseDoubleClickHandler() {
		[
			val system = (it.object as SystemClientSide)
			Usertracking::trackSystemDoubleClick(system)
			system.opened = !system.opened
			if(Experiment::tutorial){
				val step = Experiment::getStep()
				if(!step.connection && system.name.equals(step.source) && step.opened == system.opened){
					Experiment::incStep()
				}
			}
			SceneDrawer::createObjectsFromLandscape(system.parent, true)
		]
	}
	
	def static private MouseDoubleClickHandler createNodeGroupMouseDoubleClickHandler() {
		[
			val nodeGroup = (it.object as NodeGroupClientSide)
			Usertracking::trackNodeGroupDoubleClick(nodeGroup)
			nodeGroup.opened = !nodeGroup.opened
			if(Experiment::tutorial){
				val step = Experiment::getStep()
				if(!step.connection && nodeGroup.name.equals(step.source) && step.opened == nodeGroup.opened){
					Experiment::incStep()
				}
			}
			SceneDrawer::createObjectsFromLandscape(nodeGroup.parent.parent, true)
		]
	}

	def static private createNodeInteraction(NodeClientSide node) {
		if(!Experiment::tutorial){
			node.setMouseClickHandler(nodeMouseClick)
			node.setMouseRightClickHandler(nodeRightMouseClick)
			node.setMouseDoubleClickHandler(nodeMouseDblClick)
			node.applications.forEach [
				createApplicationInteraction(it)
			]
		}else{//Tutorialmodus active, only set correct handler, otherwise go further into the node
			if(!Experiment::getStep().isConnection && Experiment::getStep().source.equals(node.name)){
				node.setMouseClickHandler(nodeMouseClick)
				node.setMouseRightClickHandler(nodeRightMouseClick)
				node.setMouseDoubleClickHandler(nodeMouseDblClick)
			}else{
				node.applications.forEach [
					createApplicationInteraction(it)
				]
			}
		}
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

	def static private createApplicationInteraction(ApplicationClientSide application) {
		if(!Experiment::tutorial || 
			(!Experiment::getStep().connection && Experiment::getStep().source.equals(application.name))
		){
			application.setMouseClickHandler(applicationMouseClick)
			application.setMouseRightClickHandler(applicationMouseRightClick)
			application.setMouseDoubleClickHandler(applicationMouseDblClick)
		}
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
			if(Experiment::tutorial){
				val step = Experiment::getStep()
				if(!step.connection && app.name.equals(step.source) && step.opened){
					Experiment::incStep()
				}
			}
			SceneDrawer::createObjectsFromApplication(app, true)
		]
	}

	def static private createCommunicationInteraction(CommunicationClientSide communication) {
		if(!Experiment::tutorial || 
			(Experiment::getStep().connection && 
				communication.source.name.equals(Experiment::getStep().source) && 
				communication.target.name.equals(Experiment::getStep().dest))){
			communication.setMouseClickHandler(communicationMouseClickHandler)
		}
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
