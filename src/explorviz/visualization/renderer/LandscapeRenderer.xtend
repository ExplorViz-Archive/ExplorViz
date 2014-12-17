package explorviz.visualization.renderer

import explorviz.shared.model.Application
import explorviz.shared.model.Communication
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.System
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.BoxContainer
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.primitives.PipeContainer
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.experiment.Experiment
import java.util.ArrayList
import java.util.List
import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.visualization.engine.primitives.QuadContainer
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.primitives.Line
import explorviz.visualization.engine.primitives.LineContainer

class LandscapeRenderer {
	static var Vector3f viewCenterPoint = null
	static val DEFAULT_Z_LAYER_DRAWING = 0f

	static val List<PrimitiveObject> arrows = new ArrayList<PrimitiveObject>()

	def static void drawLandscape(Landscape landscape, List<PrimitiveObject> polygons, boolean firstViewAfterChange) {
		calcViewCenterPoint(landscape, firstViewAfterChange)

		arrows.clear()
		PipeContainer::clear()
		BoxContainer::clear()
		LabelContainer::clear()
		QuadContainer::clear()
		LineContainer::clear()

		landscape.systems.forEach [
			clearDrawingEntities(it)
			createSystemDrawing(it, DEFAULT_Z_LAYER_DRAWING)
		]

		landscape.applicationCommunication.forEach [
			it.primitiveObjects.clear()
			createCommunicationLine(DEFAULT_Z_LAYER_DRAWING, it, viewCenterPoint)
		]
		
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

	def private static createSystemDrawing(System system, float z) {
		if (system.nodeGroups.size() > 1) {
			system.positionZ = z - 0.2f
			QuadContainer::createQuad(system, viewCenterPoint, null, System::backgroundColor, false)

//			createOpenSymbol(system, System::plusColor, System::backgroundColor)
			createSystemLabel(system, system.name)
		}

		if (system.opened) {
			system.nodeGroups.forEach [
				createNodeGroupDrawing(it, z)
			]
		}

		drawTutorialIfEnabled(system, new Vector3f(system.positionX, system.positionY, z))
	}
	
	def private static void createOpenSymbol(DrawNodeEntity entity, Vector4f plusColor, Vector4f backgroundColor) {
		val extensionX = 0.1f
		val extensionY = 0.1f

		val TOP_RIGHT = new Vector3f(entity.positionX + entity.width, entity.positionY, entity.positionZ)

		var float centerX = TOP_RIGHT.x - extensionX * 1.5f
		var float centerY = TOP_RIGHT.y - extensionY * 1.5f

		var symbol = "\u2013" // -
		if (entity instanceof System) {
			if (!entity.opened) symbol = "+"
		} else if (entity instanceof NodeGroup) {
			if (!entity.opened) symbol = "+"
		}

		val texture = TextureManager::createTextureFromText(symbol, 128, 128, Math.round(plusColor.x * 255),
			Math.round(plusColor.y * 255), Math.round(plusColor.z * 255), 'bold 256px Arial', backgroundColor)

		new Quad(
			new Vector3f(centerX, centerY, TOP_RIGHT.z + 0.01f),
			new Vector3f(extensionX, extensionY, 0.0f),
			texture,
			null,
			true, true
		)
		
//		QuadContainer::createQuad(this, centerPoint, texture, null, z)
	}
	
	private def static void createSystemLabel(System system, String name) {
		val Vector3f ORIG_TOP_LEFT = new Vector3f(system.positionX, system.positionY, 0f).sub(viewCenterPoint)
		val Vector3f ORIG_TOP_RIGHT = new Vector3f(system.positionX, system.positionY + system.width, 0f).sub(viewCenterPoint)
		
		val labelWidth = 2.5f
		val labelHeight = 1f

		val labelOffsetTop = 0.1f

		val absolutLabelLeftStart = ORIG_TOP_LEFT.x + ((ORIG_TOP_RIGHT.x - ORIG_TOP_LEFT.x) / 2f) - (labelWidth / 2f)

		val BOTTOM_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_TOP_LEFT.y - labelOffsetTop - labelHeight, 0.05f)
		val BOTTOM_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth,
			ORIG_TOP_RIGHT.y - labelOffsetTop - labelHeight, 0.05f)
		val TOP_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth, ORIG_TOP_RIGHT.y - labelOffsetTop, 0.05f)
		val TOP_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_TOP_LEFT.y - labelOffsetTop, 0.05f)
		
		LabelContainer::createLabel(name, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, false, false, false, false)
	}
	
	private def static void drawTutorialIfEnabled(DrawNodeEntity nodeEntity, Vector3f pos) {
		arrows.addAll(Experiment::drawTutorial(nodeEntity.name, pos,
			nodeEntity.width, nodeEntity.height, viewCenterPoint))
	}

	def private static createNodeGroupDrawing(NodeGroup nodeGroup, float z) {
		if (nodeGroup.nodes.size() > 1) {
			nodeGroup.positionZ = z
			QuadContainer::createQuad(nodeGroup, viewCenterPoint, null, NodeGroup::backgroundColor, false)
			createOpenSymbol(nodeGroup, NodeGroup::plusColor, NodeGroup::backgroundColor)
		}

		nodeGroup.nodes.forEach [
			createNodeDrawing(it, z)
		]
		
		drawTutorialIfEnabled(nodeGroup, new Vector3f(nodeGroup.positionX, nodeGroup.positionY+0.05f, z))
	}

	def private static createNodeDrawing(Node node, float z) {
		if (node.visible) {
			node.positionZ = z + 0.01f
			QuadContainer::createQuad(node, viewCenterPoint, null, ColorDefinitions::nodeBackgroundColor, false)
			
			val labelName = if (node.parent.opened) {
					if (node.ipAddress != null && !node.ipAddress.empty && !node.ipAddress.startsWith("<")) {
						node.ipAddress
					} else {
						node.name
					}
				} else {
					node.parent.name
				}
//			createNodeLabel(node, labelName)

			node.applications.forEach [
				createApplicationDrawing(it, z)
			]

			drawTutorialIfEnabled(node, new Vector3f(node.positionX, node.positionY, z))
		}
	}
	
	def private static void createNodeLabel(Node node, String ipAddress) {
		val ORIG_BOTTOM_LEFT = new Vector3f(node.positionX, node.positionY + node.height, 0f)
		val ORIG_BOTTOM_RIGHT = new Vector3f(node.positionX + node.width, node.positionY + node.height, 0f)

		val labelWidth = 2.0f
		val labelHeight = 0.75f

		val labelOffsetBottom = 0.1f

		val absolutLabelLeftStart = ORIG_BOTTOM_LEFT.x + ((ORIG_BOTTOM_RIGHT.x - ORIG_BOTTOM_LEFT.x) / 2f) -
			(labelWidth / 2f)

		val BOTTOM_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_BOTTOM_LEFT.y + labelOffsetBottom, 0.05f)
		val BOTTOM_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth, ORIG_BOTTOM_RIGHT.y + labelOffsetBottom,
			0.05f)
		val TOP_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth,
			ORIG_BOTTOM_RIGHT.y + labelOffsetBottom + labelHeight, 0.05f)
		val TOP_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_BOTTOM_LEFT.y + labelOffsetBottom + labelHeight, 0.05f)

//		new Quad(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT,
//			TextureManager::
//				createTextureFromTextWithTextSizeWithFgColorWithBgColor(ipAddress, 1024, 512, 105,
//					ColorDefinitions::nodeForegroundColor, ColorDefinitions::nodeBackgroundColor))
	}

	def private static createApplicationDrawing(Application application, float z) {
//		var WebGLTexture texture = null
//		if (application.image != null && !application.image.empty) {
//			if (application.database) {
//				texture = TextureManager::createTextureFromImagePath(application.image, 8, 150, 496, 200, 512, 512)
//			} else {
//				texture = TextureManager::createTextureFromImagePath(application.image, 50, 50, 412, 156, 512, 512) // 256
//			}
//		} else {
//			if (application.database) {
//				texture = TextureManager::createTextureFromTextAndImagePath(application.name, "logos/database.png", 512, 256, 60,
//					ColorDefinitions::applicationForegroundColor, ColorDefinitions::applicationBackgroundColor,
//					ColorDefinitions::applicationBackgroundRightColor)
//			} else {
//				texture = TextureManager::createTextureFromTextAndImagePath(application.name, "logos/java.png", 512, 256, 60,
//					ColorDefinitions::applicationForegroundColor, ColorDefinitions::applicationBackgroundColor,
//					ColorDefinitions::applicationBackgroundRightColor)
//			}
//		}
		application.positionZ = z + 0.04f
		QuadContainer::createQuad(application, viewCenterPoint, null, null, true)
		
		drawTutorialIfEnabled(application, new Vector3f(application.positionX, application.positionY-0.05f, z))
	}
	
	def static void createCommunicationLine(float z, Communication commu, Vector3f centerPoint) {
		val lineZvalue = z + 0.02f

		if (!commu.points.empty) {
			commu.positionZ = lineZvalue
			LineContainer::createLine(commu, centerPoint)
			
			val arrow = Experiment::drawTutorialCom(commu.source.name, commu.target.name,
				new Vector3f(commu.source.positionX, commu.source.positionY, z), commu.source.width, commu.source.height,
				centerPoint)
			commu.primitiveObjects.addAll(arrow)
		}
	}
}
