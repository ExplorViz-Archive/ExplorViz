package explorviz.visualization.engine.main

import explorviz.shared.model.Application

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
//				for (clazz : component.clazzes)
//			if (component.opened)
//				drawClazz(clazz)

		var component = application.components.get(0)

		var float[] geometry;
		
		for (child : component.children) {
			
//			if (child.opened) {
////				drawOpenedComponent(child, index + 1)
//			} else {
//				if (component.opened) {
////					drawClosedComponent(child)
//				}
			}	
	}
}