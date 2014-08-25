package explorviz.visualization.layout.application

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.datastructures.quadtree.QuadTree
import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.Logging
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.layout.datastructures.graph.DijkstraAlgorithm
import explorviz.visualization.layout.datastructures.graph.Edge
import explorviz.visualization.layout.datastructures.graph.Graph
import explorviz.visualization.layout.datastructures.graph.Graphzahn
import explorviz.visualization.layout.datastructures.graph.Vector3fNode
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.main.MathHelpers
import java.util.ArrayList
import java.util.Comparator
import java.util.List

import static explorviz.visualization.layout.application.ApplicationLayoutInterface.*

class ApplicationLayoutInterface {

	public val static insetSpace = 4.0f
	public val static labelInsetSpace = 8.0f

	public val static externalPortsExtension = new Vector3f(3f, 3.5f, 3f)

	val static clazzWidth = 2.0f

	val static floorHeight = 0.75f

	val static clazzSizeDefault = 0.05f
	val static clazzSizeEachStep = 1.1f

	val static pipeSizeDefault = 0.1f
	val static pipeSizeEachStep = 0.32f

	val static Graphzahn graph = new Graphzahn()

	val static Graph<Vector3fNode> pipeGraph = new Graph<Vector3fNode>()

	val static comp = new ComponentAndClassComparator()

	def static applyLayout(Application application) throws LayoutException {
		var foundationComponent = application.components.get(0)

		// list contains only 1 Element, 
		//	root/application contains itself as the most outer component
		calcClazzHeight(foundationComponent)
		initNodes(foundationComponent)

		foundationComponent.positionX = 0f
		foundationComponent.positionY = 0f
		foundationComponent.positionZ = 0f
		pipeGraph.clear

		createQuadTree(foundationComponent)
		createPipes(foundationComponent)

		//		graph.clear
		//		graph.fillGraph(foundationComponent, application)
		//		graph.createAdjacencyMatrix
		//		cleanUpMissingSpaces(foundationComponent)
		//		cleanGraphZ(foundationComponent)
		//		Logging.log("size edges before: "+pipeGraph.edges.size)
		//				cleanEdgeGraph()
		//		Logging.log("size edges after: "+pipeGraph.edges.size)
		pipeGraph.createAdjacencyMatrix
		layoutEdges(application)

		application.incomingCommunications.forEach [
			layoutIncomingCommunication(it, application.components.get(0))
		]

		application.outgoingCommunications.forEach [
			layoutOutgoingCommunication(it, application.components.get(0))
		]
		Logging.log("leer?: " + (application.previousComponents == null))
		application.previousComponents = application.components
		application.previousCommunications = application.communications
		application

	}

	def private static int getMaxDepth(Component compo) {
		if (compo.children.empty) return 0

		var currentMax = 0
		for (child : compo.children) {
			currentMax = Math.max(currentMax, getMaxDepth(child) + 1)
		}

		return currentMax
	}

	def private static void calcClazzHeight(Component component) {
		val clazzes = new ArrayList<Clazz>()
		getClazzList(component, clazzes, true)

		val instanceCountList = new ArrayList<Integer>()
		clazzes.forEach [
			instanceCountList.add(it.instanceCount)
		]

		val categories = MathHelpers::getCategoriesForClazzes(instanceCountList)

		clazzes.forEach [
			it.height = clazzSizeEachStep * categories.get(it.instanceCount) + clazzSizeDefault
		]
	}

	def private static void getClazzList(Component component, List<Clazz> clazzes, boolean beginning) {
		component.children.forEach [
			getClazzList(it, clazzes, false)
		]

		component.clazzes.forEach [
			clazzes.add(it)
		]
	}

	def private static void initNodes(Component component) {
		component.children.forEach [
			initNodes(it)
		]

		component.clazzes.forEach [
			applyMetrics(it)
		]

		applyMetrics(component)
	}

	def private static applyMetrics(Clazz clazz) {
		clazz.width = clazzWidth
		clazz.depth = clazzWidth
	}

	def private static void applyMetrics(Component component) {
		component.height = getHeightOfComponent(component)
		component.width = 0f
		component.depth = 0f
		calculateSize(component)

	}

	def private static void calculateSize(Component component) {
		var float size = 0f

		if (!component.children.empty) {
			component.children.sortInplace(comp)
		}

		component.children.forEach [
			calculateSize(it)
		]

		for (child : component.children) {
			size = size + calculateArea(child.width, child.depth)
		}

		for (clazz : component.clazzes) {
			size = size + calculateArea(clazzWidth + insetSpace, clazzWidth + insetSpace)
		}

		var Draw3DNodeEntity smallestElement
		if (!component.clazzes.empty) {
			smallestElement = component.clazzes.get(0)
		} else {
			smallestElement = component.children.last
		}
		var int i = 0
		var boolean found = false;
		if (component.children.size > 1) {
			while (found == false) {
				if (size < calculateArea(smallestElement.width + insetSpace, smallestElement.depth + insetSpace) *
					calculateArea(Math.pow(2, i).floatValue, Math.pow(2, i).floatValue)) {
					found = true
				} else {
					i = i + 1
				}
			}

			size = (smallestElement.width + insetSpace) * (Math.pow(2, i).floatValue) + labelInsetSpace

			if (size <= 2f * component.children.get(0).width) {
				size = 2f * (component.children.get(0).width + insetSpace) + labelInsetSpace
			}

			if (!component.clazzes.empty && (component.children.size % 4 == 0)) {
				if (component.children.get(0).width.equals(component.children.last.width) &&
					component.children.get(0).depth.equals(component.children.last.depth)) {
					size = 2f * size
				}
			}
		} else if (component.children.size == 1) {
			size = component.children.get(0).width + labelInsetSpace

			if (!component.clazzes.empty) {
				size = 2f * size
			}
		} else {
			var float findSpace = Math.ceil(Math.sqrt(component.clazzes.size as double)).floatValue
			var int p = 0
			var boolean foundSpace = false
			while (foundSpace == false) {
				if (Math.pow(2, p).floatValue >= findSpace) {
					foundSpace = true
				} else {
					p++
				}
			}
			size = Math.pow(2, p).floatValue * (clazzWidth + insetSpace) + labelInsetSpace

		}

		component.width = size
		component.depth = size
	}

	def private static getHeightOfComponent(Component component) {
		if (!component.opened) {
			var childrenHeight = floorHeight

			for (child : component.children)
				if (child.height > childrenHeight)
					childrenHeight = child.height

			for (child : component.clazzes)
				if (child.height > childrenHeight)
					childrenHeight = child.height

			childrenHeight + 0.1f
		} else {
			floorHeight
		}
	}

	def private static float calculateArea(float width, float height) {
		return width * height
	}

	def private static void createQuadTree(Component component) {

		val QuadTree quad = new QuadTree(0,
			new Bounds(component.positionX + labelInsetSpace, component.positionY + floorHeight, component.positionZ,
				component.width - labelInsetSpace, 0, component.depth))

		//		val compi = new RankComperator(graph)
		component.children.sortInplace(comp).reverse

		//		var List<Component> compList = graph.orderComponents(component)
		component.children.forEach [
			quad.insert(quad, it)
			createQuadTree(it)
		]

		component.clazzes.forEach [
			quad.insert(quad, it)
		]

		quad.merge(quad)
		quad.adjustQuadTree(quad)

		component.quadTree = quad
		component.adjust

	}

	def private static void createPipes(Component component) {
		component.children.forEach [
			createPipes(it)
		]
		if (component.opened) {
			pipeGraph.merge(component.quadTree.getPipeEdges(component.quadTree))
		}
	}

	def private static layoutEdges(Application application) {
		application.communicationsAccumulated.forEach [
			it.clearAllPrimitiveObjects
			it.clearAllHandlers
		]
		application.communicationsAccumulated.clear

		//		val DijkstraAlgorithm dijky = new DijkstraAlgorithm(pipeGraph)
		application.communications.forEach [
			val source = if (it.source.parent.opened) it.source else findFirstParentOpenComponent(it.source.parent)
			val target = if (it.target.parent.opened) it.target else findFirstParentOpenComponent(it.target.parent)
			if (source != null && target != null) {
				var found = false
				for (commu : application.communicationsAccumulated) {
					if (found == false) {
						found = ((commu.source == source) && (commu.target == target))

						if (found) {
							commu.requests = commu.requests + it.requests
							commu.aggregatedCommunications.add(it)
						}
					}
				}

				if (found == false) {
					val newCommu = new CommunicationAppAccumulator()
					newCommu.source = source
					newCommu.target = target
					newCommu.requests = it.requests

					val start = new Vector3f(source.positionX + source.width / 2f, source.positionY,
						source.positionZ + source.depth / 2f)
					val end = new Vector3f(target.positionX + target.width / 2f, target.positionY,
						target.positionZ + target.depth / 2f)

					//					val Edge<Vector3fNode> pinsInOut = pinsToConnect(newCommu.source, newCommu.target)
					//					newCommu.points.add(start)
					//					newCommu.points.add(start)
					////					newCommu.points.add(new Vector3f(start.x + 10f, start.y, start.z))
					////					newCommu.points.add(new Vector3f(start.x + 10f, start.y, start.z+ 10f))
					////					newCommu.points.add(new Vector3f(start.x - 10f, start.y, start.z))
					////					newCommu.points.add(new Vector3f(start.x - 10f, start.y, start.z -10f))
					//					newCommu.points.add(end)
					pipeGraph.edges.forEach [
						newCommu.points.add(it.source)
						newCommu.points.add(it.target)
					]

					//					newCommu.points.add(end)
					//						Logging.log("Comu: " + newCommu.points)
					newCommu.aggregatedCommunications.add(it)

					application.communicationsAccumulated.add(newCommu)
				}
			}
		]

		calculatePipeSizeFromQuantiles(application)
	}

	def private static Component findFirstParentOpenComponent(Component entity) {
		if (entity.parentComponent == null || entity.parentComponent.opened) {
			return entity
		}

		return findFirstParentOpenComponent(entity.parentComponent)
	}

	public def static calculatePipeSizeFromQuantiles(Application application) {
		val requestsList = new ArrayList<Integer>
		gatherRequestsIntoList(application, requestsList)

		val categories = MathHelpers::getCategoriesForCommunication(requestsList)

		application.communicationsAccumulated.forEach [
			if (it.state != EdgeState.HIDDEN)
				it.pipeSize = categories.get(it.requests) * pipeSizeEachStep + pipeSizeDefault
		]

	//		application.incomingCommunications.forEach [ // TODO
	//			it.lineThickness = categories.get(it.requests) * pipeSizeEachStep + pipeSizeDefault
	//		]
	//
	//		application.outgoingCommunications.forEach [
	//			requestsList.add(it.requests)
	//			it.lineThickness = categories.get(it.requests) * pipeSizeEachStep + pipeSizeDefault
	//		]
	}

	private def static gatherRequestsIntoList(Application application, ArrayList<Integer> requestsList) {
		application.communicationsAccumulated.forEach [
			if (it.state != EdgeState.HIDDEN)
				requestsList.add(it.requests)
		]

		application.incomingCommunications.forEach [
			//			if (it.state != EdgeState.HIDDEN) // TODO
			//				requestsList.add(it.requests)
		]

		application.outgoingCommunications.forEach [
			//			if (it.state != EdgeState.HIDDEN) // TODO
			//				requestsList.add(it.requests)
		]
	}

	def private static void layoutIncomingCommunication(Communication commu, Component foundation) {
		val centerCommuIcon = new Vector3f(foundation.positionX - externalPortsExtension.x * 6f,
			foundation.positionY - foundation.extension.y + externalPortsExtension.y,
			foundation.positionZ + foundation.extension.z * 2f - externalPortsExtension.z)

		layoutInAndOutCommunication(commu, commu.targetClazz, centerCommuIcon)
	}

	def private static void layoutOutgoingCommunication(Communication commu, Component foundation) {
		val centerCommuIcon = new Vector3f(
			foundation.positionX + foundation.extension.x * 2f + externalPortsExtension.x * 4f,
			foundation.positionY - foundation.extension.y + externalPortsExtension.y,
			foundation.positionZ + foundation.extension.z * 2f - externalPortsExtension.z - 12f)

		layoutInAndOutCommunication(commu, commu.sourceClazz, centerCommuIcon)
	}

	def private static void layoutInAndOutCommunication(Communication commu, Clazz internalClazz,
		Vector3f centerCommuIcon) {
		commu.pointsFor3D.add(centerCommuIcon)

		if (internalClazz != null) {
			val end = new Vector3f()
			end.x = internalClazz.positionX + internalClazz.width / 2f
			end.y = internalClazz.centerPoint.y
			end.z = internalClazz.positionZ + internalClazz.depth / 2f
			commu.pointsFor3D.add(end)
		}
	}

	def private static Edge<Vector3fNode> pinsToConnect(Draw3DNodeEntity start, Draw3DNodeEntity end) {
		var Edge<Vector3fNode> toConnect = null
		val ArrayList<Vector3f> startPins = new ArrayList<Vector3f>(#[start.NP, start.OP, start.SP, start.WP])
		val ArrayList<Vector3f> endPins = new ArrayList<Vector3f>(#[end.NP, end.OP, end.SP, end.WP])
		var float minimumDistance = -1f

		for (Vector3f startV : startPins) {
			for (Vector3f endV : endPins) {
				if (minimumDistance == -1f) {
					minimumDistance = startV.distanceTo(endV)
					toConnect = new Edge<Vector3fNode>(new Vector3fNode(startV), new Vector3fNode(endV))
				} else if (minimumDistance > startV.distanceTo(endV)) {
					minimumDistance = startV.distanceTo(endV)
					toConnect = new Edge<Vector3fNode>(new Vector3fNode(startV), new Vector3fNode(endV))
				}
			}
		}
		return toConnect
	}

	def private static void cleanEdgeGraph() {
		pipeGraph.edges.sortInplace(
			new Comparator<Edge<Vector3fNode>>() {
				override compare(Edge<Vector3fNode> o1, Edge<Vector3fNode> o2) {
					return o1.source.sub(o1.target).length <=> o2.source.sub(o2.target).length
				}

				override equals(Object obj) {
					throw new UnsupportedOperationException("")
				}

			});
		pipeGraph.edges.forEach [
			var List<Vector3fNode> neighborsSource = pipeGraph.getNeighbors(it.source)
			if (neighborsSource.size > 4) {
				var int i = 0
				for (i = 0; i < neighborsSource.size; i++) {
					var neighbor = neighborsSource.get(i)

					if (it.target.z == it.source.z) {
						if ((it.target.x < neighbor.x && neighbor.x < it.source.x) ||
							(it.target.x > neighbor.x && neighbor.x > it.source.x)) {
							pipeGraph.edges.remove(it)

							//							Logging.log("z same and source: "+it.source + " between " + neighbor + " target "+it.target)
							i = neighborsSource.size
						}
					} else if (it.target.x == it.source.x) {
						if ((it.target.z < neighbor.z && neighbor.z < it.source.z) ||
							(it.target.z > neighbor.z && neighbor.z > it.source.z)) {
							pipeGraph.edges.remove(it)

							//							Logging.log("x same and source: "+it.source + " between " + neighbor + " target "+it.target)
							i = neighborsSource.size
						}
					}
				}

			}
		]
	}
}
