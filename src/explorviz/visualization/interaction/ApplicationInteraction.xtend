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
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseHoverHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.engine.popover.PopoverService
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.export.OpenSCADApplicationExporter
import explorviz.visualization.main.ClientConfiguration
import explorviz.visualization.main.JSHelpers
import java.util.HashSet

class ApplicationInteraction {
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

	static val backToLandscapeButtonId = "backToLandscapeBtn"
	static val export3DModelButtonId = "export3DModelBtn"

	def static void clearInteraction(Application application) {
		ObjectPicker::clear()

		application.components.get(0).children.forEach [
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
		application.components.get(0).children.forEach [
			createComponentInteraction(it)
		]

		application.communicationsAccumulated.forEach [
			createCommunicationInteraction(it)
		]
		if (!Experiment::tutorial || Experiment::getStep.backToLandscape) {
			showAndPrepareBackToLandscapeButton(application)
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
				SceneDrawer::createObjectsFromLandscape(application.parent.parent.parent.parent, false)
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
			component.setMouseHoverHandler(componentMouseHoverHandler)

			component.clazzes.forEach [
				createClazzInteraction(it)
			]

			component.children.forEach [
				createComponentInteraction(it)
			]
		} else { //Tutorialmodus active, only set correct handler or go further into the component
			val step = Experiment::getStep()
			if (!step.connection && component.name.equals(step.source)) {
				if (step.rightClick) {
					component.setMouseRightClickHandler(componentMouseRightClickHandler)
				} else if (step.doubleClick) {
					component.setMouseDoubleClickHandler(componentMouseDoubleClickHandler)
				} else if (step.hover) {
					component.setMouseHoverHandler(componentMouseHoverHandler)
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
	}

	def static private MouseClickHandler createComponentMouseClickHandler() {
		[
			val compo = it.object as Component
			if (!compo.opened) {
				Experiment::incTutorial(compo.name, true, false, false, false)
				Usertracking::trackComponentClick(compo)
				NodeHighlighter::highlight3DNode(compo)
			}
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
			component.opened = !component.opened
			if (component.opened) {
				component.unhighlight
			}
			Experiment::incTutorial(component.name, false, false, true, false)
			SceneDrawer::createObjectsFromApplication(component.belongingApplication, true)
		]
	}

	def static private MouseHoverHandler createComponentMouseHoverHandler() {
		[
			val component = it.object as Component
			Experiment::incTutorial(component.name, false, false, false, true)
			Usertracking::trackComponentMouseHover(component)
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(component.name), it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Contained Classes:</td><td>' + getClazzesCount(component) +
					'</td></tr><tr><td>Contained Packages:</td><td>' + getPackagesCount(component) +
					'</td></tr></table>')
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
			clazz.setMouseHoverHandler(clazzMouseHoverHandler)
		} else if (!Experiment::getStep().connection && clazz.name.equals(Experiment::getStep().source)) {
			val step = Experiment::getStep()
			if (step.rightClick) {
				clazz.setMouseRightClickHandler(clazzMouseRightClickHandler)
			} else if (step.doubleClick) {
				clazz.setMouseDoubleClickHandler(clazzMouseDoubleClickHandler)
			} else if (step.leftClick) {
				clazz.setMouseClickHandler(clazzMouseClickHandler)
			} else if (step.hover) {
				clazz.setMouseHoverHandler(clazzMouseHoverHandler)
			}
		}
	}

	def static private MouseClickHandler createClazzMouseClickHandler() {
		[
			val clazz = it.object as Clazz
			Experiment::incTutorial(clazz.name, true, false, false, false)
			Usertracking::trackClazzClick(clazz)
			NodeHighlighter::highlight3DNode(clazz)
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
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(clazz.name), it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Active Instances:</td><td>' + clazz.instanceCount +
					'</td></tr><tr><td>Called Methods:</td><td>' + getCalledMethods(clazz) + '</td></tr></table>')
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
			communication.setMouseHoverHandler(communicationMouseHoverHandler)
		} else if (Experiment::getStep().connection && Experiment::getStep().source.equals(communication.source.name) &&
			Experiment::getStep().dest.equals(communication.target.name)) {
			val step = Experiment::getStep()
			if (step.leftClick) {
				communication.setMouseClickHandler(communicationMouseClickHandler)
			} else if (step.hover) {
				communication.setMouseHoverHandler(communicationMouseHoverHandler)
			}
		}
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
			val communication = (it.object as CommunicationAppAccumulator)
			Experiment::incTutorial(communication.source.name, communication.target.name, false, false, true)
			Usertracking::trackCommunicationMouseHover(communication)
			PopoverService::showPopover(
				SafeHtmlUtils::htmlEscape(communication.source.name + " <-> " + communication.target.name),
				it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Requests:</td><td>' + communication.requests +
					'</td></tr></table>')
		]
	}
}
