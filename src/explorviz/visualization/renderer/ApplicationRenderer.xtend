package explorviz.visualization.renderer

import elemental.html.WebGLTexture
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.BoxContainer
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.primitives.LineContainer
import explorviz.visualization.engine.primitives.Pipe
import explorviz.visualization.engine.primitives.PipeContainer
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.primitives.QuadContainer
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.highlighting.NodeHighlighter
import explorviz.visualization.highlighting.TraceHighlighter
import explorviz.visualization.highlighting.TraceReplayer
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import java.util.ArrayList
import java.util.List
import explorviz.visualization.performanceanalysis.PerformanceAnalysis
import explorviz.visualization.engine.Logging

class ApplicationRenderer {
	public static var Vector3f viewCenterPoint
	static val List<PrimitiveObject> arrows = new ArrayList<PrimitiveObject>(2)

	static var WebGLTexture incomePicture
	static var WebGLTexture outgoingPicture

	def static init() {
		TextureManager::deleteTextureIfExisting(incomePicture)
		TextureManager::deleteTextureIfExisting(outgoingPicture)

		incomePicture = TextureManager::createTextureFromImagePath("in_colored.png")
		outgoingPicture = TextureManager::createTextureFromImagePath("out.png")
	}

	def static void drawApplication(Application application, List<PrimitiveObject> polygons,
		boolean firstViewAfterChange) {
		BoxContainer::clear()
		LabelContainer::clear()
		QuadContainer::clear()
		LineContainer::clear()
		PipeContainer::clear()
		arrows.clear()

		application.clearAllPrimitiveObjects

		if (viewCenterPoint == null || firstViewAfterChange) {
			viewCenterPoint = ViewCenterPointerCalculator::calculateAppCenterAndZZoom(application)
		}

		TraceHighlighter::applyHighlighting(application)
		NodeHighlighter::applyHighlighting(application)

		for (commu : application.incomingCommunications)
			drawIncomingCommunication(commu, polygons)

		for (commu : application.outgoingCommunications)
			drawOutgoingCommunication(commu, polygons)

		drawOpenedComponent(application.components.get(0), 0)

		drawCommunications(application.communicationsAccumulated)

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
			false, false)

		val pipe = new Pipe(false, true, ColorDefinitions::pipeColor)
		for (point : commu.pointsFor3D) {

			//			if (i < commu.pointsFor3D.size - 1) {
			//					PipeContainer::createPipe(commu,viewCenterPoint, commu.lineThickness, point, commu.pointsFor3D.get(i + 1), false) 
			//				commu.primitiveObjects.add(pipe) TODO
			pipe.addPoint(point.sub(viewCenterPoint))

		//			}
		}
		polygons.add(pipe)

		polygons.add(quad)
	}

	def private static void drawCommunications(List<CommunicationAppAccumulator> communicationsAccumulated) {
		PipeContainer::clear()

		for (commu : communicationsAccumulated) {
			if (commu.source != commu.target) { // dont try to draw self edges
				commu.primitiveObjects.clear()

				if (commu.state == EdgeState.REPLAY_HIGHLIGHT) {
					val distance = commu.points.get(1).sub(commu.points.get(0))
					val center = commu.points.get(0).add(distance.div(2f)).add(new Vector3f(0f, 1f, 0f))
					createHorizontalLabel(center.sub(viewCenterPoint),
						new Vector3f(Math.min(Math.abs(distance.x) + Math.abs(distance.z), 7.5f), 0f, 0f),
						TraceReplayer::currentlyHighlightedCommu.methodName + "(..)", true, false, true)
				}

				if (PerformanceAnalysis::performanceAnalysisMode) {
					if (commu.state == EdgeState.SHOW_DIRECTION_IN_AND_OUT || commu.state == EdgeState.SHOW_DIRECTION_IN || commu.state == EdgeState.SHOW_DIRECTION_OUT) {
						val distance = commu.points.get(1).sub(commu.points.get(0))
						val center = commu.points.get(0).add(distance.div(2f)).add(new Vector3f(0f, 1f, 0f))
						
						var time = 0d
						var requests = 0
						
						for (aggCommu : commu.aggregatedCommunications) {
							for (entry : aggCommu.traceIdToRuntimeMap.entrySet) {
								time = time + entry.value.averageResponseTimeInNanoSec
								requests = requests + entry.value.requests * entry.value.calledTimes
							}
						}
						
						val name = requests + " x " + (time / (1000f * 1000f))  + " ms"
						
						createHorizontalLabel(center.sub(viewCenterPoint),
							new Vector3f(Math.min(Math.abs(distance.x) + Math.abs(distance.z), 7.5f), 0f, 0f),
							name, true, false, true)
					}
				}

				drawTutorialCommunicationIfEnabled(commu, commu.points)
				if (commu.points.size >= 2) {
					PipeContainer::createPipe(commu, viewCenterPoint, commu.pipeSize)
				}
			}
		}
		PipeContainer::doPipeCreation
	}

	def private static void drawTutorialCommunicationIfEnabled(CommunicationAppAccumulator commu, List<Vector3f> points) {
		arrows.addAll(
			Experiment::draw3DTutorialCom(commu.source.name, commu.target.name, points.get(0), points.get(1),
				viewCenterPoint))
	}

	def private static void drawOpenedComponent(Component component, int index) {
		BoxContainer::createBox(component, viewCenterPoint, true)

		createVerticalLabel(component, index)

		for (clazz : component.clazzes)
			if (component.opened)
				drawClazz(clazz)

		for (child : component.children)
			if (child.opened) {
				drawOpenedComponent(child, index + 1)
			} else {
				if (component.opened) {
					drawClosedComponent(child)
				}
			}

		drawTutorialIfEnabled(component, component.position)
	}

	private def static void drawTutorialIfEnabled(Draw3DNodeEntity nodeEntity, Vector3f position) {
		val arrow = Experiment::draw3DTutorial(nodeEntity.name, position, nodeEntity.width, nodeEntity.height,
			nodeEntity.depth, viewCenterPoint, nodeEntity instanceof Clazz)
		arrows.addAll(arrow)
	}

	def private static void drawClosedComponent(Component component) {
		BoxContainer::createBox(component, viewCenterPoint, false)
		createHorizontalLabel(component.centerPoint.sub(viewCenterPoint), component.extension, component.name, true,
			false, false)

		drawTutorialIfEnabled(component,
			new Vector3f(component.positionX + 2, component.positionY + 2, component.positionZ))
	}

	def private static void drawClazz(Clazz clazz) {
		BoxContainer::createBox(clazz, viewCenterPoint, false)

		var highlight = false
		val highlightedCommu = TraceReplayer::currentlyHighlightedCommu
		if (highlightedCommu != null) {
			if (highlightedCommu.source.fullQualifiedName == clazz.fullQualifiedName ||
				highlightedCommu.target.fullQualifiedName == clazz.fullQualifiedName) {
				highlight = true
			}
		}

		createHorizontalLabel(
			clazz.centerPoint.sub(viewCenterPoint),
			clazz.extension,
			clazz.name,
			true,
			true,
			highlight
		)

		drawTutorialIfEnabled(clazz, clazz.position)
	}

	def private static void createHorizontalLabel(Vector3f center, Vector3f itsExtension, String label, boolean white,
		boolean isClazz, boolean highlight) {
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
			isClazz,
			highlight,
			true
		)
	}

	def private static void createVerticalLabel(Component component, int index) {
		val center = new Vector3f(
			component.centerPoint.x - component.extension.x + ApplicationLayoutInterface::labelInsetSpace / 2f +
				ApplicationLayoutInterface::insetSpace / 2f, component.centerPoint.y, component.centerPoint.z).sub(
			viewCenterPoint)

		val xExtension = ApplicationLayoutInterface::labelInsetSpace / 4f
		val yValue = center.y + component.extension.y + 0.02f
		val zExtension = component.extension.z

		LabelContainer::createLabel(
			component.name,
			new Vector3f(center.x - xExtension, yValue, center.z - zExtension),
			new Vector3f(center.x - xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z - zExtension),
			true,
			index != 0,
			false,
			false,
			true
		)
	}

}
