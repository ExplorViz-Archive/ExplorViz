package explorviz.visualization.renderer

import elemental.html.WebGLTexture
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.math.Matrix44f
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.engine.primitives.Pipe
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.primitives.Triangle
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import java.util.ArrayList
import java.util.List

class ApplicationRenderer {
	static var Vector3f centerPoint
	static val List<Triangle> labels = new ArrayList<Triangle>(64)

	static val List<Component> laterDrawComponent = new ArrayList<Component>(64)
	static val List<Clazz> laterDrawClazz = new ArrayList<Clazz>(64)
	static val List<CommunicationAppAccumulator> laterDrawCommunication = new ArrayList<CommunicationAppAccumulator>(64)

	static val Vector4f WHITE = new Vector4f(1f, 1f, 1f, 1f)
	static val Vector4f BLACK = new Vector4f(0f, 0f, 0f, 1f)

	static val incomePicture = TextureManager::createTextureFromImagePath("in_colored.png")
	static val outgoingPicture = TextureManager::createTextureFromImagePath("out.png")

	static val MIN_X = 0
	static val MAX_X = 1
	static val MIN_Y = 2
	static val MAX_Y = 3
	static val MIN_Z = 4
	static val MAX_Z = 5

	static var Long traceToHighlight = null

	def static void highlightTrace(Long traceId) {
		traceToHighlight = traceId
	}

	def static void unhighlightTrace() {
		traceToHighlight = null
	}

	def static void drawApplication(Application application, List<Triangle> polygons, boolean firstViewAfterChange) {
		labels.clear()
		application.clearAllPrimitiveObjects

		if (centerPoint == null || firstViewAfterChange) {

			// TODO this is just the foundation size...
			val rect = getApplicationRect(application)
			val SPACE = 15f

			centerPoint = new Vector3f(rect.get(MIN_X) + ((rect.get(MAX_X) - rect.get(MIN_X)) / 2f),
				rect.get(MIN_Y) + ((rect.get(MAX_Y) - rect.get(MIN_Y)) / 2f),
				rect.get(MIN_Z) + ((rect.get(MAX_Z) - rect.get(MIN_Z)) / 2f))

			var modelView = new Matrix44f();
			modelView = Matrix44f.rotationX(33).mult(modelView)
			modelView = Matrix44f.rotationY(45).mult(modelView)

			val southPoint = new Vector4f(rect.get(MIN_X), rect.get(MIN_Y), rect.get(MAX_Z), 1.0f).sub(
				new Vector4f(centerPoint, 0.0f))
			val northPoint = new Vector4f(rect.get(MAX_X), rect.get(MAX_Y), rect.get(MIN_Z), 1.0f).sub(
				new Vector4f(centerPoint, 0.0f))

			val westPoint = new Vector4f(rect.get(MIN_X), rect.get(MIN_Y), rect.get(MIN_Z), 1.0f).sub(
				new Vector4f(centerPoint, 0.0f))
			val eastPoint = new Vector4f(rect.get(MAX_X), rect.get(MAX_Y), rect.get(MAX_Z), 1.0f).sub(
				new Vector4f(centerPoint, 0.0f))

			val requiredWidth = Math.abs(modelView.mult(westPoint).x - modelView.mult(eastPoint).x) + SPACE
			val requiredHeight = Math.abs(modelView.mult(southPoint).y - modelView.mult(northPoint).y) + SPACE

			val perspective_factor = WebGLStart::viewportWidth / WebGLStart::viewportHeight as float

			val newZ_by_width = requiredWidth * -1f / perspective_factor
			val newZ_by_height = requiredHeight * -1f

			Camera::getVector.z = Math.min(Math.min(newZ_by_width, newZ_by_height), -15f)
		}

		application.incomingCommunications.forEach [
			drawIncomingCommunication(it, polygons)
		]

		application.outgoingCommunications.forEach [
			drawOutgoingCommunication(it, polygons)
		]

		application.components.forEach [
			drawOpenedComponent(it, polygons, 0)
		]

		drawCommunications(application.communicationsAccumulated, polygons)
		
		laterDrawCommunication.forEach [
			drawCommunication(points, it.pipeSize, 0, polygons, it, false) // TODO average response time
		]
		laterDrawCommunication.clear()

		laterDrawComponent.forEach [
			drawClosedComponents(it, polygons)
		]
		laterDrawComponent.clear()

		laterDrawClazz.forEach [
			drawClazz(it, polygons)
		]
		laterDrawClazz.clear()

		polygons.addAll(labels)
	}

	def private static void drawIncomingCommunication(Communication commu, List<Triangle> polygons) {
		drawInAndOutCommunication(commu, commu.source.name, incomePicture, polygons)
	}

	def private static void drawOutgoingCommunication(Communication commu, List<Triangle> polygons) {

		drawInAndOutCommunication(commu, commu.target.name, outgoingPicture, polygons)
	}

	def private static void drawInAndOutCommunication(Communication commu, String otherApplication,
		WebGLTexture picture, List<Triangle> polygons) {
		val center = new Vector3f(commu.pointsFor3D.get(0)).sub(centerPoint)

		val quad = new Quad(center, ApplicationLayoutInterface::externalPortsExtension, picture, null, true)

		val label = createLabel(center,
			new Vector3f(ApplicationLayoutInterface::externalPortsExtension.x * 8f,
				ApplicationLayoutInterface::externalPortsExtension.y + 4f,
				ApplicationLayoutInterface::externalPortsExtension.z * 8f), otherApplication, BLACK)

		commu.pointsFor3D.forEach [ point, i |
			if (i < commu.pointsFor3D.size - 1) {
				val pipe = createPipe(point, commu.pointsFor3D.get(i + 1), commu.lineThickness, false)

				//commu.primitiveObjects.add(pipe) TODO
				pipe.quads.forEach [
					polygons.addAll(it.triangles)
				]
			}
		]

		labels.addAll(quad.triangles)
		labels.addAll(label.triangles)
	}

	def private static drawCommunications(List<CommunicationAppAccumulator> communicationsAccumulated,
		List<Triangle> polygons) {
		communicationsAccumulated.forEach [
			var hide = false
			if (traceToHighlight != null) {
				var found = false
				for (aggCommu : it.aggregatedCommunications) {
					if (aggCommu.traceIdToRuntimeMap.get(traceToHighlight) != null) {
						found = true
					}
				}

				hide = !found
			} else {
				hide = false
			}
			
			Experiment::draw3DTutorialCom(it.source.name, it.target.name, points.get(0), points.get(1), centerPoint,
				polygons)
			
			if (!hide) {
				laterDrawCommunication.add(it)
			} else {
				drawCommunication(points, it.pipeSize, 0, polygons, it, hide) // TODO average response time
			}
		]
	}

	def private static drawCommunication(List<Vector3f> points, float pipeSize, float averageResponseTime,
		List<Triangle> polygons, CommunicationAppAccumulator commu, boolean hide) {
		for (var i = 0; i < points.size - 1; i++) {
			val pipe = createPipe(points.get(i), points.get(i + 1), pipeSize, hide)

			commu.primitiveObjects.add(pipe)
			pipe.quads.forEach [
				polygons.addAll(it.triangles)
			]
		}
	}

	def private static createPipe(Vector3f start, Vector3f end, float lineThickness, boolean hide) {
		val communicationPipe = new Pipe()

		if (hide) {
			communicationPipe.setTransparent(true)
			communicationPipe.setColor(ColorDefinitions::pipeColorTrans)
		} else {
			communicationPipe.setTransparent(true)
			communicationPipe.setColor(ColorDefinitions::pipeColor)
		}

		communicationPipe.setLineThickness(lineThickness)
		communicationPipe.begin
		communicationPipe.addPoint(start.sub(centerPoint))
		communicationPipe.addPoint(end.sub(centerPoint))
		communicationPipe.end
		communicationPipe
	}

	def private static void drawOpenedComponent(Component component, List<Triangle> polygons, int index) {
		val box = component.createBox(centerPoint, component.color)

		val labelCenterPoint = new Vector3f(
			component.centerPoint.x - component.width / 2.0f + ApplicationLayoutInterface::labelInsetSpace / 2.0f +
				ApplicationLayoutInterface::insetSpace / 2f, component.centerPoint.y, component.centerPoint.z).sub(centerPoint)
		val labelExtension = new Vector3f(component.extension.x, component.extension.y / 2f,
			component.extension.z / 2f)
		val label = createLabelOpenPackages(labelCenterPoint, labelExtension, component.name,
			if (index == 0) BLACK else WHITE)

		component.primitiveObjects.add(box)

		box.quads.forEach [
			polygons.addAll(it.triangles)
		]
		labels.addAll(label.triangles)

		component.clazzes.forEach [
			if (component.opened) {

				//drawClazzes(it, polygons)
				laterDrawClazz.add(it)
			}
		]

		component.children.forEach [
			if (it.opened) {
				drawOpenedComponent(it, polygons, index + 1)
			} else {
				if (component.opened) {
					laterDrawComponent.add(it)
				}
			}
		]

		val arrow = Experiment::draw3DTutorial(component.name,
			new Vector3f(component.positionX, component.positionY, component.positionZ), component.width,
			component.height, component.depth, centerPoint, polygons)
		component.primitiveObjects.addAll(arrow)
	}

	def private static void drawClosedComponents(Component component, List<Triangle> polygons) {
		val box = component.createBox(centerPoint, component.color)
		val label = createLabel(component.centerPoint.sub(centerPoint), component.extension, component.name, WHITE)

		component.primitiveObjects.add(box)

		box.quads.forEach [
			polygons.addAll(it.triangles)
		]
		labels.addAll(label.triangles)

		val arrow = Experiment::draw3DTutorial(component.name,
			new Vector3f(component.positionX, component.positionY, component.positionZ), component.width,
			component.height, component.depth, centerPoint, polygons)
		component.primitiveObjects.addAll(arrow)
	}

	def private static void drawClazz(Clazz clazz, List<Triangle> polygons) {
		val box = clazz.createBox(centerPoint, ColorDefinitions::clazzColor)
		val label = createLabel(
			new Vector3f(clazz.positionX - centerPoint.x + clazz.width / 2f,
				clazz.positionY - centerPoint.y + clazz.height / 2f,
				clazz.positionZ - centerPoint.z + clazz.depth / 2f),
			new Vector3f(clazz.width / 2f, clazz.height / 2f, clazz.depth / 2f),
			clazz.name,
			WHITE
		)

		clazz.primitiveObjects.add(box)

		box.quads.forEach [
			polygons.addAll(it.triangles)
		]
		labels.addAll(label.triangles)

		val arrow = Experiment::draw3DTutorial(clazz.name,
			new Vector3f(clazz.positionX, clazz.positionY, clazz.positionZ), clazz.width, clazz.height, clazz.depth,
			centerPoint, polygons)
		clazz.primitiveObjects.addAll(arrow)
	}

	def private static createLabel(Vector3f center, Vector3f itsExtension, String label, Vector4f color) {
		val texture = TextureManager::createTextureFromTextWithColor(label, 1024, 1024, color)

		val normalY = center.y + itsExtension.y + 0.02f
		val heigheredY = center.y + itsExtension.y + 0.02f

		val xExtension = Math::max(itsExtension.x * 5f, 13f)
		val zExtension = xExtension

		new Quad(
			new Vector3f(center.x - xExtension, normalY, center.z),
			new Vector3f(center.x, normalY, center.z + zExtension),
			new Vector3f(center.x + xExtension, heigheredY, center.z),
			new Vector3f(center.x, heigheredY, center.z - zExtension),
			texture,
			true
		)
	}

	def private static createLabelOpenPackages(Vector3f center, Vector3f itsExtension, String label, Vector4f color) {
		val texture = TextureManager::createTextureFromTextWithColor(label, 1024, 1024, color)

		val yValue = center.y + 0.02f

		val xExtension = itsExtension.x
		val zExtension = Math::max(itsExtension.z * 5f, 13f)

		new Quad(
			new Vector3f(center.x - xExtension, yValue, center.z - zExtension),
			new Vector3f(center.x - xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z - zExtension),
			texture,
			true
		)
	}

	def private static getApplicationRect(Application application) {
		val rect = new ArrayList<Float>
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)

		getMinMaxFromQuad(application.components.get(0), rect)

		rect
	}

	def private static getMinMaxFromQuad(Draw3DNodeEntity entity, ArrayList<Float> rect) {
		val curX = entity.positionX
		val curY = entity.positionY
		val curZ = entity.positionZ

		if (curX < rect.get(MIN_X)) {
			rect.set(MIN_X, curX)
		}
		if (rect.get(MAX_X) < curX + (entity.width)) {
			rect.set(MAX_X, curX + (entity.width))
		}

		if (curY < rect.get(MIN_Y)) {
			rect.set(MIN_Y, curY)
		}
		if (rect.get(MAX_Y) < curY + (entity.height)) {
			rect.set(MAX_Y, curY + (entity.height))
		}

		if (curZ < rect.get(MIN_Z)) {
			rect.set(MIN_Z, curZ)
		}
		if (rect.get(MAX_Z) < curZ + (entity.depth)) {
			rect.set(MAX_Z, curZ + (entity.depth))
		}
	}
}
