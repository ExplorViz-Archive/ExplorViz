package explorviz.visualization.interaction

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import explorviz.shared.model.Application
import explorviz.shared.model.Communication
import explorviz.shared.model.Landscape
import explorviz.shared.model.System
import explorviz.shared.model.Node
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseHoverHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.engine.popover.PopoverService
import explorviz.visualization.experiment.Experiment
import explorviz.shared.model.NodeGroup

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

	def static void clearInteraction(Landscape landscape) {
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

	def static void createInteraction(Landscape landscape) {
		landscape.systems.forEach [
			createSystemInteraction(it)
		]

		landscape.applicationCommunication.forEach [
			createCommunicationInteraction(it)
		]
	}

	def static private createSystemInteraction(System system) {
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

	def static private createNodeGroupInteraction(NodeGroup nodeGroup) {
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
			val system = (it.object as System)
			Usertracking::trackSystemDoubleClick(system)
			system.opened = !system.opened
			Experiment::incTutorial(system.name, false, false, true, false)
			SceneDrawer::createObjectsFromLandscape(system.parent, true)
		]
	}

	def static private MouseDoubleClickHandler createNodeGroupMouseDoubleClickHandler() {
		[
			val nodeGroup = (it.object as NodeGroup)
			Usertracking::trackNodeGroupDoubleClick(nodeGroup)
			nodeGroup.opened = !nodeGroup.opened
			Experiment::incTutorial(nodeGroup.name, false, false, true, false)
			SceneDrawer::createObjectsFromLandscape(nodeGroup.parent.parent, true)
		]
	}

	def static private createNodeInteraction(Node node) {
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
				}else if(step.hover){
					node.setMouseHoverHandler(nodeMouseHoverClick)
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
			Usertracking::trackNodeClick(it.object as Node)
//			incStep(node.name, true, false, false, false)
		]
	}

	def static private MouseDoubleClickHandler createNodeMouseDoubleClickHandler() {
		[
			//			val node = (it.object as Node)
//			incTutorial(node.name, false, false, true, false)
		]
	}

	def static private MouseRightClickHandler createNodeMouseRightClickHandler() {
		[
			val node = it.object as Node
			//						Window::confirm("Warning The software landscape violates its requirements for response times.\nCountermeasure\nIt is suggested to start a new node of type 'm1.small' with the application 'Neo4J' on it.\n\nAfter the change, the response time is improved and the operating costs increase by 5 Euro per hour.\n\nStart the instance?")
			//						Window::confirm("SLAstic suggests to shutdown the node with IP '10.0.0.4'.\n\nTerminate this instance?")
			Usertracking::trackNodeRightClick(node);
			Experiment::incTutorial(node.name, false, true, false, false)
			PopupService::showNodePopupMenu(it.originalClickX, it.originalClickY, node)
		]
	}

	def static private MouseHoverHandler createNodeMouseHoverHandler() {
		[
			val node = it.object as Node
			// TODO
			//			Usertracking::trackNodeRightClick(node);
			val name = if (node.ipAddress != null && !node.ipAddress.isEmpty && node.ipAddress != "<UNKNOWN-IP>") node.ipAddress else node.name
			Experiment::incTutorial(name, false, false, false, true)
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(name) + " Information", it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>CPU Utilization:</td><td>' + Math.round(node.cpuUtilization * 100f) +
					'%</td></tr><tr><td>Total RAM:</td><td>' + getTotalRAMInGB(node) +
					' GB</td></tr><tr><td>Free RAM:</td><td>' + getFreeRAMInPercent(node) + '%</td></tr></table>')
		]
	}

	private def static getTotalRAMInGB(Node node) {
		Math.round((node.usedRAM + node.freeRAM) / (1024f * 1024f * 1024f))
	}

	private def static getFreeRAMInPercent(Node node) {
		val totalRAM = node.usedRAM + node.freeRAM
		if (totalRAM > 0L) {
			Math.round(((node.freeRAM as double) / (totalRAM as double)) * 100f)
		} else {
			2
		}
	}

	def static private createApplicationInteraction(Application application) {
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
			}else if(step.hover){
				application.setMouseHoverHandler(applicationMouseHoverClick)
			}
		}	
	}

	def static MouseClickHandler createApplicationMouseClickHandler() {
		[
//			incTutorial(app.name, true, false, false, false)
		]
	}

	def static MouseRightClickHandler createApplicationMouseRightClickHandler() {
		[
			val app = it.object as Application
			Usertracking::trackApplicationRightClick(app);
			Experiment::incTutorial(app.name, false, true, false, false)
			PopupService::showApplicationPopupMenu(it.originalClickX, it.originalClickY, app)
		]
	}

	def static MouseDoubleClickHandler createApplicationMouseDoubleClickHandler() {
		[
			val app = it.object as Application
			Usertracking::trackApplicationDoubleClick(app);
			Experiment::incTutorial(app.name, false, false, true, false)
			SceneDrawer::createObjectsFromApplication(app, false)
		]
	}
	
	def static private MouseHoverHandler createApplicationMouseHoverHandler() {
		[
//			val application = it.object as Application
			// TODO
			//			Usertracking::trackNodeRightClick(node);
			//Experiment::incTutorial(application.name, false, false, false, true)
//			PopoverService::showPopover(application.name + " Information", it.originalClickX, it.originalClickY,
//				'<table style="width:100%"><tr><td>None</td><td>' + '' + '</td></tr></table>')
		]
	}

	def static private createCommunicationInteraction(Communication communication) {
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
//						val communication = (it.object as Communication)
//						Experiment::incTutorial(communication.source.name, communication.target.name, true, false)
//						Window::alert(
//							"Clicked communication between " + communication.source.name + " and " + communication.target.name +
//								" with requests per second: " + communication.requestsPerSecond)
		]
	}
	
}
