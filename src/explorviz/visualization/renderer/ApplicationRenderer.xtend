package explorviz.visualization.renderer

import elemental.html.WebGLTexture
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.math.Matrix44f
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.engine.primitives.Pipe
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.primitives.LabelContainer

class ApplicationRenderer {
	static var Vector3f centerPoint

	static val List<Component> laterDrawComponent = new ArrayList<Component>(64)
	static val List<Clazz> laterDrawClazz = new ArrayList<Clazz>(64)
	static val List<CommunicationAppAccumulator> laterDrawCommunication = new ArrayList<CommunicationAppAccumulator>(64)

	static val Vector4f WHITE = new Vector4f(1f, 1f, 1f, 1f)
	static val Vector4f BLACK = new Vector4f(0f, 0f, 0f, 1f)

	static var WebGLTexture incomePicture
	static var WebGLTexture outgoingPicture

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
	
	def static init() {
		incomePicture = TextureManager::createTextureFromImagePath("in_colored.png")
		outgoingPicture = TextureManager::createTextureFromImagePath("out.png")
	}

	def static void drawApplication(Application application, List<PrimitiveObject> polygons,
		boolean firstViewAfterChange) {
		LabelContainer::clear()
		application.clearAllPrimitiveObjects

		if (centerPoint == null || firstViewAfterChange) {
			calculateCenterAndZZoom(application)
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
			drawCommunication(points, it.pipeSize, polygons, it, false)
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
		
		LabelContainer::doLabelCreation
	}

	def static calculateCenterAndZZoom(Application application) {
		val foundation = application.components.get(0)

		val rect = new ArrayList<Float>
		rect.add(foundation.positionX)
		rect.add(foundation.positionX + foundation.width)
		rect.add(foundation.positionY)
		rect.add(foundation.positionY + foundation.height)
		rect.add(foundation.positionZ)
		rect.add(foundation.positionZ + foundation.depth)

		val SPACE_IN_PERCENT = 0.02f

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

		var requiredWidth = Math.abs(modelView.mult(westPoint).x - modelView.mult(eastPoint).x)
		requiredWidth += requiredWidth * SPACE_IN_PERCENT
		var requiredHeight = Math.abs(modelView.mult(southPoint).y - modelView.mult(northPoint).y)
		requiredHeight += requiredHeight * SPACE_IN_PERCENT

		val perspective_factor = WebGLStart::viewportWidth / WebGLStart::viewportHeight as float

		val newZ_by_width = requiredWidth * -1f / perspective_factor
		val newZ_by_height = requiredHeight * -1f

		Camera::getVector.z = Math.min(Math.min(newZ_by_width, newZ_by_height), -15f)
	}

	def private static void drawIncomingCommunication(Communication commu, List<PrimitiveObject> polygons) {
		drawInAndOutCommunication(commu, commu.source.name, incomePicture, polygons)
	}

	def private static void drawOutgoingCommunication(Communication commu, List<PrimitiveObject> polygons) {

		drawInAndOutCommunication(commu, commu.target.name, outgoingPicture, polygons)
	}

	def private static void drawInAndOutCommunication(Communication commu, String otherApplication,
		WebGLTexture picture, List<PrimitiveObject> polygons) {
		val center = new Vector3f(commu.pointsFor3D.get(0)).sub(centerPoint)

		val quad = new Quad(center, ApplicationLayoutInterface::externalPortsExtension, picture, null, true, true)

		createLabel(center,
			new Vector3f(ApplicationLayoutInterface::externalPortsExtension.x * 8f,
				ApplicationLayoutInterface::externalPortsExtension.y + 4f,
				ApplicationLayoutInterface::externalPortsExtension.z * 8f), otherApplication, BLACK)

		commu.pointsFor3D.forEach [ point, i |
			commu.primitiveObjects.clear
			if (i < commu.pointsFor3D.size - 1) {
				val pipe = createPipe(point, commu.pointsFor3D.get(i + 1), commu.lineThickness, false)

				//				commu.primitiveObjects.add(pipe) TODO
				pipe.quads.forEach [
					polygons.add(it)
				]
			}
		]

		polygons.add(quad)
	}

	def private static drawCommunications(List<CommunicationAppAccumulator> communicationsAccumulated,
		List<PrimitiveObject> polygons) {
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
				drawCommunication(points, it.pipeSize, polygons, it, hide)
			}
		]
	}

	def private static drawCommunication(List<Vector3f> points, float pipeSize, List<PrimitiveObject> polygons,
		CommunicationAppAccumulator commu, boolean hide) {
		for (var i = 0; i < points.size - 1; i++) {
			val pipe = createPipe(points.get(i), points.get(i + 1), pipeSize, hide)

			commu.primitiveObjects.add(pipe)
			pipe.quads.forEach [
				polygons.add(it)
			]
		}
	}

	def private static createPipe(Vector3f start, Vector3f end, float lineThickness, boolean hide) {
		var Pipe communicationPipe

		if (hide) {
			communicationPipe = new Pipe(true, true, ColorDefinitions::pipeColorTrans)
		} else {
			communicationPipe = new Pipe(false, true, ColorDefinitions::pipeColor)
		}

		communicationPipe.setLineThickness(lineThickness)
		communicationPipe.addPoint(start.sub(centerPoint))
		communicationPipe.addPoint(end.sub(centerPoint))
		communicationPipe
	}

	def private static void drawOpenedComponent(Component component, List<PrimitiveObject> polygons, int index) {
		val box = component.createBox(centerPoint, component.color)

		val labelCenterPoint = new Vector3f(
			component.centerPoint.x - component.extension.x + ApplicationLayoutInterface::labelInsetSpace / 2f +
				ApplicationLayoutInterface::insetSpace / 2f, component.centerPoint.y, component.centerPoint.z).sub(centerPoint)
				
		val labelExtension = new Vector3f(ApplicationLayoutInterface::labelInsetSpace / 4f, component.extension.y,
			component.extension.z)
			
		createLabelOpenPackages(labelCenterPoint, labelExtension, component.name,
			if (index == 0) BLACK else WHITE)

		component.primitiveObjects.add(box)

		box.quads.forEach [
			polygons.add(it)
		]

		component.clazzes.forEach [
			if (component.opened) {
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

	def private static void drawClosedComponents(Component component, List<PrimitiveObject> polygons) {
		val box = component.createBox(centerPoint, component.color)
		
		createLabel(component.centerPoint.sub(centerPoint), component.extension, component.name, WHITE)

		component.primitiveObjects.add(box)

		box.quads.forEach [
			polygons.addAll(it)
		]

		val arrow = Experiment::draw3DTutorial(component.name,
			new Vector3f(component.positionX, component.positionY, component.positionZ), component.width,
			component.height, component.depth, centerPoint, polygons)
		component.primitiveObjects.addAll(arrow)
	}

	def private static void drawClazz(Clazz clazz, List<PrimitiveObject> polygons) {
		val box = clazz.createBox(centerPoint, ColorDefinitions::clazzColor)
		createLabel(
			clazz.centerPoint.sub(centerPoint),
			clazz.extension,
			clazz.name,
			WHITE
		)

		clazz.primitiveObjects.add(box)

		box.quads.forEach [
			polygons.add(it)
		]

		val arrow = Experiment::draw3DTutorial(clazz.name,
			new Vector3f(clazz.positionX, clazz.positionY, clazz.positionZ), clazz.width, clazz.height, clazz.depth,
			centerPoint, polygons)
		clazz.primitiveObjects.addAll(arrow)
	}

	def private static void createLabel(Vector3f center, Vector3f itsExtension, String label, Vector4f color) {
		val yValue = center.y + itsExtension.y

		val xExtension = itsExtension.x
		val zExtension = itsExtension.z

		LabelContainer::createLabel(label,
			new Vector3f(center.x - xExtension, yValue, center.z),
			new Vector3f(center.x, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z),
			new Vector3f(center.x, yValue, center.z - zExtension),
			false
		)
	}

	def private static void createLabelOpenPackages(Vector3f center, Vector3f itsExtension, String label, Vector4f color) {
		val yValue = center.y + itsExtension.y

		val xExtension = itsExtension.x
		val zExtension = itsExtension.z
		
		LabelContainer::createLabel(label,
			new Vector3f(center.x - xExtension, yValue, center.z - zExtension),
			new Vector3f(center.x - xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z - zExtension), true
		)
	}
}
