package explorviz.visualization.interaction

import com.google.gwt.i18n.client.DateTimeFormat
import com.google.gwt.i18n.client.DefaultDateTimeFormatInfo
import com.google.gwt.safehtml.shared.SafeHtmlUtils
import explorviz.shared.model.Application
import explorviz.shared.model.Communication
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.System
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.ClassnameSplitter
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseHoverHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.engine.popover.PopoverService
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.main.AlertDialogJS
import java.util.Date

class LandscapeInteraction {
	static val MouseHoverHandler systemMouseHover = createSystemMouseHoverHandler()
	static val MouseDoubleClickHandler systemMouseDblClick = createSystemMouseDoubleClickHandler()

	static val MouseHoverHandler nodeGroupMouseHover = createNodeGroupMouseHoverHandler()
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
	static val MouseHoverHandler communicationMouseHoverHandler = createCommunicationMouseHoverHandler()

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
		system.setMouseHoverHandler(systemMouseHover)
		if (!Experiment::tutorial) {
			system.setMouseDoubleClickHandler(systemMouseDblClick)

			system.nodeGroups.forEach [
				createNodeGroupInteraction(it)
			]
		} else { //Tutorialmodus active, only set the correct handler, otherwise go further into the system
			val step = Experiment::getStep()
			if (!step.isConnection && step.source.equals(system.name) && step.doubleClick) {
				system.setMouseDoubleClickHandler(systemMouseDblClick)
			} else {
				system.nodeGroups.forEach [
					createNodeGroupInteraction(it)
				]
			}
		}

	}

	def static private MouseHoverHandler createSystemMouseHoverHandler() {
		[
			val system = (it.object as System)
			// TODO
			//			Usertracking::trackNodeRightClick(node);
			val name = system.name
			var nodesCount = 0
			var applicationCount = 0
			for (nodeGroup : system.nodeGroups) {
				nodesCount = nodesCount + nodeGroup.nodes.size()
				for (node : nodeGroup.nodes) {
					applicationCount = applicationCount + node.applications.size()
				}
			}
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(name), it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Nodes:</td><td style="text-align:right;padding-left:10px;">' +
					nodesCount + '</td></tr><tr><td>Applications:</td><td style="text-align:right;padding-left:10px;">' +
					applicationCount + '</td></tr></table>')
		]
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

	def static private createNodeGroupInteraction(NodeGroup nodeGroup) {
		nodeGroup.setMouseHoverHandler(nodeGroupMouseHover)
		if (!Experiment::tutorial) {
			nodeGroup.setMouseDoubleClickHandler(nodeGroupMouseDblClick)

			nodeGroup.nodes.forEach [
				createNodeInteraction(it)
			]
		} else { //Tutorialmodus active, only set correct handler, otherwise go further into the nodegroup
			val step = Experiment::getStep()
			if (!step.isConnection && step.source.equals(nodeGroup.name) && step.doubleClick) {
				nodeGroup.setMouseDoubleClickHandler(nodeGroupMouseDblClick)
			} else {
				nodeGroup.nodes.forEach [
					createNodeInteraction(it)
				]
			}
		}
	}

	def static private MouseHoverHandler createNodeGroupMouseHoverHandler() {
		[
			val nodeGroup = (it.object as NodeGroup)
			Experiment::incTutorial(nodeGroup.name, false, false, true, false)
			// TODO
			//			Usertracking::trackNodeRightClick(node);
			val name = nodeGroup.name
			Experiment::incTutorial(name, false, false, false, true)
			var avgNodeCPUUtil = 0d
			var applicationCount = 0
			for (node : nodeGroup.nodes) {
				avgNodeCPUUtil = avgNodeCPUUtil + node.cpuUtilization
				applicationCount = applicationCount + node.applications.size()
			}
			PopoverService::showPopover("[" + SafeHtmlUtils::htmlEscape(name) + "]", it.originalClickX,
				it.originalClickY,
				'<table style="width:100%"><tr><td>Nodes:</td><td style="text-align:right;padding-left:10px;">' +
					nodeGroup.nodes.size() +
					'</td></tr><tr><td>Applications:</td><td style="text-align:right;padding-left:10px;">' +
					applicationCount +
					'</td></tr><tr><td>Avg. CPU Utilization:</td><td style="text-align:right;padding-left:10px;">' +
					Math.round(avgNodeCPUUtil * 100f) / nodeGroup.nodes.size() + '%</td></tr></table>')
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
		if (node.parent.opened || node.parent.nodes.size() == 1) {
			node.setMouseHoverHandler(nodeMouseHoverClick)
		}

		if (!Experiment::tutorial) {
			node.setMouseClickHandler(nodeMouseClick)
			node.setMouseRightClickHandler(nodeRightMouseClick)
			node.setMouseDoubleClickHandler(nodeMouseDblClick)
			node.applications.forEach [
				createApplicationInteraction(it)
			]
		} else { //Tutorialmodus active, only set correct handler, otherwise go further into the node
			val step = Experiment::getStep()
			if (!step.isConnection && step.source.equals(node.name)) {
				if (step.leftClick) {
					node.setMouseClickHandler(nodeMouseClick)
				} else if (step.rightClick) {
					node.setMouseRightClickHandler(nodeRightMouseClick)
				} else if (step.doubleClick) {
					node.setMouseDoubleClickHandler(nodeMouseDblClick)
				} else if (step.hover) {
					node.setMouseHoverHandler(nodeMouseHoverClick)
				}
			} else {
				node.applications.forEach [
					createApplicationInteraction(it)
				]
			}
		}
	}

	def static private MouseClickHandler createNodeMouseClickHandler() {
		[
			//			Usertracking::trackNodeClick(it.object as Node)
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
			val name = node.displayName
			Experiment::incTutorial(node.name, false, false, false, true)
			val otherId = if (node.displayName == node.name && node.ipAddress != null)
					'<tr><td>IP Address:</td><td style="text-align:right;padding-left:10px;">' +
						SafeHtmlUtils::htmlEscape(node.ipAddress) + '%</td></tr>'
				else if (node.name != null)
					'<tr><td>Hostname:</td><td style="text-align:right;padding-left:10px;">' +
						SafeHtmlUtils::htmlEscape(node.name) + '%</td></tr>'
				else
					''
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(name), it.originalClickX, it.originalClickY,
				'<table style="width:100%">' + otherId +
					'<tr><td>CPU Utilization:</td><td style="text-align:right;padding-left:10px;">' +
					Math.round(node.cpuUtilization * 100f) +
					'%</td></tr><tr><td>Total RAM:</td><td style="text-align:right;padding-left:10px;">' +
					getTotalRAMInGB(node) +
					' GB</td></tr><tr><td>Free RAM:</td><td style="text-align:right;padding-left:10px;">' +
					getFreeRAMInPercent(node) + '%</td></tr></table>')
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
		if (!Experiment::tutorial) {
			application.setMouseClickHandler(applicationMouseClick)
			application.setMouseRightClickHandler(applicationMouseRightClick)
			application.setMouseDoubleClickHandler(applicationMouseDblClick)
			application.setMouseHoverHandler(applicationMouseHoverClick)
		} else if (!Experiment::getStep().connection && Experiment::getStep().source.equals(application.name)) {
			val step = Experiment::getStep()
			if (step.leftClick) {
				application.setMouseClickHandler(applicationMouseClick)
			} else if (step.rightClick) {
				application.setMouseRightClickHandler(applicationMouseRightClick)
			} else if (step.doubleClick) {
				application.setMouseDoubleClickHandler(applicationMouseDblClick)
			} else if (step.hover) {
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
			if (!app.components.empty && !app.components.get(0).children.empty) {
				SceneDrawer::createObjectsFromApplication(app, false)
			} else {
				AlertDialogJS::showAlertDialog("No Details Available",
					"Sorry, no details for " + app.name + " are available.")
			}
		]
	}

	def static private MouseHoverHandler createApplicationMouseHoverHandler() {
		[
			val application = it.object as Application
			// TODO
			//			Usertracking::trackNodeRightClick(node);
			val name = application.name
			Experiment::incTutorial(name, false, false, false, true)
			val pattern = "yyyy-MM-dd HH:mm"
			val info = new DefaultDateTimeFormatInfo()
			val dtf = new DateTimeFormat(pattern, info) {
			};
			val lastUsageDate = dtf.format(new Date(application.lastUsage))
			val language = application.programmingLanguage.toString().toLowerCase.toFirstUpper
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(name), it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Last Usage:</td><td style="text-align:right;padding-left:10px;">' +
					lastUsageDate + '</td></tr><tr><td>Language:</td><td style="text-align:right;padding-left:10px;">' +
					language + '</td></tr></table>')
		]
	}

	def static private createCommunicationInteraction(Communication communication) {
		if (!Experiment::tutorial || (Experiment::getStep().connection &&
			communication.source.name.equals(Experiment::getStep().source) &&
			communication.target.name.equals(Experiment::getStep().dest) && Experiment::getStep().leftClick)) {
			communication.setMouseClickHandler(communicationMouseClickHandler)
		}
		communication.setMouseHoverHandler(communicationMouseHoverHandler)
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

	def static private MouseHoverHandler createCommunicationMouseHoverHandler() {
		[
			val communication = (it.object as Communication)
			var sourceName = communication.source.name
			val sourceNameSplit = ClassnameSplitter.splitClassname(sourceName, 14, 2)
			if (sourceNameSplit.size == 2) {
				sourceName = SafeHtmlUtils::htmlEscape(sourceNameSplit.get(0)) + "<br>" +
					SafeHtmlUtils::htmlEscape(sourceNameSplit.get(1))
			} else {
				sourceName = SafeHtmlUtils::htmlEscape(communication.source.name)
			}
			var targetName = communication.target.name
			val targetNameSplit = ClassnameSplitter.splitClassname(targetName, 14, 2)
			if (targetNameSplit.size == 2) {
				targetName = SafeHtmlUtils::htmlEscape(targetNameSplit.get(0)) + "<br>" +
					SafeHtmlUtils::htmlEscape(targetNameSplit.get(1))
			} else {
				targetName = SafeHtmlUtils::htmlEscape(communication.target.name)
			}
			var requests = communication.requests
			PopoverService::showPopover(
				sourceName + "<br><span class='glyphicon glyphicon-transfer'></span><br>" + targetName,
				it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Requests: </td><td style="text-align:right;padding-left:10px;">' +
					requests + '</td></tr></table>')
		]
	}

}
