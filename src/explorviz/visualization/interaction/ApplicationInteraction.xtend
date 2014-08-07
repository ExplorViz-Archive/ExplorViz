package explorviz.visualization.interaction

import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.safehtml.shared.SafeHtmlUtils
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.ui.RootPanel
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.main.ClassnameSplitter
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseHoverHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.engine.popover.PopoverService
import explorviz.visualization.engine.primitives.FreeFieldQuad
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.export.OpenSCADApplicationExporter
import explorviz.visualization.highlighting.NodeHighlighter
import explorviz.visualization.highlighting.TraceHighlighter
import explorviz.visualization.main.ClientConfiguration
import explorviz.visualization.main.JSHelpers
import java.util.HashSet
import explorviz.visualization.clustering.Clustering

class ApplicationInteraction {
	static val MouseClickHandler freeFieldMouseClickHandler = createFreeFieldMouseClickHandler()

	static val MouseClickHandler componentMouseClickHandler = createComponentMouseClickHandler()
	static val MouseRightClickHandler componentMouseRightClickHandler = createComponentMouseRightClickHandler()
	static val MouseDoubleClickHandler componentMouseDoubleClickHandler = createComponentMouseDoubleClickHandler()
	static val MouseHoverHandler componentMouseHoverHandler = createComponentMouseHoverHandler()

	static val MouseClickHandler clazzMouseClickHandler = createClazzMouseClickHandler()
	static val MouseRightClickHandler clazzMouseRightClickHandler = createClazzMouseRightClickHandler()
	static val MouseDoubleClickHandler clazzMouseDoubleClickHandler = createClazzMouseDoubleClickHandler()
	static val MouseHoverHandler clazzMouseHoverHandler = createClazzMouseHoverHandler()

	static val MouseClickHandler communicationMouseClickHandler = createCommunicationMouseClickHandler()
	static val MouseHoverHandler communicationMouseHoverHandler = createCommunicationMouseHoverHandler()

	static HandlerRegistration backToLandscapeHandler
	static HandlerRegistration export3DModelHandler
	static HandlerRegistration openAllComponentsHandler
	static HandlerRegistration openClusteringHandler

	static val backToLandscapeButtonId = "backToLandscapeBtn"
	static val export3DModelButtonId = "export3DModelBtn"
	static val openAllComponentsButtonId = "openAllComponentsBtn"
	static val openClusteringDialogButtonId = "openClusteringDialogBtn"

	public static Component freeFieldQuad
	

	def static void clearInteraction(Application application) {
		if (freeFieldQuad != null) {
			freeFieldQuad.clearAllHandlers
			freeFieldQuad.clearAllPrimitiveObjects
		}
		ObjectPicker::clear()

		application.components.forEach [
			clearComponentInteraction(it)
		]

		application.communicationsAccumulated.forEach [
			it.clearAllHandlers()
		]
	}

	def static private void clearComponentInteraction(Component component) {
		component.clearAllHandlers()

		component.clazzes.forEach [
			it.clearAllHandlers()
		]

		component.children.forEach [
			clearComponentInteraction(it)
		]
	}

	def static void createInteraction(Application application) {
		freeFieldQuad = new Component()
		freeFieldQuad.setMouseClickHandler(freeFieldMouseClickHandler)
		val freeFieldQuadPrimitive = new FreeFieldQuad(new Vector3f(-10000f, 0f, 10000f),
			new Vector3f(10000f, 0f, 10000f), new Vector3f(10000f, 0f, -10000f), new Vector3f(-10000f, 0f, -10000f))
		freeFieldQuad.primitiveObjects.add(freeFieldQuadPrimitive)

		application.components.get(0).setMouseClickHandler(freeFieldMouseClickHandler)

		application.components.get(0).children.forEach [
			createComponentInteraction(it)
		]

		application.communicationsAccumulated.forEach [
			createCommunicationInteraction(it)
		]
		if (!Experiment::tutorial || Experiment::getStep.backToLandscape) {
			showAndPrepareBackToLandscapeButton(application)
		}
		if (!Experiment::tutorial) {
			showAndPrepareOpenAllComponentsButton(application)
//			showAndPrepareOpenClusteringButton(application)
		}
		if (ClientConfiguration::show3DExportButton && !Experiment::experiment) {
			showAndPrepareExport3DModelButton(application)
		} else {
			if (export3DModelHandler != null) {
				export3DModelHandler.removeHandler
			}

			JSHelpers::hideElementById(export3DModelButtonId)
		}
	}

	def static showAndPrepareBackToLandscapeButton(Application application) {
		if (backToLandscapeHandler != null) {
			backToLandscapeHandler.removeHandler
		}

		JSHelpers::showElementById(backToLandscapeButtonId)

		val landscapeBack = RootPanel::get(backToLandscapeButtonId)

		landscapeBack.sinkEvents(Event::ONCLICK)
		backToLandscapeHandler = landscapeBack.addHandler(
			[
				JSHelpers::hideElementById(backToLandscapeButtonId)
				JSHelpers::hideElementById(export3DModelButtonId)
				if (Experiment::tutorial && Experiment::getStep().backToLandscape) {
					Experiment::incStep()
				}
				Usertracking::trackBackToLandscape()
				TraceHighlighter::reset(false)
				NodeHighlighter::reset()
				SceneDrawer::createObjectsFromLandscape(application.parent.parent.parent.parent, false)
			], ClickEvent::getType())
	}

	def static showAndPrepareOpenAllComponentsButton(Application application) {
		if (openAllComponentsHandler != null) {
			openAllComponentsHandler.removeHandler
		}

		JSHelpers::showElementById(openAllComponentsButtonId)

		val openAllComponents = RootPanel::get(openAllComponentsButtonId)

		openAllComponents.sinkEvents(Event::ONCLICK)
		openAllComponentsHandler = openAllComponents.addHandler(
			[
				Usertracking::trackComponentOpenAll()
				TraceHighlighter::reset(false)
				NodeHighlighter::reset()
				application.openAllComponents
				SceneDrawer::createObjectsFromApplication(application, true)
			], ClickEvent::getType())
	}
	
	def static showAndPrepareOpenClusteringButton(Application application) {
		if (openClusteringHandler != null) {
			openClusteringHandler.removeHandler
		}

		JSHelpers::showElementById(openClusteringDialogButtonId)

		val openClustering = RootPanel::get(openClusteringDialogButtonId)

		openClustering.sinkEvents(Event::ONCLICK)
		openClusteringHandler = openClustering.addHandler(
			[
//				Usertracking::trackComponentOpenAll()
//				TraceHighlighter::reset(false)
//				NodeHighlighter::reset()
//				application.openAllComponents
				Clustering::openClusteringDialog
				SceneDrawer::createObjectsFromApplication(application, true)
			], ClickEvent::getType())
	}

	def static showAndPrepareExport3DModelButton(Application application) {
		if (export3DModelHandler != null) {
			export3DModelHandler.removeHandler
		}

		JSHelpers::showElementById(export3DModelButtonId)

		val export3DModel = RootPanel::get(export3DModelButtonId)

		export3DModel.sinkEvents(Event::ONCLICK)
		export3DModelHandler = export3DModel.addHandler(
			[
				Usertracking::trackExport3DModel(application)
				JSHelpers::downloadAsFile(application.name + ".scad",
					OpenSCADApplicationExporter::exportApplicationAsOpenSCAD(application))
			], ClickEvent::getType())
	}

	def static private void createComponentInteraction(Component component) {
		if (!Experiment::tutorial) {
			component.setMouseClickHandler(componentMouseClickHandler)
			component.setMouseRightClickHandler(componentMouseRightClickHandler)
			component.setMouseDoubleClickHandler(componentMouseDoubleClickHandler)
			

			component.clazzes.forEach [
				createClazzInteraction(it)
			]

			component.children.forEach [
				createComponentInteraction(it)
			]
		} else { //Tutorialmodus active, only set correct handler or go further into the component
			val step = Experiment::getStep()
			val safeStep = Experiment::getSafeStep()
			if (!step.connection && component.name.equals(step.source) ||
				!safeStep.connection && component.name.equals(safeStep.source)) {
				if (step.rightClick || step.codeview) {
					component.setMouseRightClickHandler(componentMouseRightClickHandler)
				} else if (step.doubleClick) {
					component.setMouseDoubleClickHandler(componentMouseDoubleClickHandler)
				} else if (step.leftClick) {
					component.setMouseClickHandler(componentMouseClickHandler)
				}
			} else {
				component.clazzes.forEach [
					createClazzInteraction(it)
				]

				component.children.forEach [
					createComponentInteraction(it)
				]
			}
		}
		component.setMouseHoverHandler(componentMouseHoverHandler) //hovering works always
	}

	def static private MouseClickHandler createFreeFieldMouseClickHandler() {
		[
			if (!Experiment::tutorial || Experiment.getStep.leaveanalysis) {
				if (Experiment::tutorial && Experiment.getStep.leaveanalysis) {
					Experiment.incStep()
				}
				TraceHighlighter::reset(true)
				Usertracking::trackDraw3DNodeUnhighlightAll
				NodeHighlighter::unhighlight3DNodes()
			}
		]
	}

	def static private MouseClickHandler createComponentMouseClickHandler() {
		[
			val compo = it.object as Component
			Experiment::incTutorial(compo.name, true, false, false, false)
			if (!compo.opened) {
				NodeHighlighter::highlight3DNode(compo)
			} else {
				Usertracking::trackDraw3DNodeUnhighlightAll
				NodeHighlighter::unhighlight3DNodes()
			}
			Usertracking::trackComponentClick(compo)
		]
	}

	def static private MouseRightClickHandler createComponentMouseRightClickHandler() {
		[
			val compo = it.object as Component
			Usertracking::trackComponentRightClick(compo)
			Experiment::incTutorial(compo.name, false, true, false, false)
		//			PopupService::showComponentPopupMenu(it.originalClickX, it.originalClickY, compo)
		// TODO for Experiment commented out
		]
	}

	def static private MouseDoubleClickHandler createComponentMouseDoubleClickHandler() {
		[
			val component = it.object as Component
			Usertracking::trackComponentDoubleClick(component)
			if (component.highlighted || isChildHighlighted(component)) {
				Usertracking::trackDraw3DNodeUnhighlightAll
				NodeHighlighter::unhighlight3DNodes()
			}
			component.opened = !component.opened
			
			if (TraceHighlighter::isCurrentlyHighlighting) {
				TraceHighlighter::reset(false)
			}
			
			Experiment::incTutorial(component.name, false, false, true, false)
			SceneDrawer::createObjectsFromApplication(component.belongingApplication, true)
		]
	}

	def static private boolean isChildHighlighted(Component compo) {
		for (clazz : compo.clazzes) {
			if (clazz.highlighted) {
				return true
			}
		}

		for (child : compo.children) {
			if (child.highlighted) {
				return true
			} else {
				if (isChildHighlighted(child)) {
					return true
				}
			}
		}

		false
	}

	def static private MouseHoverHandler createComponentMouseHoverHandler() {
		[
			val component = it.object as Component
			Experiment::incTutorial(component.name, false, false, false, true)
			Usertracking::trackComponentMouseHover(component)
			var name = component.name
			val nameSplit = ClassnameSplitter.splitClassname(component.name, 14, 2)
			if (nameSplit.size == 2) {
				name = SafeHtmlUtils::htmlEscape(nameSplit.get(0)) + "<br>" +
					SafeHtmlUtils::htmlEscape(nameSplit.get(1))
			} else {
				name = SafeHtmlUtils::htmlEscape(component.name)
			}
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(name), it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Contained Classes: </td><td style="text-align:right;padding-left:10px;">' +
					getClazzesCount(component) +
					'</td></tr><tr><td>Contained Packages: </td><td style="text-align:right;padding-left:10px;">' +
					getPackagesCount(component) + '</td></tr></table>')
		]
	}

	def static private int getClazzesCount(Component component) {
		var result = component.clazzes.size
		for (child : component.children) {
			result = result + getClazzesCount(child)
		}
		result
	}

	def static private int getPackagesCount(Component component) {
		var result = component.children.size
		for (child : component.children) {
			result = result + getPackagesCount(child)
		}
		result
	}

	def static private void createClazzInteraction(Clazz clazz) {
		if (!Experiment::tutorial) {
			clazz.setMouseClickHandler(clazzMouseClickHandler)
			clazz.setMouseRightClickHandler(clazzMouseRightClickHandler)
			clazz.setMouseDoubleClickHandler(clazzMouseDoubleClickHandler)
		} else if (!Experiment::getStep().connection && clazz.name.equals(Experiment::getStep().source) ||
			!Experiment::getSafeStep().connection && clazz.name.equals(Experiment::getSafeStep().source)) {
			val step = Experiment::getStep()
			if (step.rightClick || step.codeview) {
				clazz.setMouseRightClickHandler(clazzMouseRightClickHandler)
			} else if (step.doubleClick) {
				clazz.setMouseDoubleClickHandler(clazzMouseDoubleClickHandler)
			} else if (step.leftClick) {
				clazz.setMouseClickHandler(clazzMouseClickHandler)
			}
		}
		clazz.setMouseHoverHandler(clazzMouseHoverHandler) //hovering always works
	}

	def static private MouseClickHandler createClazzMouseClickHandler() {
		[
			val clazz = it.object as Clazz
			Experiment::incTutorial(clazz.name, true, false, false, false)
			NodeHighlighter::highlight3DNode(clazz)
			Usertracking::trackClazzClick(clazz)
		]
	}

	def static private MouseRightClickHandler createClazzMouseRightClickHandler() {
		[
			val clazz = it.object as Clazz
			Usertracking::trackClazzRightClick(clazz)
			Experiment::incTutorial(clazz.name, false, true, false, false)
		//			PopupService::showClazzPopupMenu(it.originalClickX, it.originalClickY, clazz)
		// TODO for Experiment commented out
		]
	}

	def static private MouseDoubleClickHandler createClazzMouseDoubleClickHandler() {
		[
			val clazz = it.object as Clazz
			Experiment::incTutorial(clazz.name, false, false, true, false)
			Usertracking::trackClazzDoubleClick(clazz)
		// empty
		]
	}

	def static private MouseHoverHandler createClazzMouseHoverHandler() {
		[
			val clazz = it.object as Clazz
			Experiment::incTutorial(clazz.name, false, false, false, true)
			Usertracking::trackClazzMouseHover(clazz)
			var name = clazz.name
			val nameSplit = ClassnameSplitter.splitClassname(clazz.name, 14, 2)
			if (nameSplit.size == 2) {
				name = SafeHtmlUtils::htmlEscape(nameSplit.get(0)) + "<br>" +
					SafeHtmlUtils::htmlEscape(nameSplit.get(1))
			} else {
				name = SafeHtmlUtils::htmlEscape(clazz.name)
			}
			PopoverService::showPopover(name, it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Active Instances: </td><td style="text-align:right;padding-left:10px;">' +
					clazz.instanceCount +
					'</td></tr><tr><td>Called Methods: </td><td style="text-align:right;padding-left:10px;">' +
					getCalledMethods(clazz) + '</td></tr></table>')
		]
	}

	def static private int getCalledMethods(Clazz clazz) {
		var methods = new HashSet<String>
		for (commu : clazz.parent.belongingApplication.communications) {
			if (commu.target == clazz && commu.target != commu.source) {
				methods.add(commu.methodName)
			}
		}
		methods.size()
	}

	def static private createCommunicationInteraction(CommunicationAppAccumulator communication) {
		if (!Experiment::tutorial) {
			communication.setMouseClickHandler(communicationMouseClickHandler)
		} else if (Experiment::getStep().connection && Experiment::getStep().source.equals(communication.source.name) &&
			Experiment::getStep().dest.equals(communication.target.name) || Experiment::getSafeStep().connection &&
			Experiment::getSafeStep().source.equals(communication.source.name) &&
			Experiment::getSafeStep().dest.equals(communication.target.name)) {
			val step = Experiment::getStep()
			if (step.leftClick || step.choosetrace || step.leaveanalysis || step.pauseanalysis || step.startanalysis ||
				step.nextanalysis) {
				communication.setMouseClickHandler(communicationMouseClickHandler)
			}
		}
		communication.setMouseHoverHandler(communicationMouseHoverHandler) //hovering always works
	}

	def static private MouseClickHandler createCommunicationMouseClickHandler() {
		[
			val communication = (it.object as CommunicationAppAccumulator)
			Usertracking::trackCommunicationClick(communication)
			Experiment::incTutorial(communication.source.name, communication.target.name, true, false, false)
			TraceHighlighter::openTraceChooser(communication)
		]
	}

	def static private MouseHoverHandler createCommunicationMouseHoverHandler() {
		[
			val communicationParam = (it.object as CommunicationAppAccumulator)
			Experiment::incTutorial(communicationParam.source.name, communicationParam.target.name, false, false, true)
			Usertracking::trackCommunicationMouseHover(communicationParam)
			
			val communication = if (NodeHighlighter::isCurrentlyHighlighting) {
				if (communicationParam.source.fullQualifiedName == NodeHighlighter::highlightedNode.fullQualifiedName) {
					communicationParam
				} else if (communicationParam.target.fullQualifiedName == NodeHighlighter::highlightedNode.fullQualifiedName) {
				   val commu = new CommunicationAppAccumulator()
				   commu.requests = communicationParam.requests
				   commu.source = communicationParam.target
				   commu.target = communicationParam.source
				   commu.aggregatedCommunications.addAll(communicationParam.aggregatedCommunications)
				   commu
				} else {
					communicationParam
				}
			} else {
				communicationParam
			}
			
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
			var methods = '<table style="width:100%">'
			methods += getMethodList(communication)
			methods += "</table>"
			var requests = communication.requests
			PopoverService::showPopover(
				sourceName + "<br><span class='glyphicon glyphicon-transfer'></span><br>" + targetName,
				it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Requests: </td><td style="text-align:right;padding-left:10px;">' +
					requests + '</td></tr></table><br>' + methods)
		]
	}

	private def static String getMethodList(CommunicationAppAccumulator communication) {
		var methods = ''

		val commuSorted = communication.aggregatedCommunications.sort(
			[ c1, c2 |
				var c1DirectionArrow = if (isClazzChildOf(c1.target, communication.target)) {
						"right"
					} else {
						"left"
					}
				var c2DirectionArrow = if (isClazzChildOf(c1.target, communication.target)) {
						"right"
					} else {
						"left"
					}
				if (c1DirectionArrow <=> c2DirectionArrow == 0) {
					val c1MethodName = if (!c1.methodName.startsWith("new "))
							c1.target.name + "." + c1.methodName
						else
							c1.methodName
					val c2MethodName = if (!c2.methodName.startsWith("new "))
							c2.target.name + "." + c2.methodName
						else
							c2.methodName
					if ((!c1.methodName.startsWith("new ")) && (c2.methodName.startsWith("new "))) {
						return 1
					}
					if ((c1.methodName.startsWith("new ")) && (!c2.methodName.startsWith("new "))) {
						return -1
					}

					c1MethodName <=> c2MethodName
				} else {
					(c1DirectionArrow <=> c2DirectionArrow) * -1
				}
			])
		val alreadyAddedMethods = new HashSet<String>()
		for (aggCommu : commuSorted) {
			var directionArrow = if (isClazzChildOf(aggCommu.target, communication.target)) {
					"right"
				} else {
					"left"
				}

			var oneLiner = true
			var alreadyAdded = false
			var method = aggCommu.methodName

			if (!aggCommu.methodName.startsWith("new ")) {
				val fullMethod = aggCommu.target.name + "." + method
				alreadyAdded = alreadyAddedMethods.contains(fullMethod)

				if (!alreadyAdded) {
					alreadyAddedMethods.add(fullMethod)
					if (fullMethod.length >= 38) {
						oneLiner = false
					} else {
						method = fullMethod
					}
				}
			} else {
				alreadyAdded = alreadyAddedMethods.contains(method)
				if (!alreadyAdded)
					alreadyAddedMethods.add(method)
			}

			if (!alreadyAdded) {
				if (oneLiner) {
					methods += generateMethodRows(directionArrow, method + "(..)")
				} else {
					methods += generateMethodRows(directionArrow, aggCommu.target.name)
					methods += "<tr><td></td><td style='padding-left:25px;'>" + "." + method + "(..)" + "</td></tr>"
				}
			}
		}

		methods
	}

	def static generateMethodRows(String directionArrow, String content) {
		"<tr><td><span class='glyphicon glyphicon-arrow-" + directionArrow +
			"'></span></td><td style='padding-left:10px;'>" + content + "</td></tr>"
	}

	def static isClazzChildOf(Clazz clazz, Draw3DNodeEntity entity) {
		if (entity instanceof Clazz) {
			return clazz == entity
		}

		isClazzChildOfHelper(clazz.parent, entity)
	}

	def static boolean isClazzChildOfHelper(Component component, Draw3DNodeEntity entity) {
		if (component == null) {
			return false
		}

		if (component == entity) {
			return true
		}

		isClazzChildOfHelper(component.parentComponent, entity)
	}

}
