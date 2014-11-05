package explorviz.visualization.renderer

import elemental.html.WebGLTexture
import explorviz.plugin.attributes.IPluginKeys
import explorviz.plugin.main.Perspective
import explorviz.shared.model.Application
import explorviz.shared.model.Communication
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.System
import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.BoxContainer
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.primitives.PipeContainer
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.main.ExplorViz
import java.util.ArrayList
import java.util.List
import explorviz.plugin.capacitymanagement.CapManExecutionStates

class LandscapeRenderer {
	static var Vector3f viewCenterPoint = null
	static val DEFAULT_Z_LAYER_DRAWING = 0f

	static val List<PrimitiveObject> arrows = new ArrayList<PrimitiveObject>()

	static var WebGLTexture warningSignTexture
	static var WebGLTexture errorSignTexture

	def static init() {
		warningSignTexture = TextureManager::createTextureFromImagePath("logos/warning.png", 8, 8, 112, 112, 128, 128)
		errorSignTexture = TextureManager::createTextureFromImagePath("logos/error.png", 8, 8, 112, 112, 128, 128)
	}

	def static void drawLandscape(Landscape landscape, List<PrimitiveObject> polygons, boolean firstViewAfterChange) {
		calcViewCenterPoint(landscape, firstViewAfterChange)

		arrows.clear()
		PipeContainer::clear()
		BoxContainer::clear()
		LabelContainer::clear()

		landscape.systems.forEach [
			clearDrawingEntities(it)
			createSystemDrawing(it, DEFAULT_Z_LAYER_DRAWING, polygons)
		]

		landscape.applicationCommunication.forEach [
			it.primitiveObjects.clear()
			Communication::createCommunicationLine(DEFAULT_Z_LAYER_DRAWING, it, viewCenterPoint, polygons)
		]

		polygons.addAll(arrows)
	}

	public def static void calcViewCenterPoint(Landscape landscape, boolean firstViewAfterChange) {
		if (viewCenterPoint == null || firstViewAfterChange) {
			viewCenterPoint = ViewCenterPointerCalculator::calculateLandscapeCenterAndZZoom(landscape)
		}
	}

	def private static void clearDrawingEntities(System system) {
		system.primitiveObjects.clear()

		system.nodeGroups.forEach [
			it.primitiveObjects.clear()
			it.nodes.forEach [
				it.primitiveObjects.clear()
				it.applications.forEach [
					it.primitiveObjects.clear()
				]
			]
		]
	}

	def private static createSystemDrawing(System system, float z, List<PrimitiveObject> polygons) {
		if (system.nodeGroups.size() > 1) {
			val systemQuad = system.createSystemQuad(z - 0.2f, viewCenterPoint)

			val systemOpenSymbol = system.createSystemOpenSymbol()
			val systemLabel = system.createSystemLabel(systemQuad, system.name)

			system.primitiveObjects.add(systemQuad)
			system.primitiveObjects.add(systemOpenSymbol)
			system.primitiveObjects.add(systemLabel)

			polygons.add(systemQuad)
			polygons.add(systemOpenSymbol)
			polygons.add(systemLabel)
		}

		if (system.opened) {
			system.nodeGroups.forEach [
				createNodeGroupDrawing(it, z, polygons)
			]
		}

		drawTutorialIfEnabled(system, new Vector3f(system.positionX, system.positionY, z))
	}

	private def static void drawTutorialIfEnabled(DrawNodeEntity nodeEntity, Vector3f pos) {
		arrows.addAll(
			Experiment::drawTutorial(nodeEntity.name, pos, nodeEntity.width, nodeEntity.height, viewCenterPoint))
	}

	def private static createNodeGroupDrawing(NodeGroup nodeGroup, float z, List<PrimitiveObject> polygons) {
		if (nodeGroup.nodes.size() > 1) {
			val nodeGroupQuad = nodeGroup.createNodeGroupQuad(z, viewCenterPoint)
			val nodeGroupOpenSymbol = nodeGroup.createNodeGroupOpenSymbol()

			nodeGroup.primitiveObjects.add(nodeGroupQuad)
			nodeGroup.primitiveObjects.add(nodeGroupOpenSymbol)

			polygons.add(nodeGroupQuad)
			polygons.add(nodeGroupOpenSymbol)
		}

		nodeGroup.nodes.forEach [
			createNodeDrawing(it, z, polygons)
		]

		drawTutorialIfEnabled(nodeGroup, new Vector3f(nodeGroup.positionX, nodeGroup.positionY + 0.05f, z))
	}

	def private static createNodeDrawing(Node node, float z, List<PrimitiveObject> polygons) {
		if (node.visible) {
			val nodeQuad = node.createNodeQuad(z + 0.01f, viewCenterPoint)
			val labelName = if (node.parent.opened) {
					if (node.ipAddress != null && !node.ipAddress.empty && !node.ipAddress.startsWith("<")) {
						node.ipAddress
					} else {
						node.name
					}
				} else {
					node.parent.name
				}
			val nodeLabel = node.createNodeLabel(nodeQuad, labelName)

			node.primitiveObjects.add(nodeQuad)
			node.primitiveObjects.add(nodeLabel)

			polygons.add(nodeQuad)
			polygons.add(nodeLabel)

			if (ExplorViz::currentPerspective == Perspective::EXECUTION) {
				if (node.parent.opened || node.parent.nodes.size == 1) {
					if (node.isGenericDataPresent(IPluginKeys::CAPMAN_EXECUTION_STATE)) {
						val state = node.getGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE) as CapManExecutionStates
						if (state != CapManExecutionStates::NONE) {
							nodeQuad.blinking = true
							nodeLabel.blinking = true
						}
					}
				}
			}

			node.applications.forEach [
				createApplicationDrawing(it, z, polygons)
			]

			drawTutorialIfEnabled(node, new Vector3f(node.positionX, node.positionY, z))
		}
	}

	def private static createApplicationDrawing(Application application, float z, List<PrimitiveObject> polygons) {
		val applicationQuad = application.createApplicationQuad(application.name, z + 0.04f, viewCenterPoint)
		application.primitiveObjects.add(applicationQuad)
		polygons.add(applicationQuad)

		var WebGLTexture symbol = null

		if (ExplorViz::currentPerspective == Perspective::SYMPTOMS) {

			if (application.isGenericDataPresent(IPluginKeys::WARNING_ANOMALY) &&
				application.getGenericBooleanData(IPluginKeys::WARNING_ANOMALY)) {
				symbol = warningSignTexture
			} else if (application.isGenericDataPresent(IPluginKeys::ERROR_ANOMALY) &&
				application.getGenericBooleanData(IPluginKeys::ERROR_ANOMALY)) {
				symbol = errorSignTexture
			}
		} else if (ExplorViz::currentPerspective == Perspective::DIAGNOSIS) {

			if (application.isGenericDataPresent(IPluginKeys::WARNING_ROOTCAUSE) &&
				application.getGenericBooleanData(IPluginKeys::WARNING_ROOTCAUSE)) {
				symbol = warningSignTexture
			} else if (application.isGenericDataPresent(IPluginKeys::ERROR_ROOTCAUSE) &&
				application.getGenericBooleanData(IPluginKeys::ERROR_ROOTCAUSE)) {
				symbol = errorSignTexture
			}
		} else if (ExplorViz::currentPerspective == Perspective::EXECUTION) {
			if (application.parent.parent.opened || application.parent.parent.nodes.size == 1) {
				if (application.isGenericDataPresent(IPluginKeys::CAPMAN_EXECUTION_STATE)) {
					val state = application.getGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE) as CapManExecutionStates
					if (state != CapManExecutionStates::NONE) {
						applicationQuad.blinking = true
					}
				}
			}
		}

		if (symbol != null) {
			val signWidth = application.height / 6f

			val appCenterX = application.positionX + application.width / 2f - viewCenterPoint.x
			val appCenterY = application.positionY - application.height / 2f - viewCenterPoint.y

			val warningSign = new Quad(
				new Vector3f(appCenterX + application.width / 2f - signWidth,
					appCenterY + application.height / 2f - signWidth, z + 0.1f),
				new Vector3f(signWidth, signWidth, 0.0f), symbol, null, true, false)

			application.primitiveObjects.add(warningSign)
			polygons.add(warningSign)
		}

		drawTutorialIfEnabled(application,
			new Vector3f(
				application.positionX,
				application.positionY - 0.05f,
				z
			))
	}
}
