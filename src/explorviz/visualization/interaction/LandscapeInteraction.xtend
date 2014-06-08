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
import explorviz.visualization.experiment.Experiment

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
		if(!Experiment::tutorial){
			system.setMouseDoubleClickHandler(systemMouseDblClick)
	
			system.nodeGroups.forEach [
				createNodeGroupInteraction(it)
			]
		}else{ //Tutorialmodus active, only set the correct handler, otherwise go further into the system
			val step = Experiment::getStep()
			if(!step.isConnection && step.source.equals(system.name) && step.doubleClick){
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
			val step = Experiment::getStep()
			if(!step.isConnection && step.source.equals(nodeGroup.name) && step.doubleClick){
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
			Experiment::incTutorial(system.name, false, false, true)
			SceneDrawer::createObjectsFromLandscape(system.parent, true)
		]
	}

	def static private MouseDoubleClickHandler createNodeGroupMouseDoubleClickHandler() {
		[
			val nodeGroup = (it.object as NodeGroupClientSide)
			Usertracking::trackNodeGroupDoubleClick(nodeGroup)
			nodeGroup.opened = !nodeGroup.opened
			Experiment::incTutorial(nodeGroup.name, false, false, true)
			SceneDrawer::createObjectsFromLandscape(nodeGroup.parent.parent, true)
		]
	}

	def static private createNodeInteraction(NodeClientSide node) {
		if(!Experiment::tutorial){
			node.setMouseClickHandler(nodeMouseClick)
			node.setMouseRightClickHandler(nodeRightMouseClick)
			node.setMouseDoubleClickHandler(nodeMouseDblClick)
			node.setMouseHoverHandler(nodeMouseHoverClick)
			node.applications.forEach [
				createApplicationInteraction(it)
			]
		}else{//Tutorialmodus active, only set correct handler, otherwise go further into the node
			val step = Experiment::getStep()
			if(!step.isConnection && step.source.equals(node.name)){
				if(step.leftClick){
					node.setMouseClickHandler(nodeMouseClick)
				}else if(step.rightClick){
					node.setMouseRightClickHandler(nodeRightMouseClick)
				}else if(step.doubleClick){
					node.setMouseDoubleClickHandler(nodeMouseDblClick)
				}
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
//			incStep(node.name, true, false, false)
		]
	}

	def static private MouseDoubleClickHandler createNodeMouseDoubleClickHandler() {
		[
			//			val node = (it.object as NodeClientSide)
//			incTutorial(node.name, false, false, true)
		]
	}

	def static private MouseRightClickHandler createNodeMouseRightClickHandler() {
		[
			val node = it.object as NodeClientSide
			//						Window::confirm("Warning The software landscape violates its requirements for response times.\nCountermeasure\nIt is suggested to start a new node of type 'm1.small' with the application 'Neo4J' on it.\n\nAfter the change, the response time is improved and the operating costs increase by 5 Euro per hour.\n\nStart the instance?")
			//						Window::confirm("SLAstic suggests to shutdown the node with IP '10.0.0.4'.\n\nTerminate this instance?")
			Usertracking::trackNodeRightClick(node);
			Experiment::incTutorial(node.name, false, true, false)
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
		if(!Experiment::tutorial){
			application.setMouseClickHandler(applicationMouseClick)
			application.setMouseRightClickHandler(applicationMouseRightClick)
			application.setMouseDoubleClickHandler(applicationMouseDblClick)
			application.setMouseHoverHandler(applicationMouseHoverClick)
		}
		else if(!Experiment::getStep().connection && Experiment::getStep().source.equals(application.name)){
			val step = Experiment::getStep()
			if(step.leftClick){
				application.setMouseClickHandler(applicationMouseClick)
			}else if(step.rightClick){
				application.setMouseRightClickHandler(applicationMouseRightClick)
			}else if(step.doubleClick){
				application.setMouseDoubleClickHandler(applicationMouseDblClick)
			}
		}	
	}

	def static MouseClickHandler createApplicationMouseClickHandler() {
		[
//			incTutorial(app.name, true, false, false)
		]
	}

	def static MouseRightClickHandler createApplicationMouseRightClickHandler() {
		[
			val app = it.object as ApplicationClientSide
			Usertracking::trackApplicationRightClick(app);
			Experiment::incTutorial(app.name, false, true, false)
			PopupService::showApplicationPopupMenu(it.originalClickX, it.originalClickY, app)
		]
	}

	def static MouseDoubleClickHandler createApplicationMouseDoubleClickHandler() {
		[
			val app = it.object as ApplicationClientSide
			Usertracking::trackApplicationDoubleClick(app);
			Experiment::incTutorial(app.name, false, false, true)
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
		if(!Experiment::tutorial || 
			(Experiment::getStep().connection && 
				communication.source.name.equals(Experiment::getStep().source) && 
				communication.target.name.equals(Experiment::getStep().dest) &&
				Experiment::getStep().leftClick)){
			communication.setMouseClickHandler(communicationMouseClickHandler)
		}
	}

	def static private MouseClickHandler createCommunicationMouseClickHandler() {
		[
//						val communication = (it.object as CommunicationClientSide)
//						Experiment::incTutorial(communication.source.name, communication.target.name, true, false)
//						Window::alert(
//							"Clicked communication between " + communication.source.name + " and " + communication.target.name +
//								" with requests per second: " + communication.requestsPerSecond)
		]
	}
	
}
