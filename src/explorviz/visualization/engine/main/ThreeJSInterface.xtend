package explorviz.visualization.engine.main

import explorviz.shared.model.Application
import explorviz.visualization.renderer.ThreeJSRenderer
import com.google.gwt.core.client.JsArrayNumber
import com.google.gwt.core.client.JavaScriptObject
import explorviz.visualization.engine.Logging
import explorviz.shared.model.Component

class ThreeJSInterface {

	static var Application application

	static var JsArrayNumber box_geometry

	def static update(Application app) {
		application = app
		box_geometry = JavaScriptObject.createArray().cast();
	}

	def static parseApplication() {
		parseComponents(0,application.components.get(0))

	//...
	}

	def private static parseComponents(int cnt, Component comp) {

		//				for (clazz : component.clazzes)
		//			if (component.opened)
		//				drawClazz(clazz)
		
		var tempCnt = cnt
		
		for (child : comp.children) {	
			
			// TODO recursive calls for every child with no index 
			// malfunction		

			//			if (child.opened) {
			//				drawOpenedComponent(child, index + 1)
			//			} else {
			//				if (component.opened) {
			//					drawClosedComponent(child)
			//				}
			box_geometry.set(tempCnt, child.height)
			box_geometry.set(tempCnt + 1, child.width)
			box_geometry.set(tempCnt + 2, child.depth)
			box_geometry.set(tempCnt + 3, child.positionX)
			box_geometry.set(tempCnt + 4, child.positionY)
			box_geometry.set(tempCnt + 5, child.positionZ)	
			
			tempCnt += 6		
		}

		ThreeJSRenderer::boxCreation()
	}

	def static render() {
		ThreeJSRenderer.render()
	}
}
