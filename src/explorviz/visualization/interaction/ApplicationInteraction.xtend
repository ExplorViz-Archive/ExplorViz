package explorviz.visualization.interaction

import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.safehtml.shared.SafeHtmlUtils
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.ui.RootPanel
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.CommunicationClazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.contextmenu.PopupService
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
import explorviz.visualization.performanceanalysis.PerformanceAnalysis
import java.util.ArrayList
import java.util.Collections
import java.util.HashSet
import explorviz.visualization.engine.main.WebVRJS
import explorviz.visualization.databasequeries.DatabaseQueries

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
	static HandlerRegistration export3DModelAction1Handler
	static HandlerRegistration export3DModelAction2Handler
	static HandlerRegistration export3DModelAction3Handler
	static HandlerRegistration export3DModelAction4Handler
	static HandlerRegistration openAllComponentsHandler
	static HandlerRegistration performanceAnalysisHandler
	static HandlerRegistration virtualRealityModeHandler
	static HandlerRegistration databaseQueriesHandler

	static val backToLandscapeButtonId = "backToLandscapeBtn"
	static val export3DModelButtonId = "export3DModelBtn"
	static val export3DModelButtonInnerId = "export3DModelBtnInner"
	static val openAllComponentsButtonId = "openAllComponentsBtn"
	static val performanceAnalysisButtonId = "performanceAnalysisBtn"
	static val virtualRealityModeButtonId = "virtualRealityModeBtn"
	static val databaseQueriesButtonId = "databaseQueriesBtn"

	public static Component freeFieldQuad

	def static void clearInteraction(Application application) {
		if (freeFieldQuad != null) {
			freeFieldQuad.clearAllHandlers
			freeFieldQuad.clearAllPrimitiveObjects
		}
		ObjectPicker::clear()

		for (component : application.components)
			clearComponentInteraction(component)

		for (commu : application.communicationsAccumulated)
			commu.clearAllHandlers()
	}

	def static private void clearComponentInteraction(Component component) {
		component.clearAllHandlers()

		for (clazz : component.clazzes)
			clazz.clearAllHandlers()

		for (child : component.children)
			clearComponentInteraction(child)
	}

	def static void createInteraction(Application application) {
		freeFieldQuad = new Component()
		freeFieldQuad.setMouseClickHandler(freeFieldMouseClickHandler)
		val freeFieldQuadPrimitive = new FreeFieldQuad(new Vector3f(-10000f, 0f, 10000f),
			new Vector3f(10000f, 0f, 10000f), new Vector3f(10000f, 0f, -10000f), new Vector3f(-10000f, 0f, -10000f))
		freeFieldQuad.primitiveObjects.add(freeFieldQuadPrimitive)

		application.components.get(0).setMouseClickHandler(freeFieldMouseClickHandler)

		for (child : application.components.get(0).children)
			createComponentInteraction(child)

		for (commu : application.communicationsAccumulated)
			createCommunicationInteraction(commu)

		if (!Experiment::tutorial || Experiment::getStep.backToLandscape) {
			showAndPrepareBackToLandscapeButton(application)
		}
		if (!Experiment::tutorial) {
			showAndPrepareOpenAllComponentsButton(application)
			showAndPreparePerformanceAnalysisButton(application)
			showAndPrepareVirtualRealityModeButton()
			showAndPrepareDatabaseQueriesButton(application)
		}
		if (ClientConfiguration::show3DExportButton && !Experiment::experiment) {
			showAndPrepareExport3DModelButton(application)
		} else {
			if (export3DModelAction1Handler != null) {
				export3DModelAction1Handler.removeHandler
			}
			if (export3DModelAction2Handler != null) {
				export3DModelAction2Handler.removeHandler
			}
			if (export3DModelAction3Handler != null) {
				export3DModelAction3Handler.removeHandler
			}
			if (export3DModelAction4Handler != null) {
				export3DModelAction4Handler.removeHandler
			}

			JSHelpers::hideElementById(export3DModelButtonId)
			JSHelpers::hideElementById(export3DModelButtonInnerId)
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
				JSHelpers::hideElementById(export3DModelButtonInnerId)
				JSHelpers::hideElementById(openAllComponentsButtonId)
				JSHelpers::hideElementById(performanceAnalysisButtonId)
				JSHelpers::hideElementById(virtualRealityModeButtonId)
				JSHelpers::hideElementById(databaseQueriesButtonId)
				JSHelpers::hideDialogById("performanceAnalysisDialog")
				JSHelpers::hideDialogById("searchDialog")
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

	def static showAndPrepareExport3DModelButton(Application application) {
		if (export3DModelAction1Handler != null) {
			export3DModelAction1Handler.removeHandler
		}
		if (export3DModelAction2Handler != null) {
			export3DModelAction2Handler.removeHandler
		}
		if (export3DModelAction3Handler != null) {
			export3DModelAction3Handler.removeHandler
		}
		if (export3DModelAction4Handler != null) {
			export3DModelAction4Handler.removeHandler
		}

		JSHelpers::showElementById(export3DModelButtonId)
		JSHelpers::showElementById(export3DModelButtonInnerId)

		export3DModelAction1Handler = createExport3DHandler("export3Daction1", 1, application)
		export3DModelAction2Handler = createExport3DHandler("export3Daction2", 2, application)
		export3DModelAction3Handler = createExport3DHandler("export3Daction3", 3, application)
		export3DModelAction4Handler = createExport3DHandler("export3Daction4", 4, application)
	}

	def static HandlerRegistration createExport3DHandler(String id, int type, Application app) {
		val export3DModelAction = RootPanel::get(id)

		export3DModelAction.sinkEvents(Event::ONCLICK)
		export3DModelAction.addHandler(
			[
				Usertracking::trackExport3DModel(app)
				JSHelpers::downloadAsFile(app.name + ".scad",
					OpenSCADApplicationExporter::exportApplicationAsOpenSCAD(app, type))
			], ClickEvent::getType())
	}

	def static showAndPreparePerformanceAnalysisButton(Application application) {
		if (performanceAnalysisHandler != null) {
			performanceAnalysisHandler.removeHandler
		}

		JSHelpers::showElementById(performanceAnalysisButtonId)

		val performanceAnalysis = RootPanel::get(performanceAnalysisButtonId)

		performanceAnalysis.sinkEvents(Event::ONCLICK)
		performanceAnalysisHandler = performanceAnalysis.addHandler(
			[
				PerformanceAnalysis::openDialog(application.name)
			], ClickEvent::getType())
	}

	def static showAndPrepareVirtualRealityModeButton() {
		if (virtualRealityModeHandler != null) {
			virtualRealityModeHandler.removeHandler
		}

		JSHelpers::showElementById(virtualRealityModeButtonId)

		val virtualReality = RootPanel::get(virtualRealityModeButtonId)

		virtualReality.sinkEvents(Event::ONCLICK)
		virtualRealityModeHandler = virtualReality.addHandler(
			[
				WebVRJS::goFullScreen
			], ClickEvent::getType())
	}
	
	def static showAndPrepareDatabaseQueriesButton(Application application) {
		if (databaseQueriesHandler != null) {
			databaseQueriesHandler.removeHandler
		}

		JSHelpers::showElementById(databaseQueriesButtonId)

		val database = RootPanel::get(databaseQueriesButtonId)

		database.sinkEvents(Event::ONCLICK)
		databaseQueriesHandler = database.addHandler(
			[
				DatabaseQueries::open(application)
			], ClickEvent::getType())
	}

	def static private void createComponentInteraction(Component component) {
		if (!Experiment::tutorial) {
			component.setMouseClickHandler(componentMouseClickHandler)
			component.setMouseRightClickHandler(componentMouseRightClickHandler)
			component.setMouseDoubleClickHandler(componentMouseDoubleClickHandler)

			for (clazz : component.clazzes)
				createClazzInteraction(clazz)

			for (child : component.children)
				createComponentInteraction(child)

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
				for (clazz : component.clazzes)
					createClazzInteraction(clazz)

				for (child : component.children)
					createComponentInteraction(child)
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
				if (!Experiment::tutorial || Experiment.getStep.leaveanalysis) {
					if (Experiment::tutorial && Experiment.getStep.leaveanalysis) {
						Experiment.incStep()
					}
					TraceHighlighter::reset(true)
					Usertracking::trackDraw3DNodeUnhighlightAll
					NodeHighlighter::unhighlight3DNodes()
				}
			}
			Usertracking::trackComponentClick(compo)
		]
	}

	def static private MouseRightClickHandler createComponentMouseRightClickHandler() {
		[
			val compo = it.object as Component
			Usertracking::trackComponentRightClick(compo)
			Experiment::incTutorial(compo.name, false, true, false, false)
			PopupService::showComponentPopupMenu(it.originalClickX, it.originalClickY, compo)
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
			PopoverService::showPopover(name, it.originalClickX, it.originalClickY,
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
			PopupService::showClazzPopupMenu(it.originalClickX, it.originalClickY, clazz)
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

	def static public int getCalledMethods(Clazz clazz) {
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
					if (communicationParam.source.fullQualifiedName ==
						NodeHighlighter::highlightedNode.fullQualifiedName) {
						communicationParam
					} else if (communicationParam.target.fullQualifiedName ==
						NodeHighlighter::highlightedNode.fullQualifiedName) {
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
			var methods = getMethodList(communication)
			var requests = communication.requests
			PopoverService::showPopover(
				sourceName + "<br><span class='glyphicon glyphicon-transfer'></span><br>" + targetName,
				it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Requests: </td><td style="text-align:right;padding-left:10px;">' +
					requests + '</td></tr></table><br>' + methods)
		]
	}

	private def static String getMethodList(CommunicationAppAccumulator communication) {
		var commuSorted = new ArrayList<CommunicationClazz>()
		commuSorted.addAll(communication.aggregatedCommunications)
		Collections.sort(commuSorted,
			[ c1, c2 |
				var c1DirectionArrow = if (isClazzChildOf(c1.target, communication.target)) {
						"r"
					} else {
						"l"
					}
				var c2DirectionArrow = if (isClazzChildOf(c2.target, communication.target)) {
						"r"
					} else {
						"l"
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

		var methods = ''

		var firstRight = true
		var firstLeft = true
		for (aggCommu : commuSorted) {
			if (isClazzChildOf(aggCommu.target, communication.target)) {
				if (firstRight) {
					val targetIsPackage = communication.target instanceof Component
					val highlightAsPackages = if (targetIsPackage) {
							"color:#555555;"
						} else
							""
					methods += "<div style='font-weight:bold;" + highlightAsPackages +
						"'>... <span class='glyphicon glyphicon-arrow-right'></span> " + communication.target.name +
						"</div><table style='width:100%'>"
					firstRight = false
				}
			} else {
				if (firstLeft) {
					if (!firstRight) {
						methods += "</table><br>"
					}
					val sourceIsPackage = communication.source instanceof Component
					val highlightAsPackages = if (sourceIsPackage) {
							"color:#555555;"
						} else
							""
					methods += "<div style='font-weight:bold;" + highlightAsPackages +
						"'>... <span class='glyphicon glyphicon-arrow-right'></span> " + communication.source.name +
						"</div><table style='width:100%'>"
					firstLeft = false
				}
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
					var methodWithParentheses = method + "(..)"
					methods += generateMethodRows(methodWithParentheses, methodWithParentheses.startsWith("new "))
				} else {
					methods += generateMethodRows(aggCommu.target.name, false)
					methods += "<tr><td></td><td style='padding-left:20px;'>" + "." + method + "(..)" + "</td></tr>"
				}
			}
		}
		methods += "</table>"
		methods
	}

	def static generateMethodRows(String content, boolean highlight) {
		"<tr><td>&#8211;</td><td style='padding-left:5px;'>" + if (highlight) {
			"<div style='color:#2456a1;font-weight:bold;'>" + content + "</div>"
		} else
			content + "</td></tr>"
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
