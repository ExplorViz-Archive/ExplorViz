package explorviz.visualization.renderer

import elemental.html.WebGLTexture
import explorviz.plugin_client.main.Perspective
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.BoxContainer
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.primitives.PipeContainer
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.highlighting.NodeHighlighter
import explorviz.visualization.highlighting.TraceHighlighter
import explorviz.visualization.highlighting.TraceReplayer
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import explorviz.visualization.main.ExplorViz
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.primitives.QuadContainer
import explorviz.visualization.engine.primitives.LineContainer
import explorviz.visualization.engine.math.Vector4f
import explorviz.plugin_client.attributes.IPluginKeys

class ApplicationRenderer {
	public static var Vector3f viewCenterPoint
	static val List<PrimitiveObject> specialSymbols = new ArrayList<PrimitiveObject>(2)

	static var WebGLTexture incomePicture
	static var WebGLTexture outgoingPicture
	static var WebGLTexture warningSignTexture
	static var WebGLTexture errorSignTexture

	def static init() {
		TextureManager::deleteTextureIfExisting(incomePicture)
		TextureManager::deleteTextureIfExisting(outgoingPicture)
		
		incomePicture = TextureManager::createTextureFromImagePath("in_colored.png")
		outgoingPicture = TextureManager::createTextureFromImagePath("out.png")
		warningSignTexture = TextureManager::createTextureFromImagePath("logos/warning.png")
		errorSignTexture = TextureManager::createTextureFromImagePath("logos/error.png")
	}

	def static void drawApplication(Application application, List<PrimitiveObject> polygons,
		boolean firstViewAfterChange) {
		BoxContainer::clear()
		LabelContainer::clear()
		QuadContainer::clear()
		LineContainer::clear()
		PipeContainer::clear()
		specialSymbols.clear()

		application.clearAllPrimitiveObjects

		if (viewCenterPoint == null || firstViewAfterChange) {
			viewCenterPoint = ViewCenterPointerCalculator::calculateAppCenterAndZZoom(application)
		}

		TraceHighlighter::applyHighlighting(application)
		NodeHighlighter::applyHighlighting(application)

		//		application.incomingCommunications.forEach [
		//			drawIncomingCommunication(it, polygons)
		//		]
		//
		//		application.outgoingCommunications.forEach [
		//			drawOutgoingCommunication(it, polygons)
		//		]
		drawOpenedComponent(application.components.get(0), 0)

		drawCommunications(application.communicationsAccumulated)

		BoxContainer::doBoxCreation
		LabelContainer::doLabelCreation

		polygons.addAll(specialSymbols)
	}

	//	def private static void drawIncomingCommunication(Communication commu, List<PrimitiveObject> polygons) {
	//		drawInAndOutCommunication(commu, commu.source.name, incomePicture, polygons)
	//	}
	//
	//	def private static void drawOutgoingCommunication(Communication commu, List<PrimitiveObject> polygons) {
	//
	//		drawInAndOutCommunication(commu, commu.target.name, outgoingPicture, polygons)
	//	}
	//
	//	def private static void drawInAndOutCommunication(Communication commu, String otherApplication,
	//		WebGLTexture picture, List<PrimitiveObject> polygons) {
	//		val center = new Vector3f(commu.pointsFor3D.get(0)).sub(viewCenterPoint)
	//		val portsExtension = ApplicationLayoutInterface::externalPortsExtension
	//
	//		val quad = new Quad(center, portsExtension, picture, null, true, true)
	//		createHorizontalLabel(center,
	//			new Vector3f(portsExtension.x * 8f, portsExtension.y + 4f, portsExtension.z * 8f), otherApplication, false,
	//			false, false)
	//
	//		commu.pointsFor3D.forEach [ point, i |
	//			commu.primitiveObjects.clear
	//			if (i < commu.pointsFor3D.size - 1) {
	//				//				PipeContainer::createPipe(commu,viewCenterPoint, commu.lineThickness, point, commu.pointsFor3D.get(i + 1), false) 
	//				//				commu.primitiveObjects.add(pipe) TODO
	//			}
	//		]
	//
	//		polygons.add(quad)
	//	}
	def private static void drawCommunications(List<CommunicationAppAccumulator> communicationsAccumulated) {
		PipeContainer::clear()

		communicationsAccumulated.forEach [
			if (it.source != it.target) { // dont try to draw self edges
				primitiveObjects.clear()

				if (it.state == EdgeState.REPLAY_HIGHLIGHT) {
					val distance = points.get(1).sub(points.get(0))
					val center = points.get(0).add(distance.div(2f)).add(new Vector3f(0f, 1f, 0f))
					createHorizontalLabel(center.sub(viewCenterPoint),
						new Vector3f(Math.min(Math.abs(distance.x) + Math.abs(distance.z), 7.5f), 0f, 0f),
						TraceReplayer::currentlyHighlightedCommu.methodName + "(..)", true, false, true)
				}

				drawTutorialCommunicationIfEnabled(it, points)
				if (it.points.size >= 2) {
					PipeContainer::createPipe(it, viewCenterPoint, pipeSize)
				}
			}
		]
		PipeContainer::doPipeCreation
	}

	def private static void drawTutorialCommunicationIfEnabled(CommunicationAppAccumulator commu, List<Vector3f> points) {
		specialSymbols.addAll(
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

		drawTutorialIfEnabled(component, component.position)
	}

	private def static void drawTutorialIfEnabled(Draw3DNodeEntity nodeEntity, Vector3f position) {
		val arrow = Experiment::draw3DTutorial(nodeEntity.name, position, nodeEntity.width, nodeEntity.height,
			nodeEntity.depth, viewCenterPoint, nodeEntity instanceof Clazz)
		specialSymbols.addAll(arrow)
	}

	def private static void drawClosedComponent(Component component) {
		BoxContainer::createBox(component, viewCenterPoint, false)
		createHorizontalLabel(component.centerPoint.sub(viewCenterPoint), component.extension, component.name, true,
			false, false)

		createWarningOrErrorSign(component)

		drawTutorialIfEnabled(component,
			new Vector3f(component.positionX + 2, component.positionY + 2, component.positionZ))
	}

	private def static createWarningOrErrorSign(Draw3DNodeEntity node) {
		var WebGLTexture symbol = null
		var Vector4f rgbColor = null

		if (ExplorViz::currentPerspective == Perspective::SYMPTOMS) {

			if (node.isGenericDataPresent(IPluginKeys::WARNING_ANOMALY) &&
				node.getGenericBooleanData(IPluginKeys::WARNING_ANOMALY)) {
				symbol = warningSignTexture
			} else if (node.isGenericDataPresent(IPluginKeys::ERROR_ANOMALY) &&
				node.getGenericBooleanData(IPluginKeys::ERROR_ANOMALY)) {
				symbol = errorSignTexture
			}
		} else if (ExplorViz::currentPerspective == Perspective::DIAGNOSIS) {
			if (node.isGenericDataPresent(IPluginKeys::ROOTCAUSE_RGB_INDICATOR)) {
				val rgbValue = node.getGenericStringData(IPluginKeys::ROOTCAUSE_RGB_INDICATOR)
				val splitVal = rgbValue.split(",")
				rgbColor = new Vector4f(Integer.parseInt(splitVal.get(0)) / 255f,
					Integer.parseInt(splitVal.get(1)) / 255f, Integer.parseInt(splitVal.get(2)) / 255f, 1f)
			}
		}

		if (symbol != null || rgbColor != null) {
			val xExtension = Math.max(Math.max(node.extension.x / 5f, node.extension.z / 5f), 0.75f)

			val warningSignWidth = xExtension + 0.1f
			val center = node.centerPoint.sub(viewCenterPoint)
			val yValue = center.y + node.extension.y + 0.02f

			val signCenter = new Vector3f(center.x + xExtension + warningSignWidth + warningSignWidth / 2f,
				center.y + node.extension.y + 0.02f, center.z + warningSignWidth / 2f)
			var Quad sign = null
			val BOTTOM_LEFT = new Vector3f(signCenter.x - warningSignWidth, yValue, signCenter.z)
			val BOTTOM_RIGHT = new Vector3f(signCenter.x, yValue, signCenter.z + warningSignWidth)
			val TOP_RIGHT = new Vector3f(signCenter.x + warningSignWidth, yValue, signCenter.z)
			val TOP_LEFT = new Vector3f(signCenter.x, yValue, signCenter.z - warningSignWidth)

			if (rgbColor == null) {
				sign = new Quad(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, symbol, true, false)
			} else {
				sign = new Quad(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, rgbColor, true, false)
			}

			specialSymbols.add(sign)
		}
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

		createWarningOrErrorSign(clazz)

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
			true,
			false
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
			true,
			false
		)
	}

}
