package explorviz.visualization.interaction

import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.model.CommunicationClazzClientSide

class ApplicationInteraction {
	def static void clearInteraction(ApplicationClientSide application) {
		application.components.forEach [
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
		application.components.forEach [
			createComponentInteraction(it)
		]
		
		application.communications.forEach [
			createCommunicationInteraction(it)
		]
	}
	
	def static private void createComponentInteraction(ComponentClientSide component) {
		component.addMouseRightClickHandler(createComponentMouseRightClickHandler())
		component.addMouseDoubleClickHandler(createComponentMouseDoubleClickHandler())
		
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
			SceneDrawer::createObjectsFromApplication(component.belongingApplication, true)
		]
	}
	
	def static private void createClazzInteraction(ClazzClientSide clazz) {
		clazz.addMouseRightClickHandler(createClazzMouseRightClickHandler())
		clazz.addMouseDoubleClickHandler([])
	}

	def static private MouseRightClickHandler createClazzMouseRightClickHandler() {
		[
			val clazz = it.object as ClazzClientSide
			Usertracking::trackClazzRightClick(clazz)
			PopupService::showClazzPopupMenu(it.originalClickX, it.originalClickY, clazz)
		]
	}
	
	def static private createCommunicationInteraction(CommunicationClazzClientSide communication) {
		communication.addMouseClickHandler(createCommunicationMouseClickHandler())
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