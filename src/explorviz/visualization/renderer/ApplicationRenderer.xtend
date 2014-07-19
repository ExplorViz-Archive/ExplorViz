package explorviz.visualization.renderer

import elemental.html.WebGLTexture
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.BoxContainer
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.primitives.PipeContainer
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import java.util.ArrayList
import java.util.List
import explorviz.shared.model.helper.Draw3DNodeEntity

class ApplicationRenderer {
	static var Vector3f viewCenterPoint
	static val List<PrimitiveObject> arrows = new ArrayList<PrimitiveObject>(2)

	static var WebGLTexture incomePicture
	static var WebGLTexture outgoingPicture

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
		BoxContainer::clear()
		LabelContainer::clear()
		arrows.clear()

		application.clearAllPrimitiveObjects

		if (viewCenterPoint == null || firstViewAfterChange) {
			viewCenterPoint = ViewCenterPointerCalculator::calculateAppCenterAndZZoom(application)
		}

		application.incomingCommunications.forEach [
			drawIncomingCommunication(it, polygons)
		]

		application.outgoingCommunications.forEach [
			drawOutgoingCommunication(it, polygons)
		]

		drawOpenedComponent(application.components.get(0), 0)

		drawCommunications(application.communicationsAccumulated)

		PipeContainer::doPipeCreation
		BoxContainer::doBoxCreation
		LabelContainer::doLabelCreation

		polygons.addAll(arrows)
	}

	def private static void drawIncomingCommunication(Communication commu, List<PrimitiveObject> polygons) {
		drawInAndOutCommunication(commu, commu.source.name, incomePicture, polygons)
	}

	def private static void drawOutgoingCommunication(Communication commu, List<PrimitiveObject> polygons) {

		drawInAndOutCommunication(commu, commu.target.name, outgoingPicture, polygons)
	}

	def private static void drawInAndOutCommunication(Communication commu, String otherApplication,
		WebGLTexture picture, List<PrimitiveObject> polygons) {
		val center = new Vector3f(commu.pointsFor3D.get(0)).sub(viewCenterPoint)
		val portsExtension = ApplicationLayoutInterface::externalPortsExtension

		val quad = new Quad(center, portsExtension, picture, null, true, true)
		createHorizontalLabel(center,
			new Vector3f(portsExtension.x * 8f, portsExtension.y + 4f, portsExtension.z * 8f), otherApplication, false,
			false)

		commu.pointsFor3D.forEach [ point, i |
			commu.primitiveObjects.clear
			if (i < commu.pointsFor3D.size - 1) {
				//				PipeContainer::createPipe(commu,viewCenterPoint, commu.lineThickness, point, commu.pointsFor3D.get(i + 1), false) 
				//				commu.primitiveObjects.add(pipe) TODO
			}
		]

		polygons.add(quad)
	}

	def private static drawCommunications(List<CommunicationAppAccumulator> communicationsAccumulated) {
		PipeContainer::clear()
		
		communicationsAccumulated.forEach [
			primitiveObjects.clear()
			var hide = false
			if (traceToHighlight != null) {
				var found = false
				for (aggCommu : it.aggregatedCommunications) {
					if (aggCommu.traceIdToRuntimeMap.get(traceToHighlight) != null) {
						found = true
					}
				}

				hide = !found
			}
			drawTutorialCommunicationIfEnabled(it, points)
			for (var i = 0; i < points.size - 1; i++) {
				PipeContainer::createPipe(it, viewCenterPoint, pipeSize, points.get(i), points.get(i + 1), hide)
			}
		]
	}

	def private static void drawTutorialCommunicationIfEnabled(CommunicationAppAccumulator commu, List<Vector3f> points) {
		arrows.addAll(
			Experiment::draw3DTutorialCom(commu.source.name, commu.target.name, points.get(0), points.get(1),
				viewCenterPoint))
	}

	def private static void drawOpenedComponent(Component component, int index) {
		BoxContainer::createBox(component, viewCenterPoint, true)

		createVerticalLabel(component, index)

		component.clazzes.forEach [
			if (component.opened) {
				drawClazz(it)
			}
		]

		component.children.forEach [
			if (it.opened) {
				drawOpenedComponent(it, index + 1)
			} else {
				if (component.opened) {
					drawClosedComponent(it)
				}
			}
		]

		drawTutorialIfEnabled(component)
	}

	private def static drawTutorialIfEnabled(Draw3DNodeEntity nodeEntity) {
		val arrow = Experiment::draw3DTutorial(nodeEntity.name, nodeEntity.position, nodeEntity.width, nodeEntity.height,
			nodeEntity.depth, viewCenterPoint, nodeEntity instanceof Clazz)
		arrows.addAll(arrow)
	}

	def private static void drawClosedComponent(Component component) {
		BoxContainer::createBox(component, viewCenterPoint, false)
		createHorizontalLabel(component.centerPoint.sub(viewCenterPoint), component.extension, component.name, true,
			false)

		drawTutorialIfEnabled(component)
	}

	def private static void drawClazz(Clazz clazz) {
		BoxContainer::createBox(clazz, viewCenterPoint, false)
		createHorizontalLabel(
			clazz.centerPoint.sub(viewCenterPoint),
			clazz.extension,
			clazz.name,
			true,
			true
		)

		drawTutorialIfEnabled(clazz)
	}

	def private static void createHorizontalLabel(Vector3f center, Vector3f itsExtension, String label, boolean white,
		boolean isClazz) {
		val xExtension = Math.max(Math.max(itsExtension.x / 5f, itsExtension.z / 5f), 0.75f)
		val yValue = center.y + itsExtension.y + 0.02f
		val zExtension = xExtension

		LabelContainer::createLabel(
			label,
			new Vector3f(center.x - xExtension, yValue, center.z),
			new Vector3f(center.x, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z),
			new Vector3f(center.x, yValue, center.z - zExtension),
			false,
			white,
			isClazz
		)
	}

	def private static void createVerticalLabel(Component component, int index) {
		val center = new Vector3f(
			component.centerPoint.x - component.extension.x + ApplicationLayoutInterface::labelInsetSpace / 2f +
				ApplicationLayoutInterface::insetSpace / 2f, component.centerPoint.y, component.centerPoint.z).sub(
			viewCenterPoint)


		val xExtension = ApplicationLayoutInterface::labelInsetSpace / 4f
		val yValue = center.y + component.extension.y+ 0.02f
		val zExtension = component.extension.z

		LabelContainer::createLabel(
			component.name,
			new Vector3f(center.x - xExtension, yValue, center.z - zExtension),
			new Vector3f(center.x - xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z - zExtension),
			true,
			index != 0,
			false
		)
	}

}
