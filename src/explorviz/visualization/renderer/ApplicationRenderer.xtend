package explorviz.visualization.renderer

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.engine.primitives.Pipe
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.model.CommunicationClientSide
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.helper.Draw3DNodeEntity
import java.util.ArrayList
import java.util.List
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.model.helper.CommunicationAppAccumulator
import elemental.html.WebGLTexture

class ApplicationRenderer {
	static var Vector3f centerPoint
	static val List<PrimitiveObject> labels = new ArrayList<PrimitiveObject>(64)

	static val List<ComponentClientSide> laterDrawComponent = new ArrayList<ComponentClientSide>(64)
	static val List<ClazzClientSide> laterDrawClazz = new ArrayList<ClazzClientSide>(64)

	static val Vector4f WHITE = new Vector4f(1f, 1f, 1f, 1f)
	static val Vector4f BLACK = new Vector4f(0f, 0f, 0f, 1f)

	//	static val Vector4f BLUE = new Vector4f(193 / 255f, 0 / 255f, 79 / 255f, 1f)
	//	static val Vector4f RED = new Vector4f(240 / 255f, 240 / 255f, 10 / 255f, 1f)
	static val incomePicture = TextureManager::createTextureFromImagePath("in_colored.png")
	static val outgoingPicture = TextureManager::createTextureFromImagePath("out.png")

	def static drawApplication(ApplicationClientSide application, List<PrimitiveObject> polygons) {
		labels.clear()
		application.clearAllPrimitiveObjects()

		if (centerPoint == null) {
			centerPoint = getCenterPoint(application)
			Camera::vector.z = -100f
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

	def private static void drawIncomingCommunication(CommunicationClientSide commu, List<PrimitiveObject> polygons) {
		drawInAndOutCommunication(commu, commu.source.name, incomePicture, polygons)
	}

	def private static void drawOutgoingCommunication(CommunicationClientSide commu, List<PrimitiveObject> polygons) {

		drawInAndOutCommunication(commu, commu.target.name, outgoingPicture, polygons)
	}

	def private static void drawInAndOutCommunication(CommunicationClientSide commu, String otherApplication,
		WebGLTexture picture, List<PrimitiveObject> polygons) {
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
				polygons.add(pipe)
			}
		]

		labels.add(quad)
		labels.add(label)
	}

	def private static drawCommunications(List<CommunicationAppAccumulator> communicationsAccumulated,
		List<PrimitiveObject> polygons) {
		communicationsAccumulated.forEach [

		Experiment::draw3DTutorialCom(it.source.name, it.target.name, points.get(0),
					points.get(1), centerPoint, polygons)
				
			drawCommunication(points, it.pipeSize, it.averageResponseTime, polygons)
		]
	}

	def private static drawCommunication(List<Vector3f> points, float pipeSize, float averageResponseTime,
		List<PrimitiveObject> polygons, CommunicationAppAccumulator commu) {
		points.forEach [ point, i |
			if (i < points.size - 1) {
				val pipe = createPipe(point, points.get(i + 1), pipeSize, false)

				commu.primitiveObjects.add(pipe)
				polygons.add(pipe)
			}
		]
	}

	def private static createPipe(Vector3f start, Vector3f end, float lineThickness, boolean transparent) {
		val communicationPipe = new Pipe()
		if (transparent) {
			communicationPipe.setTransparent(true)
			communicationPipe.setColor(ColorDefinitions::pipeColorTrans)
		} else {
			communicationPipe.setColor(ColorDefinitions::pipeColor)
		}
		communicationPipe.setLineThickness(lineThickness)
		communicationPipe.begin
		communicationPipe.addPoint(start.sub(centerPoint))
		communicationPipe.addPoint(end.sub(centerPoint))
		communicationPipe.end
		communicationPipe
	}

	def private static void drawOpenedComponent(ComponentClientSide component, List<PrimitiveObject> polygons, int index) {
		val box = component.createBox(centerPoint, component.color)

		val labelCenterPoint = new Vector3f(
			component.centerPoint.x - component.width / 2.0f + ApplicationLayoutInterface::labelInsetSpace / 2.0f +
				ApplicationLayoutInterface::insetSpace / 2f, component.centerPoint.y, component.centerPoint.z).sub(centerPoint)
		val labelExtension = new Vector3f(component.extension.x, component.extension.y / 2f,
			component.extension.z / 2f)
		val label = createLabelOpenPackages(labelCenterPoint, labelExtension, component.name,
			if (index == 0) BLACK else WHITE)

		component.primitiveObjects.add(box)

		polygons.add(box)
		labels.add(label)

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

					//					drawClosedComponents(it, polygons)
					laterDrawComponent.add(it)
				}
			}
		]

		val arrow = Experiment::draw3DTutorial(component.name,
			new Vector3f(component.positionX, component.positionY, component.positionZ), component.width,
			component.height, component.depth, centerPoint, polygons)
		component.primitiveObjects.addAll(arrow)
	}

	def private static void drawClosedComponents(ComponentClientSide component, List<PrimitiveObject> polygons) {
		val box = component.createBox(centerPoint, component.color)
		val label = createLabel(component.centerPoint.sub(centerPoint), component.extension, component.name, WHITE)

		component.primitiveObjects.add(box)

		polygons.add(box)
		labels.add(label)

		val arrow = Experiment::draw3DTutorial(component.name,
			new Vector3f(component.positionX, component.positionY, component.positionZ), component.width,
			component.height, component.depth, centerPoint, polygons)
		component.primitiveObjects.addAll(arrow)
	}

	def private static void drawClazz(ClazzClientSide clazz, List<PrimitiveObject> polygons) {
		val box = clazz.createBox(centerPoint, clazz.color)
		val label = createLabel(
			new Vector3f(clazz.positionX - centerPoint.x + clazz.width / 2f,
				clazz.positionY - centerPoint.y + clazz.height / 2f,
				clazz.positionZ - centerPoint.z + clazz.depth / 2f),
			new Vector3f(clazz.width / 2f, clazz.height / 2f, clazz.depth / 2f),
			clazz.name,
			WHITE
		)

		clazz.primitiveObjects.add(box)

		polygons.add(box)
		labels.add(label)

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

	def private static getCenterPoint(ApplicationClientSide application) {
		val rect = new ArrayList<Float>
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)

		val MIN_X = 0
		val MAX_X = 1
		val MIN_Y = 2
		val MAX_Y = 3
		val MIN_Z = 4
		val MAX_Z = 5

		application.components.forEach [
			getMinMaxFromQuad(it, rect, MIN_X, MAX_X, MAX_Y, MIN_Y, MAX_Z, MIN_Z)
		]

		new Vector3f(rect.get(MIN_X) + ((rect.get(MAX_X) - rect.get(MIN_X)) / 2f),
			rect.get(MIN_Y) + ((rect.get(MAX_Y) - rect.get(MIN_Y)) / 2f),
			rect.get(MIN_Z) + ((rect.get(MAX_Z) - rect.get(MIN_Z)) / 2f))
	}

	def private static getMinMaxFromQuad(Draw3DNodeEntity entity, ArrayList<Float> rect, int MIN_X, int MAX_X, int MAX_Y,
		int MIN_Y, int MAX_Z, int MIN_Z) {
		val curX = entity.positionX
		val curY = entity.positionY
		val curZ = entity.positionZ

		if (curX < rect.get(MIN_X)) {
			rect.set(MIN_X, curX)
		}
		if (rect.get(MAX_X) < curX + (entity.width)) {
			rect.set(MAX_X, curX + (entity.width))
		}

		if (curY > rect.get(MAX_Y)) {
			rect.set(MAX_Y, curY)
		}
		if (rect.get(MIN_Y) > curY - (entity.height)) {
			rect.set(MIN_Y, curY - (entity.height))
		}

		if (curZ < rect.get(MIN_Z)) {
			rect.set(MIN_Z, curZ)
		}
		if (rect.get(MAX_Z) < curZ + (entity.width)) {
			rect.set(MAX_Z, curZ + (entity.width))
		}
	}
}
