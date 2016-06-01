package explorviz.visualization.engine.main

import explorviz.shared.model.Application
import explorviz.visualization.renderer.ThreeJSRenderer
import explorviz.shared.model.Component

class ThreeJSWrapper {
	
	static var Application application
	
	def static update(Application app) {	
		application = app		
	}
	
	def static parseApplication() {
		parseComponents()
		//...
	}
	
	def static parseComponents() {

		var component = application.components.get(0);
//		var float[] geometry;
//		ThreeJSRenderer.passSystem(component.name, component.depth, component.width, component.height,component.positionX, component.positionY, component.positionZ);
		ThreeJSRenderer.passResetCamera(component.depth, component.width, component.height,component.positionX, component.positionY, component.positionZ);
		drawComponent(component);
	}
	
	def static drawComponent(Component component) {
			ThreeJSRenderer.passPackage(component.name, component.depth, component.width, component.height,
			component.positionX, component.positionY, component.positionZ)
		
//for (clazz : component.clazzes)
//			if (component.opened)
//				drawClazz(clazz)
				
			for (child : component.children) {				
				if (child.opened) {
					drawComponent(child)
				}
				else {
					if (component.opened) {
						drawComponent(child)
					}
				}
			}
			
//			if (child.opened) {
////				drawOpenedComponent(child, index + 1)
//			} else {
//				if (component.opened) {
////					drawClosedComponent(child)
//				}
			}
}