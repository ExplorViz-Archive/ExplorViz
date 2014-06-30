package explorviz.visualization.interaction

import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.safehtml.shared.SafeHtmlUtils
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.Window
import com.google.gwt.user.client.ui.RootPanel
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseHoverHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.engine.popover.PopoverService
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.export.OpenSCADApplicationExporter
import explorviz.visualization.main.JSHelpers

class ApplicationInteraction {
	static val MouseRightClickHandler componentMouseRightClickHandler = createComponentMouseRightClickHandler()
	static val MouseDoubleClickHandler componentMouseDoubleClickHandler = createComponentMouseDoubleClickHandler()

	static val MouseClickHandler clazzMouseClickHandler = createClazzMouseClickHandler()
	static val MouseRightClickHandler clazzMouseRightClickHandler = createClazzMouseRightClickHandler()
	static val MouseDoubleClickHandler clazzMouseDoubleClickHandler = createClazzMouseDoubleClickHandler()
	static val MouseHoverHandler clazzMouseHoverHandler = createClazzMouseHoverHandler()

	static val MouseClickHandler communicationMouseClickHandler = createCommunicationMouseClickHandler()
//	static val MouseHoverHandler communicationMouseHoverHandler = createCommunicationMouseHoverHandler()
	
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
		if(!Experiment::tutorial || Experiment::step.backToLandscape){
			showAndPrepareBackToLandscapeButton(application)
		}
		showAndPrepareExport3DModelButton(application)
	}
	
	def static showAndPrepareBackToLandscapeButton(Application application) {
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
	
	def static showAndPrepareExport3DModelButton(Application application) {
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

	def static private void createComponentInteraction(Component component) {
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
			val compo = it.object as Component
			Usertracking::trackComponentRightClick(compo)
			Experiment::incTutorial(compo.name, false, true, false)
			PopupService::showComponentPopupMenu(it.originalClickX, it.originalClickY, compo)
		]
	}

	def static private MouseDoubleClickHandler createComponentMouseDoubleClickHandler() {
		[
			val component = it.object as Component
			Usertracking::trackComponentDoubleClick(component)
			component.opened = !component.opened
			Experiment::incTutorial(component.name, false, false, true)
			SceneDrawer::createObjectsFromApplication(component.belongingApplication, true)
		]
	}

	def static private void createClazzInteraction(Clazz clazz) {
		if(!Experiment::tutorial){
			clazz.setMouseClickHandler(clazzMouseClickHandler)
			clazz.setMouseRightClickHandler(clazzMouseRightClickHandler)
			clazz.setMouseDoubleClickHandler(clazzMouseDoubleClickHandler)
			clazz.setMouseHoverHandler(clazzMouseHoverHandler)
		}else if(!Experiment::getStep().connection && clazz.name.equals(Experiment::getStep().source)){
			val step = Experiment::getStep()
			if(step.rightClick){
				clazz.setMouseRightClickHandler(clazzMouseRightClickHandler)
			}else if(step.doubleClick){
				clazz.setMouseDoubleClickHandler(clazzMouseDoubleClickHandler)
			}
		}
	}

	def static private MouseClickHandler createClazzMouseClickHandler() {
		[
			val clazz = it.object as Clazz
//			Usertracking::trackClazzRightClick(clazz) TODO
//			Experiment::incTutorial(clazz.name, false, true, false)
			clazz.primitiveObjects.get(0).highlight(new Vector4f(1.0f, 0.0f, 0.0f, 1.0f))
		]
	}
	
	def static private MouseRightClickHandler createClazzMouseRightClickHandler() {
		[
			val clazz = it.object as Clazz
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
	
	def static private MouseHoverHandler createClazzMouseHoverHandler() {
		[
			val clazz = it.object as Clazz
			// TODO
			//			Usertracking::trackNodeRightClick(node);
			PopoverService::showPopover(SafeHtmlUtils::htmlEscape(clazz.name) + " Information", it.originalClickX, it.originalClickY,
				'<table style="width:100%"><tr><td>Instances:</td><td>' + clazz.instanceCount +
					'</td></tr></table>')
		]
	}

	def static private createCommunicationInteraction(CommunicationAppAccumulator communication) {
		if(!Experiment::tutorial 
			|| (Experiment::getStep().connection && Experiment::getStep().source.equals(communication.source.name) 
				&& Experiment::getStep().dest.equals(communication.target.name)
			) 
		){
			communication.setMouseClickHandler(communicationMouseClickHandler)
//			communication.setMouseHoverHandler(communicationMouseHoverHandler)
		}
	}
	
	def static private MouseClickHandler createCommunicationMouseClickHandler() {
		[
			Usertracking::trackCommunicationClick(it.object as CommunicationAppAccumulator)
					val communication = (it.object as CommunicationAppAccumulator)
//					Experiment::incTutorial(communication.source.name, communication.target.name, true, false)
					Window::alert(
						"Clicked communication between " + communication.source.fullQualifiedName + " and " + communication.target.fullQualifiedName +
							" with requests this interval: " + communication.requests)
		]
	}
	
//	def static private MouseHoverHandler createCommunicationMouseHoverHandler() {
//		[
//			val communcation = it.object as CommunicationAppAccumulator
//			// TODO
//			//			Usertracking::trackNodeRightClick(node);
//			PopoverService::showPopover(SafeHtmlUtils::htmlEscape("") + " Information", it.originalClickX, it.originalClickY,
//				'<table style="width:100%"><tr><td>Requests:</td><td>' + communcation.requestCount +
//					'</td></tr></table>')
//		]
//	}
	
}
