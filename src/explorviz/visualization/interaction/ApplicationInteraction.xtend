package explorviz.visualization.interaction

import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.ui.RootPanel
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.model.CommunicationClazzClientSide
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.export.OpenSCADApplicationExporter
import explorviz.visualization.experiment.Experiment

class ApplicationInteraction {
	static val MouseRightClickHandler componentMouseRightClickHandler = createComponentMouseRightClickHandler()
	static val MouseDoubleClickHandler componentMouseDoubleClickHandler = createComponentMouseDoubleClickHandler()

	static val MouseRightClickHandler clazzMouseRightClickHandler = createClazzMouseRightClickHandler()
	static val MouseDoubleClickHandler clazzMouseDoubleClickHandler = createClazzMouseDoubleClickHandler()

	static val MouseClickHandler communicationMouseClickHandler = createCommunicationMouseClickHandler()
	
	static HandlerRegistration backToLandscapeHandler
	static HandlerRegistration export3DModelHandler
	
	static val backToLandscapeButtonId = "backToLandscapeBtn"
	static val export3DModelButtonId = "export3DModelBtn"
	
	static var tutorialContinuesHere = false

	def static void clearInteraction(ApplicationClientSide application) {
		ObjectPicker::clear()

		application.components.get(0).children.forEach [
			clearComponentInteraction(it)
		]

		application.communications.forEach [
			it.clearAllHandlers()
		]
	}

	def static private void clearComponentInteraction(ComponentClientSide component) {
		component.clearAllHandlers()

		component.clazzes.forEach [
			it.clearAllHandlers()
		]

		component.children.forEach [
			clearComponentInteraction(it)
		]
	}

	def static void createInteraction(ApplicationClientSide application) {
		application.components.get(0).children.forEach [
			createComponentInteraction(it)
		]

		application.communications.forEach [
			createCommunicationInteraction(it)
		]
		
		if(!tutorialContinuesHere){
			showAndPrepareBackToLandscapeButton(application)
		}
		showAndPrepareExport3DModelButton(application)
	}
	
	def static showAndPrepareBackToLandscapeButton(ApplicationClientSide application) {
		if (backToLandscapeHandler != null) {
			backToLandscapeHandler.removeHandler
		}
		
		JSHelpers::showElementById(backToLandscapeButtonId)
		
		val landscapeBack = RootPanel::get(backToLandscapeButtonId)
		
		landscapeBack.sinkEvents(Event::ONCLICK)
		backToLandscapeHandler = landscapeBack.addHandler([
			JSHelpers::hideElementById(backToLandscapeButtonId)
			JSHelpers::hideElementById(export3DModelButtonId)
			
			SceneDrawer::createObjectsFromLandscape(application.parent.parent.parent.parent, false)
		], ClickEvent::getType())
	}
	
	def static showAndPrepareExport3DModelButton(ApplicationClientSide application) {
		if (export3DModelHandler != null) {
			export3DModelHandler.removeHandler
		}
		
		JSHelpers::showElementById(export3DModelButtonId)
		
		val export3DModel = RootPanel::get(export3DModelButtonId)
		
		export3DModel.sinkEvents(Event::ONCLICK)
		export3DModelHandler = export3DModel.addHandler([
			JSHelpers::downloadAsFile(application.name + ".scad", OpenSCADApplicationExporter::exportApplicationAsOpenSCAD(application))
		], ClickEvent::getType())
	}

	def static private void createComponentInteraction(ComponentClientSide component) {
		component.setMouseRightClickHandler(componentMouseRightClickHandler)
		component.setMouseDoubleClickHandler(componentMouseDoubleClickHandler)

		component.clazzes.forEach [
			createClazzInteraction(it)
		]

		component.children.forEach [
			createComponentInteraction(it)
		]
	}

	def static private MouseRightClickHandler createComponentMouseRightClickHandler() {
		[
			val compo = it.object as ComponentClientSide
			Usertracking::trackComponentRightClick(compo)
			PopupService::showComponentPopupMenu(it.originalClickX, it.originalClickY, compo)
		]
	}

	def static private MouseDoubleClickHandler createComponentMouseDoubleClickHandler() {
		[
			val component = it.object as ComponentClientSide
			Usertracking::trackComponentDoubleClick(component)
			component.opened = !component.opened
			if(Experiment::tutorial){
				val step = Experiment::getStep()
				if(!step.connection && component.name.equals(step.source) && component.opened == step.opened){
					Experiment::incStep()
				}
			}
			SceneDrawer::createObjectsFromApplication(component.belongingApplication, true)
		]
	}

	def static private void createClazzInteraction(ClazzClientSide clazz) {
		clazz.setMouseRightClickHandler(clazzMouseRightClickHandler)
		clazz.setMouseDoubleClickHandler(clazzMouseDoubleClickHandler)
	}

	def static private MouseRightClickHandler createClazzMouseRightClickHandler() {
		[
			val clazz = it.object as ClazzClientSide
			Usertracking::trackClazzRightClick(clazz)
			PopupService::showClazzPopupMenu(it.originalClickX, it.originalClickY, clazz)
		]
	}

	def static private MouseDoubleClickHandler createClazzMouseDoubleClickHandler() {
		[]
	}

	def static private createCommunicationInteraction(CommunicationClazzClientSide communication) {
		communication.setMouseClickHandler(communicationMouseClickHandler)
	}

	def static private MouseClickHandler createCommunicationMouseClickHandler() {
		[
			Usertracking::trackCommunicationClick(it.object as CommunicationClazzClientSide)
		//			val communication = (it.object as CommunicationClazzClientSide)
		//			Window::alert(
		//				"Clicked communication between " + communication.source.fullQualifiedName + " and " + communication.target.fullQualifiedName +
		//					" with requests per second: " + communication.requestsPerSecond)
		]
	}
}
