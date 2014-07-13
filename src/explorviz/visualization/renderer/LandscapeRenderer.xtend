package explorviz.visualization.renderer

import explorviz.shared.model.Application
import explorviz.shared.model.Communication
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.System
import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.experiment.Experiment
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.primitives.BoxContainer
import explorviz.visualization.engine.primitives.PipeContainer

class LandscapeRenderer {
	static var Vector3f centerPoint = null

	static val List<PrimitiveObject> arrows = new ArrayList<PrimitiveObject>()


	static val MIN_X = 0
	static val MAX_X = 1
	static val MIN_Y = 2
	static val MAX_Y = 3

	def static void drawLandscape(Landscape landscape, List<PrimitiveObject> polygons, boolean firstViewAfterChange) {
		if (centerPoint == null || firstViewAfterChange) {
			calculateCenterAndZZoom(landscape)
		}

		val DEFAULT_Z_LAYER_DRAWING = 0f
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
		]
		Communication::createCommunicationLines(DEFAULT_Z_LAYER_DRAWING, landscape, centerPoint, polygons)
		
		polygons.addAll(arrows)

		
	}

	def static void calculateCenterAndZZoom(Landscape landscape) {
		val rect = getLandscapeRect(landscape)
		val SPACE_IN_PERCENT = 0.02f

		val perspective_factor = WebGLStart::viewportWidth / WebGLStart::viewportHeight as float

		var requiredWidth = Math.abs(rect.get(MAX_X) - rect.get(MIN_X))
		requiredWidth += requiredWidth * SPACE_IN_PERCENT
		var requiredHeight = Math.abs(rect.get(MAX_Y) - rect.get(MIN_Y))
		requiredHeight += requiredHeight * SPACE_IN_PERCENT

		val newZ_by_width = requiredWidth * -1f / perspective_factor
		val newZ_by_height = requiredHeight * -1f

		Camera::getVector.z = Math.min(Math.min(newZ_by_width, newZ_by_height), -10f)

		centerPoint = new Vector3f(rect.get(MIN_X) + ((rect.get(MAX_X) - rect.get(MIN_X)) / 2f),
			rect.get(MIN_Y) + ((rect.get(MAX_Y) - rect.get(MIN_Y)) / 2f), 0)
	}

	def private static createSystemDrawing(System system, float z, List<PrimitiveObject> polygons) {
		if (system.nodeGroups.size() > 1) {
			val systemQuad = system.createSystemQuad(z - 0.2f, centerPoint)

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

		val arrow = Experiment::drawTutorial(system.name, new Vector3f(system.positionX, system.positionY, z),
			system.width, system.height, centerPoint)
		arrows.addAll(arrow)
	}

	def private static createNodeGroupDrawing(NodeGroup nodeGroup, float z, List<PrimitiveObject> polygons) {
		if (nodeGroup.nodes.size() > 1) {
			val nodeGroupQuad = nodeGroup.createNodeGroupQuad(z, centerPoint)

			val nodeGroupOpenSymbol = nodeGroup.createNodeGroupOpenSymbol()

			nodeGroup.primitiveObjects.add(nodeGroupQuad)
			nodeGroup.primitiveObjects.add(nodeGroupOpenSymbol)

			polygons.add(nodeGroupQuad)
			polygons.add(nodeGroupOpenSymbol)
		}

		nodeGroup.nodes.forEach [
			createNodeDrawing(it, z, polygons)
		]

		val arrow = Experiment::drawTutorial(nodeGroup.name, new Vector3f(nodeGroup.positionX, nodeGroup.positionY, z),
			nodeGroup.width, nodeGroup.height, centerPoint)
		arrows.addAll(arrow)
	}

	def private static createNodeDrawing(Node node, float z, List<PrimitiveObject> polygons) {
		if (node.visible) {
			val nodeQuad = node.createNodeQuad(z + 0.01f, centerPoint)
			val label = if (node.parent.opened) node.ipAddress else node.parent.name
			val nodeLabel = node.createNodeLabel(nodeQuad, label)

			node.primitiveObjects.add(nodeQuad)
			node.primitiveObjects.add(nodeLabel)

			polygons.add(nodeQuad)
			polygons.add(nodeLabel)

			node.applications.forEach [
				createApplicationDrawing(it, z, polygons)
			]

			val arrow = Experiment::drawTutorial(node.name, new Vector3f(node.positionX, node.positionY, z),
				node.width, node.height, centerPoint)
			arrows.addAll(arrow)
		}
	}

	def private static createApplicationDrawing(Application application, float z, List<PrimitiveObject> polygons) {
		var PrimitiveObject oldQuad = null
		if (!application.primitiveObjects.empty) {
			oldQuad = application.primitiveObjects.get(0)
		}

		val applicationQuad = application.createApplicationQuad(application.name, z + 0.04f, centerPoint, oldQuad)
		application.primitiveObjects.add(applicationQuad)
		polygons.add(applicationQuad)

		val arrow = Experiment::drawTutorial(application.name,
			new Vector3f(application.positionX, application.positionY, z), application.width, application.height,
			centerPoint)
		arrows.addAll(arrow)
	}

	def private static List<Float> getLandscapeRect(Landscape landscape) {
		val rect = new ArrayList<Float>
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)

		if (landscape.systems.empty) {
			rect.set(MIN_X, 0f)
			rect.set(MAX_X, 1f)
			rect.set(MIN_Y, 0f)
			rect.set(MAX_Y, 1f)
		}

		landscape.systems.forEach [ system |
			getMinMaxFromQuad(system, rect)
			system.nodeGroups.forEach [
				it.nodes.forEach [
					getMinMaxFromQuad(it, rect)
				]
			]
		]

		rect
	}

	def private static void getMinMaxFromQuad(DrawNodeEntity it, ArrayList<Float> rect) {
		val curX = it.positionX
		val curY = it.positionY
		if (curX < rect.get(MIN_X)) {
			rect.set(MIN_X, curX)
		}
		if (rect.get(MAX_X) < curX + (it.width)) {
			rect.set(MAX_X, curX + (it.width))
		}
		if (curY > rect.get(MAX_Y)) {
			rect.set(MAX_Y, curY)
		}
		if (rect.get(MIN_Y) > curY - (it.height)) {
			rect.set(MIN_Y, curY - (it.height))
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
}
