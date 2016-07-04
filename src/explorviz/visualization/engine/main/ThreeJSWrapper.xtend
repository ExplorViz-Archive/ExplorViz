package explorviz.visualization.engine.main

import explorviz.shared.model.Application
import explorviz.visualization.renderer.ThreeJSRenderer
import explorviz.shared.model.Component
import explorviz.visualization.renderer.ViewCenterPointerCalculator
import explorviz.visualization.engine.math.Vector3f

class ThreeJSWrapper {

	static var Application application
	private static var Vector3f viewCenterPoint
	private static var boolean doAnimation

	def static update(Application app, boolean doAnim) {
		application = app

		if (viewCenterPoint == null || !doAnim) {
			viewCenterPoint = ViewCenterPointerCalculator::calculateAppCenterAndZZoom(application)
		}

		doAnimation = doAnim
	}

	def static parseApplication() {
		ThreeJSRenderer::deleteMeshes()
		parseComponents()
	// ...
	}

	def static parseComponents() {

		var component = application.components.get(0);
//		ThreeJSRenderer.passSystem(component.name, component.depth, component.width, component.height,component.positionX, component.positionY, component.positionZ);
		// ThreeJSRenderer.passResetCamera(component.depth, component.width, component.height,component.positionX, component.positionY, component.positionZ);
		drawComponent(component);

		if (!doAnimation)
			ThreeJSRenderer::resetCamera()
	}

	def static void drawComponent(Component component) {

		var centerPoint = component.centerPoint.sub(viewCenterPoint)

		var extensionForPoint = component.extension

		ThreeJSRenderer::createBoxes(component.name, extensionForPoint.x, extensionForPoint.z, extensionForPoint.y,
			centerPoint.x * 0.5f, centerPoint.y * 0.5f, centerPoint.z * 0.5f)

//for (clazz : component.clazzes)
//			if (component.opened)
//				drawClazz(clazz)
		for (child : component.children) {
			if (child.opened) {
				drawComponent(child)
			} else {
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
