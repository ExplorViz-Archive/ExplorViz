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
import explorviz.visualization.model.CommunicationAppAccumulator

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

		application.communicationsAccumulated.forEach [
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
		tutorialContinuesHere = false
		application.components.get(0).children.forEach [
			createComponentInteraction(it)
		]

		application.communicationsAccumulated.forEach [
			createCommunicationInteraction(it)
		]
		if(!tutorialContinuesHere || !Experiment::tutorial){
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
			
			if(Experiment::tutorial && Experiment::getStep().backToLandscape){
				Experiment::incStep()
			}
			
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
		if(!Experiment::tutorial){
			component.setMouseRightClickHandler(componentMouseRightClickHandler)
			component.setMouseDoubleClickHandler(componentMouseDoubleClickHandler)
	
			component.clazzes.forEach [
				createClazzInteraction(it)
			]
	
			component.children.forEach [
				createComponentInteraction(it)
			]	
		}else{//Tutorialmodus active, only set correct handler or go further into the component
			val step = Experiment::getStep()
			if(!step.connection && component.name.equals(step.source)){
				tutorialContinuesHere = true
				if(step.rightClick){
					component.setMouseRightClickHandler(componentMouseRightClickHandler)
				}else if(step.doubleClick){
					component.setMouseDoubleClickHandler(componentMouseDoubleClickHandler)
				}
			}else{
				component.clazzes.forEach [
					createClazzInteraction(it)
				]
	
				component.children.forEach [
					createComponentInteraction(it)
				]	
			}
		}
	}

	def static private MouseRightClickHandler createComponentMouseRightClickHandler() {
		[
			val compo = it.object as ComponentClientSide
			Usertracking::trackComponentRightClick(compo)
			Experiment::incTutorial(compo.name, false, true, false)
			PopupService::showComponentPopupMenu(it.originalClickX, it.originalClickY, compo)
		]
	}

	def static private MouseDoubleClickHandler createComponentMouseDoubleClickHandler() {
		[
			val component = it.object as ComponentClientSide
			Usertracking::trackComponentDoubleClick(component)
			component.opened = !component.opened
			Experiment::incTutorial(component.name, false, false, true)
			SceneDrawer::createObjectsFromApplication(component.belongingApplication, true)
		]
	}

	def static private void createClazzInteraction(ClazzClientSide clazz) {
		if(!Experiment::tutorial){
			clazz.setMouseRightClickHandler(clazzMouseRightClickHandler)
			clazz.setMouseDoubleClickHandler(clazzMouseDoubleClickHandler)
		}else if(!Experiment::getStep().connection && clazz.name.equals(Experiment::getStep().source)){
			val step = Experiment::getStep()
			tutorialContinuesHere = true
			if(step.rightClick){
				clazz.setMouseRightClickHandler(clazzMouseRightClickHandler)
			}else if(step.doubleClick){
				clazz.setMouseDoubleClickHandler(clazzMouseDoubleClickHandler)
			}
		}
	}

	def static private MouseRightClickHandler createClazzMouseRightClickHandler() {
		[
			val clazz = it.object as ClazzClientSide
			Usertracking::trackClazzRightClick(clazz)
			Experiment::incTutorial(clazz.name, false, true, false)
			PopupService::showClazzPopupMenu(it.originalClickX, it.originalClickY, clazz)
		]
	}

	def static private MouseDoubleClickHandler createClazzMouseDoubleClickHandler() {
		[
			//incTutorial(clazz.name, false, false, true)
		]
	}

	def static private createCommunicationInteraction(CommunicationAppAccumulator communication) {
		if(!Experiment::tutorial 
			|| (Experiment::getStep().connection && Experiment::getStep().source.equals(communication.source.name) 
				&& Experiment::getStep().dest.equals(communication.target.name)
			) 
		){
			tutorialContinuesHere = true
			communication.setMouseClickHandler(communicationMouseClickHandler)
		}
	}

	def static private MouseClickHandler createCommunicationMouseClickHandler() {
		[
			Usertracking::trackCommunicationClick(it.object as CommunicationAppAccumulator)
		//			val communication = (it.object as CommunicationClazzClientSide)
		//			Experiment::incTutorial(communication.source.name, communication.target.name, true, false)
		//			Window::alert(
		//				"Clicked communication between " + communication.source.fullQualifiedName + " and " + communication.target.fullQualifiedName +
		//					" with requests per second: " + communication.requestsPerSecond)
		]
	}
	
}
