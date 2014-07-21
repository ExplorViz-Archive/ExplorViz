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

		drawTutorialIfEnabled(system, z)
	}
	
	private def static void drawTutorialIfEnabled(DrawNodeEntity nodeEntity, float z) {
		arrows.addAll(Experiment::drawTutorial(nodeEntity.name, new Vector3f(nodeEntity.positionX, nodeEntity.positionY, z),
			nodeEntity.width, nodeEntity.height, viewCenterPoint))
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

		drawTutorialIfEnabled(nodeGroup, z)
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

			node.applications.forEach [
				createApplicationDrawing(it, z, polygons)
			]

			drawTutorialIfEnabled(node, z)
		}
	}

	def private static createApplicationDrawing(Application application, float z, List<PrimitiveObject> polygons) {
		val applicationQuad = application.createApplicationQuad(application.name, z + 0.04f, viewCenterPoint)
		
		application.primitiveObjects.add(applicationQuad)
		
		polygons.add(applicationQuad)

		drawTutorialIfEnabled(application, z)
	}
}
