package explorviz.visualization.interaction

import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.safehtml.shared.SafeHtmlUtils
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.ui.RootPanel
import explorviz.shared.model.Application
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.System
import explorviz.shared.model.helper.CommunicationTileAccumulator
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.ClassnameSplitter
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseHoverHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.engine.popover.PopoverService
import explorviz.visualization.export.RunnableLandscapeExporter
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.visualization.main.JSHelpers
import java.util.HashMap

class ModelingInteraction {
	static val MouseHoverHandler systemMouseHover = createSystemMouseHoverHandler()
	static val MouseDoubleClickHandler systemMouseDblClick = createSystemMouseDoubleClickHandler()
	static val MouseRightClickHandler systemRightMouseClick = createSystemMouseRightClickHandler()

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
	static val MouseRightClickHandler communicationMouseRightClick = createCommunicationMouseRightClickHandler()
	static val MouseHoverHandler communicationMouseHoverHandler = createCommunicationMouseHoverHandler()

	static HandlerRegistration addSystemHandler
	static HandlerRegistration exportAsRunnableHandler

	static val addSystemButtonId = "addSystemBtn"
	static val exportAsRunnableButtonId = "exportAsRunnableModellingBtn"

	def static void clearInteraction(Landscape landscape) {
		ObjectPicker::clear()

		for (system : landscape.systems) {
			system.clearAllHandlers()
			for (nodeGroup : system.nodeGroups) {
				nodeGroup.clearAllHandlers()
				for (node : nodeGroup.nodes) {
					node.clearAllHandlers()
					for (application : node.applications)
						application.clearAllHandlers()
				}
			}
		}
		for (commu : landscape.communicationsAccumulated) {
			for (tile : commu.tiles)
				tile.clearAllHandlers()
		}
	}

	def static void createInteraction(Landscape landscape) {
		for (system : landscape.systems)
			createSystemInteraction(system)

		for (commu : landscape.communicationsAccumulated) {
			for (tile : commu.tiles)
				createCommunicationInteraction(tile)
		}

		showAndPrepareAddSystemButton(landscape)
		showAndPrepareExportAsRunnableButton(landscape)
	}

	def static void showAndPrepareAddSystemButton(Landscape landscape) {
		if (addSystemHandler != null) {
			addSystemHandler.removeHandler
		}

		JSHelpers::showElementById(addSystemButtonId)

		val button = RootPanel::get(addSystemButtonId)

		button.sinkEvents(Event::ONCLICK)
		addSystemHandler = button.addHandler(
			[
				val system = new System()
				system.name = "<NEW-SYSTEM>"
				landscape.systems.add(system)
				system.parent = landscape
				LandscapeExchangeManager.saveTargetModelIfInModelingMode(landscape)
				SceneDrawer::createObjectsFromLandscape(landscape, (landscape.systems.size != 1))
			], ClickEvent::getType())
	}

	def static void showAndPrepareExportAsRunnableButton(Landscape landscape) {
		if (exportAsRunnableHandler != null) {
			exportAsRunnableHandler.removeHandler
		}

		JSHelpers::showElementById(exportAsRunnableButtonId)

		val button = RootPanel::get(exportAsRunnableButtonId)

		button.sinkEvents(Event::ONCLICK)
		exportAsRunnableHandler = button.addHandler(
			[
				JSHelpers::downloadAsFile("myLandscape.rb",
					RunnableLandscapeExporter::exportAsRunnableLandscapeRubyExport(landscape))
			], ClickEvent::getType())
	}

	def static private createSystemInteraction(System system) {
		system.setMouseHoverHandler(systemMouseHover)
		system.setMouseDoubleClickHandler(systemMouseDblClick)
		system.setMouseRightClickHandler(systemRightMouseClick)

		for (nodeGroup : system.nodeGroups)
			createNodeGroupInteraction(nodeGroup)
	}

	def static private MouseHoverHandler createSystemMouseHoverHandler() {
		[
			val system = (it.object as System)
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
			system.opened = !system.opened
			SceneDrawer::createObjectsFromLandscape(system.parent, true)
		]
	}

	def static private MouseRightClickHandler createSystemMouseRightClickHandler() {
		[
			val system = it.object as System
			PopupService::showModelingSystemPopupMenu(it.originalClickX, it.originalClickY, system)
		]
	}

	def static private createNodeGroupInteraction(NodeGroup nodeGroup) {
		nodeGroup.setMouseHoverHandler(nodeGroupMouseHover)
		nodeGroup.setMouseDoubleClickHandler(nodeGroupMouseDblClick)

		for (node : nodeGroup.nodes)
			createNodeInteraction(node)
	}

	def static private MouseHoverHandler createNodeGroupMouseHoverHandler() {
		[
			val nodeGroup = (it.object as NodeGroup)
			val name = nodeGroup.name
			var applicationCount = 0
			for (node : nodeGroup.nodes) {
				applicationCount = applicationCount + node.applications.size()
			}
			PopoverService::showPopover("[" + SafeHtmlUtils::htmlEscape(name) + "]", it.originalClickX,
				it.originalClickY,
				'<table style="width:100%"><tr><td>Nodes:</td><td style="text-align:right;padding-left:10px;">' +
					nodeGroup.nodes.size() +
					'</td></tr><tr><td>Applications:</td><td style="text-align:right;padding-left:10px;">' +
					applicationCount + '</td></tr></table>')
		]
	}

	def static private MouseDoubleClickHandler createNodeGroupMouseDoubleClickHandler() {
		[
			val nodeGroup = (it.object as NodeGroup)
			nodeGroup.opened = !nodeGroup.opened
			SceneDrawer::createObjectsFromLandscape(nodeGroup.parent.parent, true)
		]
	}

	def static private createNodeInteraction(Node node) {
		if (node.parent.opened || node.parent.nodes.size() == 1) {
			node.setMouseHoverHandler(nodeMouseHoverClick)
		}

		node.setMouseClickHandler(nodeMouseClick)
		node.setMouseRightClickHandler(nodeRightMouseClick)
		node.setMouseDoubleClickHandler(nodeMouseDblClick)
		for (application : node.applications)
			createApplicationInteraction(application)
	}

	def static private MouseClickHandler createNodeMouseClickHandler() {
		[]
	}

	def static private MouseDoubleClickHandler createNodeMouseDoubleClickHandler() {
		[]
	}

	def static private MouseRightClickHandler createNodeMouseRightClickHandler() {
		[
			val node = it.object as Node
			PopupService::showModelingNodePopupMenu(it.originalClickX, it.originalClickY, node)
		]
	}

	def static private MouseHoverHandler createNodeMouseHoverHandler() {
		[
			val node = it.object as Node
			val name = node.displayName
			val otherId = if (node.displayName == node.name && node.ipAddress != null)
					'<tr><td>IP Address:</td><td style="text-align:right;padding-left:10px;">' +
						SafeHtmlUtils::htmlEscape(node.ipAddress) + '</td></tr>'
				else if (node.name != null)
					'<tr><td>Hostname:</td><td style="text-align:right;padding-left:10px;">' +
						SafeHtmlUtils::htmlEscape(node.name) + '</td></tr>'
				else
					''
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(name), it.originalClickX, it.originalClickY,
				'<table style="width:100%">' + otherId + '<tr></table>')
		]
	}

	def static private createApplicationInteraction(Application application) {
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
			val app = it.object as Application
			PopupService::showModelingApplicationPopupMenu(it.originalClickX, it.originalClickY, app)
		]
	}

	def static MouseDoubleClickHandler createApplicationMouseDoubleClickHandler() {
		[]
	}

	def static private MouseHoverHandler createApplicationMouseHoverHandler() {
		[
			val application = it.object as Application
			val name = application.name
			val language = application.programmingLanguage.toString().toLowerCase.toFirstUpper
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(name), it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Language:</td><td style="text-align:right;padding-left:10px;">' +
					language + '</td></tr></table>')
		]
	}

	def static private createCommunicationInteraction(CommunicationTileAccumulator communication) {

		communication.setMouseClickHandler(communicationMouseClickHandler)
		communication.setMouseRightClickHandler(communicationMouseRightClick)
		communication.setMouseHoverHandler(communicationMouseHoverHandler)
	}

	def static private MouseClickHandler createCommunicationMouseClickHandler() {
		[]
	}

	def static private MouseRightClickHandler createCommunicationMouseRightClickHandler() {
		[
			val accum = (it.object as CommunicationTileAccumulator)
			PopupService::showModelingCommunicationPopupMenu(it.originalClickX, it.originalClickY, accum)
		]
	}

	def static private MouseHoverHandler createCommunicationMouseHoverHandler() {
		[
			val accum = (it.object as CommunicationTileAccumulator)
			if (accum.communications.empty) return;
			var sourceNameTheSame = true
			var targetNameTheSame = true
			var previousSourceName = accum.communications.get(0).source.name
			var previousTargetName = accum.communications.get(0).target.name
			val technology = accum.communications.get(0).technology
			for (commu : accum.communications) {
				if (previousSourceName != commu.source.name) {
					sourceNameTheSame = false
				}

				if (previousTargetName != commu.target.name) {
					targetNameTheSame = false
				}

				previousSourceName = commu.source.name
				previousTargetName = commu.target.name
			}
			var title = "Accumulated Communication"
			val arrow = "&nbsp;<span class='glyphicon glyphicon-transfer'></span>&nbsp;"
			var body = ""
			if (sourceNameTheSame && !targetNameTheSame) {
				title = splitName(previousSourceName) + arrow + "..."

				var alreadyOutputedCommu = new HashMap<String, Boolean>

				for (commu : accum.communications) {
					if (alreadyOutputedCommu.get(commu.target.name) == null) {
						var requests = 0
						for (reqCommu : accum.communications) {
							if (reqCommu.target.name == commu.target.name) {
								requests = requests + reqCommu.requests
							}
						}

						body = body + '<tr><td>...</td><td>' + arrow + '</td><td>' + commu.target.name +
							':</td><td style="text-align:right;padding-left:10px;">' + requests + '</td></tr>'
						alreadyOutputedCommu.put(commu.target.name, true)
					}
				}
				body = body +
					'<tr><td>Technology:</td><td></td><td></td><td style="text-align:right;padding-left:10px;">' +
					technology + '</td></tr>'
			} else if (!sourceNameTheSame && targetNameTheSame) {
				title = "..." + arrow + splitName(previousTargetName)

				var alreadyOutputedCommu = new HashMap<String, Boolean>

				for (commu : accum.communications) {
					if (alreadyOutputedCommu.get(commu.source.name) == null) {
						var requests = 0
						for (reqCommu : accum.communications) {
							if (reqCommu.source.name == commu.source.name) {
								requests = requests + reqCommu.requests
							}
						}

						body = body + '<tr><td>' + commu.source.name + '</td><td>' + arrow + '</td><td>' +
							'...:</td><td style="text-align:right;padding-left:10px;">' + requests + '</td></tr>'
						alreadyOutputedCommu.put(commu.source.name, true)
					}
				}
				body = body +
					'<tr><td>Technology:</td><td></td><td></td><td style="text-align:right;padding-left:10px;">' +
					technology + '</td></tr>'
			} else if (sourceNameTheSame && targetNameTheSame) {
				title = splitName(previousSourceName) + "<br>" + arrow + "<br>" + splitName(previousTargetName)
				var requests = 0
				for (commu : accum.communications) {
					requests = requests + commu.requests
				}
				body = '<tr><td>Requests: </td><td style="text-align:right;padding-left:10px;">' + requests +
					'</td></tr><tr><td>Technology: </td><td style="text-align:right;padding-left:10px;">' + technology +
					'</td></tr>'
			}
			PopoverService::showPopover(title, it.originalClickX, it.originalClickY,
				'<table style="width:100%">' + body + '</table>')
		]
	}

	def static private String splitName(String name) {
		val nameSplit = ClassnameSplitter.splitClassname(name, 14, 2)
		if (nameSplit.size == 2) {
			SafeHtmlUtils::htmlEscape(nameSplit.get(0)) + "<br>" + SafeHtmlUtils::htmlEscape(nameSplit.get(1))
		} else {
			SafeHtmlUtils::htmlEscape(name)
		}
	}

}
