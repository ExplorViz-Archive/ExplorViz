package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.model.Component
import explorviz.shared.model.Landscape
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.renderer.ColorDefinitions

class LandscapeConverter<T> implements AsyncCallback<T> {

	var public static Landscape oldLandscape

	override onFailure(Throwable caught) {
		// TODO check for 0 (connection lost)
		//      new ErrorPage().renderWithMessage(pageControl, caught.getMessage())
	}

	def static reset() {
		destroyOldLandscape()
	}

	override onSuccess(T result) {
		val newLandscape = result as Landscape
		if (oldLandscape == null || newLandscape.hash != oldLandscape.hash) {
			if (oldLandscape != null) {
				destroyOldLandscape()
			}

			// TODO only update
			var landscapeCS = result as Landscape
			landscapeCS.systems.forEach [
				it.nodeGroups.forEach [
					it.nodes.forEach [
						it.applications.forEach [
									val foundationComponent = new Component()
									foundationComponent.setOpened(true)
									foundationComponent.name = it.name
									foundationComponent.fullQualifiedName = it.name
									foundationComponent.belongingApplication = it
									foundationComponent.color = ColorDefinitions::componentFoundationColor
									
									foundationComponent.children.addAll(it.components)
									
									foundationComponent.children.forEach [
										setComponentAttributes(it, 0, true)
									]
									
									it.components.clear()
									it.components.add(foundationComponent)
						]
					]
					
					it.setOpened(false) // TODO to server
				]
			]
			
			SceneDrawer::viewScene(landscapeCS, true)
			oldLandscape = landscapeCS
		}
	}

	def static destroyOldLandscape() {
		if (oldLandscape != null) {
			oldLandscape.destroy()
			oldLandscape = null
		}
	}
	
	def void setComponentAttributes(Component component, int index, boolean shouldBeOpened) {
		var openNextLevel = shouldBeOpened

		if (!openNextLevel) {
			component.opened = false
		} else if (component.children.size == 1) {
			component.opened = true
		} else {
			component.opened = true
			openNextLevel = false
		}
		
		if (index % 2 == 1) {
			component.color = ColorDefinitions::componentFirstColor
		} else {
			component.color = ColorDefinitions::componentSecondColor
		}
		
		for (child : component.children)
			setComponentAttributes(child, index+1, openNextLevel)
	}

// TODO move to server
//	def Communication convertToCommunicationCS(Communication communication, Landscape landscapeCS) {
//		val communicationCS = new Communication()
//		communicationCS.requests = communication.requests
//		
//		communicationCS.source = seekForIdApplication(communication.source.id, landscapeCS)
//		communicationCS.target = seekForIdApplication(communication.target.id, landscapeCS)
//
//		if (communicationCS.source != null && communication.sourceClazz != null) {
//			communicationCS.sourceClazz = seekForClazz(communication.sourceClazz, communicationCS.source.components)
//			communicationCS.source.outgoingCommunications.add(communicationCS)
//		}
//		if (communicationCS.target != null && communication.targetClazz != null) {
//			communicationCS.targetClazz = seekForClazz(communication.targetClazz, communicationCS.target.components)
//			communicationCS.target.incomingCommunications.add(communicationCS)
//		}
//
//		communicationCS
//	}
}
