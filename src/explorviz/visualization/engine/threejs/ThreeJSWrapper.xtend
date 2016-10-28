package explorviz.visualization.engine.threejs

import explorviz.shared.model.Application
import explorviz.visualization.renderer.ThreeJSRenderer
import explorviz.shared.model.Component
import explorviz.visualization.renderer.ViewCenterPointerCalculator
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Box
import explorviz.visualization.renderer.ColorDefinitions
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.primitives.Pipe
import java.util.List
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.picking.ClickEvent
import explorviz.visualization.engine.picking.EventObserver
import explorviz.shared.model.Clazz
import explorviz.visualization.interaction.ApplicationInteraction

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
		parseCommunication()
		parseComponents()
	// ThreeJSRenderer::addLabels()
	}

	def static parseCommunication() {
		drawInAndOutCommunication(application.communicationsAccumulated)
	}

	def static drawInAndOutCommunication(List<CommunicationAppAccumulator> communicationsAccumulated) {
		for (commu : communicationsAccumulated) {
			if (commu.source != commu.target) {
				commu.primitiveObjects.clear()

				if (commu.points.size >= 2) {
					val transparent = commu.state == EdgeState.TRANSPARENT

					val color = if (transparent)
							ColorDefinitions::pipeColorTrans
						else if (commu.state == EdgeState.REPLAY_HIGHLIGHT)
							ColorDefinitions::pipeHighlightColor
						else
							ColorDefinitions::pipeColor

					val pipe = new Pipe(transparent, true, color)

					pipe.setLineThickness(commu.pipeSize)

					val start = commu.points.get(0).sub(viewCenterPoint)
					val end = commu.points.get(1).sub(viewCenterPoint)

					pipe.addPoint(start.mult(0.5f))
					pipe.addPoint(end.mult(0.5f))

					ThreeJSRenderer::createPipe(pipe, start.mult(0.5f), end.mult(0.5f), commu)

				}
			}
		}
	}

	def static parseComponents() {
		var component = application.components.get(0);
		drawComponent(component);

		if (!doAnimation)
			ThreeJSRenderer::resetCamera()
	}

	def static void drawComponent(Component component) {

		var centerPoint = component.centerPoint.sub(viewCenterPoint)

		var Box package = new Box(centerPoint.mult(0.5f), component)

		ThreeJSRenderer::createBox(package, component, component.name, false, component.opened, component.foundation)

		// create classes 
		for (clazz : component.clazzes) {
			if (component.opened) {
				var classCenter = clazz.centerPoint.sub(viewCenterPoint)
				var color = ColorDefinitions::clazzColor

				if (clazz.highlighted)
					color = ColorDefinitions::highlightColor

				var Box class = new Box(new Vector3f(classCenter.x * 0.5f, classCenter.y * 0.5f, classCenter.z * 0.5f),
					clazz.extension, color)
				ThreeJSRenderer::createBox(class, clazz, clazz.name, true, false, false)
			}
		}

		// iterate through child components
		for (child : component.children) {
			if (child.opened) {
				drawComponent(child)
			} else {
				if (component.opened) {
					drawComponent(child)
				}
			}
		}
	}

	def static handleEvents(String eventType, EventObserver entity, int x, int y) {

		if (entity == null || entity.mouseHoverHandler == null)
			return;

		val clickEvent = new ClickEvent()
		clickEvent.positionX = x
		clickEvent.positionX = y
		clickEvent.originalClickX = x
		clickEvent.originalClickY = y
		clickEvent.object = entity

		switch (eventType) {
			case "doubleClick": {
				entity.mouseDoubleClickHandler.handleDoubleClick(clickEvent)
			}
			case "singleClick": {
				entity.mouseClickHandler.handleClick(clickEvent)
			}
			case "hover": {
				entity.mouseHoverHandler.handleHover(clickEvent)
			}
			default: {
			}
		}
	}

	def static String[] getHoverInformation(Draw3DNodeEntity entity, String type) {
		
		if (entity == null)
			return null

		if (type.equals("class")) {
			var clazz = entity as Clazz

			val String[] text = newArrayOfSize(2)

			var calledClasses = ApplicationInteraction::getCalledMethods(clazz)

			text.set(0, "Active Instances: " + clazz.instanceCount)
			text.set(1, "Called Methods: " + calledClasses)

			return text
		}
		
		if (type.equals("package")) {
			var component = entity as Component

			val String[] text = newArrayOfSize(2)

			var containedClasses = ApplicationInteraction::getClazzesCount(component)
			var containedPackages = ApplicationInteraction::getPackagesCount(component)

			text.set(0, "Contained Classes: " + containedClasses)
			text.set(1, "Contained Packages: " + containedPackages)

			return text
		}
		
		return null

	}

}
