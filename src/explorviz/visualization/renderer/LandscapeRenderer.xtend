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
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.primitives.BoxContainer
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.primitives.LineContainer
import explorviz.visualization.engine.primitives.PipeContainer
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.primitives.QuadContainer
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.main.ExplorViz
import java.util.ArrayList
import java.util.List
import explorviz.shared.model.helper.ELanguage
import explorviz.shared.model.helper.CommunicationAccumulator
import explorviz.shared.model.helper.CommunicationTileAccumulator
import explorviz.shared.model.helper.Point
import explorviz.visualization.main.MathHelpers
import explorviz.plugin.capacitymanagement.CapManExecutionStates

class LandscapeRenderer {
	static var Vector3f viewCenterPoint = null
	static val DEFAULT_Z_LAYER_DRAWING = 0f

	public static val SYSTEM_LABEL_HEIGHT = 0.5f

	public static val NODE_LABEL_HEIGHT = 0.25f

	public static val APPLICATION_PIC_SIZE = 0.16f
	public static val APPLICATION_PIC_PADDING_SIZE = 0.15f
	public static val APPLICATION_LABEL_HEIGHT = 0.25f

	static val List<PrimitiveObject> arrows = new ArrayList<PrimitiveObject>(2)

	static var WebGLTexture javaPicture
	static var WebGLTexture cppPicture
	static var WebGLTexture perlPicture
	static var WebGLTexture databasePicture
	static var WebGLTexture warningSignTexture
	static var WebGLTexture errorSignTexture

	def static init() {
		TextureManager::deleteTextureIfExisting(javaPicture)
		TextureManager::deleteTextureIfExisting(cppPicture)
		TextureManager::deleteTextureIfExisting(perlPicture)
		TextureManager::deleteTextureIfExisting(databasePicture)

		javaPicture = TextureManager::createTextureFromImagePath("logos/java12.png")
		cppPicture = TextureManager::createTextureFromImagePath("logos/java12.png")
		perlPicture = TextureManager::createTextureFromImagePath("logos/java12.png")
		databasePicture = TextureManager::createTextureFromImagePath("logos/database2.png")
		warningSignTexture = TextureManager::createTextureFromImagePath("logos/warning.png")
		errorSignTexture = TextureManager::createTextureFromImagePath("logos/error.png")
	}

	def static void drawLandscape(Landscape landscape, List<PrimitiveObject> polygons, boolean firstViewAfterChange) {
		calcViewCenterPoint(landscape, firstViewAfterChange)

		arrows.clear()
		BoxContainer::clear()
		LabelContainer::clear()
		QuadContainer::clear()
		LineContainer::clear()
		PipeContainer::clear()

		landscape.systems.forEach [
			clearDrawingEntities(it)
			createSystemDrawing(it, DEFAULT_Z_LAYER_DRAWING, polygons)
		]

		landscape.communicationsAccumulated.clear()

		landscape.applicationCommunication.forEach [
			createCommunicationAccumlated(DEFAULT_Z_LAYER_DRAWING, it, landscape.communicationsAccumulated)
		]

		createCommunicationLineDrawing(landscape.communicationsAccumulated)

		QuadContainer::doQuadCreation()
		LabelContainer::doLabelCreation()
		LineContainer::doLineCreation()

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
			system.positionZ = z - 0.2f
			QuadContainer::createQuad(system, viewCenterPoint, null, System::backgroundColor, false)

			createOpenSymbol(system, System::plusColor, System::backgroundColor)
			createSystemLabel(system, system.name)
		}

		if (system.opened) {
			system.nodeGroups.forEach [
				createNodeGroupDrawing(it, z, polygons)
			]
		}

		drawTutorialIfEnabled(system, new Vector3f(system.positionX, system.positionY, z))
	}

	def private static void createOpenSymbol(DrawNodeEntity entity, Vector4f plusColor, Vector4f backgroundColor) {
		val extensionX = 0.2f
		val extensionY = 0.2f

		val TOP_RIGHT = new Vector3f(entity.positionX + entity.width, entity.positionY, entity.positionZ)

		var float centerX = TOP_RIGHT.x - extensionX
		var float centerY = TOP_RIGHT.y - extensionY

		var symbol = "-" // -
		if (entity instanceof System) {
			if (!entity.opened) symbol = "+"
		} else if (entity instanceof NodeGroup) {
			if (!entity.opened) symbol = "+"
		}

		val zValue = entity.positionZ + 0.01f
		LabelContainer::createLabel(symbol,
			new Vector3f(centerX - extensionX, centerY - extensionY, zValue).sub(viewCenterPoint),
			new Vector3f(centerX + extensionX, centerY - extensionY, zValue).sub(viewCenterPoint),
			new Vector3f(centerX + extensionX, centerY + extensionY, zValue).sub(viewCenterPoint),
			new Vector3f(centerX - extensionX, centerY + extensionY, zValue).sub(viewCenterPoint), false, false, false,
			false, false)
	}

	private def static void createSystemLabel(System system, String name) {
		val Vector3f ORIG_TOP_LEFT = new Vector3f(system.positionX, system.positionY, 0f).sub(viewCenterPoint)
		val Vector3f ORIG_TOP_RIGHT = new Vector3f(system.positionX + system.width, system.positionY, 0f).sub(
			viewCenterPoint)

		val labelWidth = 2.5f

		val labelOffsetTop = 0.3f

		val absolutLabelLeftStart = ORIG_TOP_LEFT.x + ((ORIG_TOP_RIGHT.x - ORIG_TOP_LEFT.x) / 2f) - (labelWidth / 2f)

		val BOTTOM_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_TOP_LEFT.y - labelOffsetTop - SYSTEM_LABEL_HEIGHT,
			0.05f)
		val BOTTOM_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth,
			ORIG_TOP_RIGHT.y - labelOffsetTop - SYSTEM_LABEL_HEIGHT, 0.05f)
		val TOP_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth, ORIG_TOP_RIGHT.y - labelOffsetTop, 0.05f)
		val TOP_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_TOP_LEFT.y - labelOffsetTop, 0.05f)

		LabelContainer::createLabel(name, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, false, false, false, false,
			false)
	}

	private def static void drawTutorialIfEnabled(DrawNodeEntity nodeEntity, Vector3f pos) {
		arrows.addAll(
			Experiment::drawTutorial(nodeEntity.name, pos, nodeEntity.width, nodeEntity.height, viewCenterPoint))
	}

	def private static createNodeGroupDrawing(NodeGroup nodeGroup, float z, List<PrimitiveObject> polygons) {
		nodeGroup.positionZ = z

		if (nodeGroup.nodes.size() > 1) {
			QuadContainer::createQuad(nodeGroup, viewCenterPoint, null, NodeGroup::backgroundColor, false)
			createOpenSymbol(nodeGroup, NodeGroup::plusColor, NodeGroup::backgroundColor)
		}

		nodeGroup.nodes.forEach [
			createNodeDrawing(it, z, polygons)
		]

		drawTutorialIfEnabled(nodeGroup, new Vector3f(nodeGroup.positionX, nodeGroup.positionY + 0.05f, z))
	}

	def private static createNodeDrawing(Node node, float z, List<PrimitiveObject> polygons) {
		if (node.visible) {
			node.positionZ = z + 0.01f
			QuadContainer::createQuad(node, viewCenterPoint, null, ColorDefinitions::nodeBackgroundColor, false)

			createNodeLabel(node, node.displayName)

			if (ExplorViz::currentPerspective == Perspective::EXECUTION) {
				if (node.parent.opened || node.parent.nodes.size == 1) {
					if (node.isGenericDataPresent(IPluginKeys::CAPMAN_EXECUTION_STATE)) {
						val state = node.getGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE) as CapManExecutionStates
						if (state != CapManExecutionStates::NONE) {
							//nodeQuad.blinking = true
							//nodeLabel.blinking = true
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

	def private static void createNodeLabel(Node node, String labelName) {
		val ORIG_BOTTOM_LEFT = new Vector3f(node.positionX, node.positionY - node.height, 0f).sub(viewCenterPoint)
		val ORIG_BOTTOM_RIGHT = new Vector3f(node.positionX + node.width, node.positionY - node.height, 0f).sub(
			viewCenterPoint)

		val labelWidth = 2.0f
		val labelHeight = NODE_LABEL_HEIGHT

		val labelOffsetBottom = 0.2f

		val absolutLabelLeftStart = ORIG_BOTTOM_LEFT.x + ((ORIG_BOTTOM_RIGHT.x - ORIG_BOTTOM_LEFT.x) / 2f) -
			(labelWidth / 2f)

		val BOTTOM_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_BOTTOM_LEFT.y + labelOffsetBottom, 0.05f)
		val BOTTOM_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth, ORIG_BOTTOM_RIGHT.y + labelOffsetBottom,
			0.05f)
		val TOP_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth,
			ORIG_BOTTOM_RIGHT.y + labelOffsetBottom + labelHeight, 0.05f)
		val TOP_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_BOTTOM_LEFT.y + labelOffsetBottom + labelHeight, 0.05f)

		LabelContainer::createLabel(labelName, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, false, true, false, false,
			false)
	}

	def private static createApplicationDrawing(Application application, float z, List<PrimitiveObject> polygons) {
		application.positionZ = z + 0.1f
		QuadContainer::createQuad(application, viewCenterPoint, null, null, true)
		createApplicationLabel(application, application.name)

		val logoTexture = if (application.database)
				databasePicture
			else if (application.programmingLanguage == ELanguage::JAVA) {
				javaPicture
			} else if (application.programmingLanguage == ELanguage::CPP) {
				cppPicture
			} else if (application.programmingLanguage == ELanguage::PERL) {
				perlPicture
			}

		val logo = new Quad(
			new Vector3f(
				application.positionX + application.width - APPLICATION_PIC_SIZE / 2f - APPLICATION_PIC_PADDING_SIZE -
					viewCenterPoint.x,
				application.positionY - application.height / 2f - viewCenterPoint.y + APPLICATION_LABEL_HEIGHT / 8f,
				application.positionZ + 0.01f - viewCenterPoint.z),
			new Vector3f(APPLICATION_PIC_SIZE, APPLICATION_PIC_SIZE, 0f), logoTexture, null, true, true)

		polygons.add(logo)

		drawTutorialIfEnabled(application, new Vector3f(application.positionX, application.positionY - 0.05f, z))
	}

	def private static void createApplicationLabel(Application app, String labelName) {
		val ORIG_BOTTOM_LEFT = new Vector3f(app.positionX, app.positionY - app.height, 0f).sub(viewCenterPoint)
		val ORIG_TOP_RIGHT = new Vector3f(app.positionX + app.width, app.positionY, 0f).sub(viewCenterPoint)

		val labelWidth = 2.0f

		val X_LEFT = ORIG_BOTTOM_LEFT.x +
			(((ORIG_TOP_RIGHT.x - ORIG_BOTTOM_LEFT.x) - APPLICATION_PIC_PADDING_SIZE - APPLICATION_PIC_SIZE) / 2f) -
			(labelWidth / 2f)
		val Y_BOTTOM = ORIG_BOTTOM_LEFT.y + ((ORIG_TOP_RIGHT.y - ORIG_BOTTOM_LEFT.y) / 2f) -
			(APPLICATION_LABEL_HEIGHT / 2f)

		val BOTTOM_LEFT = new Vector3f(X_LEFT, Y_BOTTOM, 0.05f)
		val BOTTOM_RIGHT = new Vector3f(X_LEFT + labelWidth, Y_BOTTOM, 0.05f)
		val TOP_RIGHT = new Vector3f(X_LEFT + labelWidth, Y_BOTTOM + APPLICATION_LABEL_HEIGHT, 0.05f)
		val TOP_LEFT = new Vector3f(X_LEFT, Y_BOTTOM + APPLICATION_LABEL_HEIGHT, 0.05f)

		LabelContainer::createLabel(labelName, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, false, true, false, false,
			false)
	}

	def static void createCommunicationAccumlated(float z, Communication commu,
		List<CommunicationAccumulator> communicationAccumulated) {
		val lineZvalue = z + 0.02f

		if (!commu.points.empty) {
			val accum = new CommunicationAccumulator()
			communicationAccumulated.add(accum)

			for (var i = 1; i < commu.points.size; i++) {
				val lastPoint = commu.points.get(i - 1)
				val thisPoint = commu.points.get(i)

				val tile = seekOrCreateTile(lastPoint, thisPoint, communicationAccumulated, lineZvalue)
				tile.communications.add(commu)
				tile.requestsCache = tile.requestsCache + commu.requests

				accum.tiles.add(tile)
			}
		} else if (ExplorViz::currentPerspective == Perspective::EXECUTION) {
//			if (application.parent.parent.opened || application.parent.parent.nodes.size == 1) {
//				if (application.isGenericDataPresent(IPluginKeys::CAPMAN_EXECUTION_STATE)) {
//					val state = application.getGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE) as CapManExecutionStates
//					if (state != CapManExecutionStates::NONE) {
//						applicationQuad.blinking = true
//					}
//				}
//			}
		}
	}

	def static private seekOrCreateTile(Point start, Point end,
		List<CommunicationAccumulator> communicationAccumulated, float z) {
		for (accum : communicationAccumulated) {
			for (tile : accum.tiles) {
				if (tile.startPoint.equals(start) && tile.endPoint.equals(end)) {
					return tile
				}
			}
		}

		val tile = new CommunicationTileAccumulator()
		tile.startPoint = start
		tile.endPoint = end
		tile.positionZ = z
		tile
	}

	def static private void createCommunicationLineDrawing(List<CommunicationAccumulator> communicationAccumulated) {
		val requestsList = new ArrayList<Integer>
		communicationAccumulated.forEach [
			it.tiles.forEach [
				requestsList.add(it.requestsCache)
			]
		]

		val categories = MathHelpers::getCategoriesForCommunication(requestsList)

		communicationAccumulated.forEach [
			it.primitiveObjects.clear()
			for (tile : it.tiles) {
				tile.lineThickness = 0.07f * categories.get(tile.requestsCache) + 0.01f
			}
			LineContainer::createLine(it, viewCenterPoint)
		]
	}
}
