package explorviz.visualization.layout.application

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.layout.datastructures.hypergraph.DijkstraAlgorithm
import explorviz.visualization.layout.datastructures.hypergraph.Edge
import explorviz.visualization.layout.datastructures.hypergraph.Graph
import explorviz.visualization.layout.datastructures.hypergraph.Graphzahn
import explorviz.visualization.layout.datastructures.hypergraph.RankComperator
import explorviz.visualization.layout.datastructures.hypergraph.Vector3fNode
import explorviz.visualization.layout.datastructures.quadtree.QuadTree
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.main.MathHelpers
import java.util.ArrayList
import java.util.Comparator
import java.util.List

import static explorviz.visualization.layout.application.ApplicationLayoutInterfaceBackup.*

class ApplicationLayoutInterfaceBackup {

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
		graph.clear
		graph.fillGraph(foundationComponent, application)
		graph.createAdjacencyMatrix
		createQuadTree(foundationComponent)
		cleanUpMissingSpaces(foundationComponent)
//		cleanGraphZ(foundationComponent)
//		Logging.log("size edges before: "+pipeGraph.edges.size)
//				cleanEdgeGraph()
//		Logging.log("size edges after: "+pipeGraph.edges.size)
				
		//		cutQuadX(foundationComponent)
		//		cutQuadZ(foundationComponent)
//		pipeGraph.createAdjacencyMatrix
//		layoutEdges(application)
//
//		application.incomingCommunications.forEach [
//			layoutIncomingCommunication(it, application.components.get(0))
//		]
//
//		application.outgoingCommunications.forEach [
//			layoutOutgoingCommunication(it, application.components.get(0))
//		]

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
			size = size + calculateArea((clazzWidth),(clazzWidth))
		}
			
			var Draw3DNodeEntity smallestElement
		if(!component.clazzes.empty) {
			smallestElement = component.clazzes.get(0)
		} else {
			smallestElement = component.children.last
		}
		var int i = 0
		var boolean found = false;
		if (component.children.size > 1) {
			while (found == false) {
				if (size < calculateArea(smallestElement.width, smallestElement.depth) *
					calculateArea(Math.pow(2, i).floatValue, Math.pow(2, i).floatValue)) {
					found = true
				} else {
					i = i + 1
				}
			}

			size = (smallestElement.width) * (Math.pow(2, i).floatValue) + labelInsetSpace

			if (size <= 2f * (component.children.get(0).width + insetSpace)) {
				size = 2f * (component.children.get(0).width + insetSpace) + labelInsetSpace
			}
			
			if(!component.clazzes.empty && (component.children.size%4 == 0)) {
				if(component.children.get(0).width.equals(component.children.last.width) && component.children.get(0).depth.equals(component.children.last.depth)) {
					size = 2f * size	
				}
			}	

		} else if (component.children.size == 1) {
			size = component.children.get(0).width + labelInsetSpace

			if (!component.clazzes.empty) {
				size = 2f * size
			}
		} else {
			if (component.clazzes.size > 2) {
				size = Math.ceil(Math.sqrt(component.clazzes.size as double)).floatValue * (clazzWidth + insetSpace) + labelInsetSpace
			} else {
				size = component.clazzes.size * (clazzWidth + insetSpace) + labelInsetSpace
			}
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
			new Bounds(component.positionX+labelInsetSpace, component.positionY + floorHeight, component.positionZ, component.width-labelInsetSpace, 0,
				component.depth))

		val compi = new RankComperator(graph)
		component.children.sortInplace(compi)

		component.children.forEach [
			quad.insert(quad, it)
			createQuadTree(it)
		]

		component.clazzes.forEach [
			quad.insert(quad, it)
		]

//		if (component.opened) {
//			pipeGraph.merge(quad.getPipeEdges(quad))
//		}
		
		if (quad.nodes.get(0) != null) {

			if (emptyQuad(quad.nodes.get(2)) == true && emptyQuad(quad.nodes.get(3)) == true) {
				component.depth = component.depth / 2f
			}

			if (emptyQuad(quad.nodes.get(1)) == true && emptyQuad(quad.nodes.get(2)) == true) {
				component.width = component.width / 2f
				component.positionX = component.positionX + component.width
			}
		}
	}

	def static boolean emptyQuad(QuadTree quad) {
		if (quad.nodes.get(0) == null && quad.objects.empty == true) {
			return true
		} else {
			return false
		}
	}
	
	def static void cleanUpMissingSpaces(Component component) {
		component.children.forEach [
			cleanUpMissingSpaces(it)
		]
		
		if(mostLeftPosition(component) != 0 && !component.children.empty) {
			cutQuadX(component)
		}
		
		val float biggestZ = biggestZ(component)
		val float biggestX = biggestX(component)
		
		
		if(!component.children.empty) {
			if(component.positionZ + component.depth <= biggestZ) {
				component.depth = (biggestZ-component.positionZ) + insetSpace
			} else if (component.positionZ + component.depth > biggestZ) {
				component.depth = (biggestZ-component.positionZ) + insetSpace
			}
			
			if(component.positionX + component.width <= biggestX) {
				component.width = (biggestX-component.positionX) + labelInsetSpace
			} else if (component.positionX + component.width > biggestX) {
				component.width = (biggestX-component.positionX) + labelInsetSpace
			}
		}
		
	}
	
	def private static float mostLeftPosition(Component component) {
		var float mostLeftPosition = 0f

		if (!component.children.empty) {
			mostLeftPosition = component.children.get(0).positionX
			
			for (Component child : component.children) {
				if (child.positionX < mostLeftPosition) mostLeftPosition = child.positionX
			}
		} else if(!component.clazzes.empty) {
			mostLeftPosition = component.clazzes.get(0).positionX
			
			for(Clazz clazz : component.clazzes) {
				if(clazz.positionX < mostLeftPosition) mostLeftPosition = clazz.positionX
			}
		}

		return mostLeftPosition
	}

	def private static void cutQuadX(Component component) {
		var float mostLeftPosition = mostLeftPosition(component)

		if (mostLeftPosition > component.positionX + 4 * labelInsetSpace) {
			component.width = component.width - (mostLeftPosition - component.positionX - labelInsetSpace) + labelInsetSpace
		}

		moveComponentsX(component, -(mostLeftPosition - component.positionX - labelInsetSpace) + labelInsetSpace)
	}

	def private static void moveComponentsX(Component component, float moveParameter) {
		component.children.forEach [
			moveComponentsX(it, moveParameter)
			it.positionX = it.positionX + moveParameter
		]

		component.clazzes.forEach [
			it.positionX = it.positionX + moveParameter
		]
	}

	def private static float biggestZ(Component component) {
		var float mostBottomPosition = 0f

		if (!component.children.empty) {
			mostBottomPosition = component.children.get(0).positionZ + component.children.get(0).depth
		}

		for (Component child : component.children) {
			if (child.positionZ + child.depth > mostBottomPosition) mostBottomPosition = child.positionZ + child.depth
		}
		
		for(Clazz clazz : component.clazzes) {
			if (clazz.positionZ + clazz.depth > mostBottomPosition) mostBottomPosition = clazz.positionZ + clazz.depth			
		}

		return mostBottomPosition
	}
	
	def private static float biggestX(Component component) {
		var float mostRightPosition = 0f
		component.children.sortInplace(comp)
		
		if (!component.children.empty) {
			mostRightPosition = component.children.get(0).positionX + component.children.get(0).width
		
			for (Component child : component.children) {
				if (child.positionX + child.width > mostRightPosition) mostRightPosition = child.positionX + child.width
			}
			
		} else if(!component.clazzes.empty) {
			mostRightPosition = component.clazzes.get(0).positionX + component.clazzes.get(0).width
		
			for (Clazz clazz: component.clazzes) {
				if (clazz.positionX + clazz.width > mostRightPosition) mostRightPosition = clazz.positionX + clazz.width
			}			
		}

		return mostRightPosition
	}	

	def private static void cutQuadZ(Component component) {
		component.depth = ApplicationLayoutInterface.biggestZ(component)
	}

	def private static layoutEdges(Application application) {
		application.communicationsAccumulated.forEach [
			it.clearAllPrimitiveObjects
			it.clearAllHandlers
		]
		application.communicationsAccumulated.clear

		val DijkstraAlgorithm dijky = new DijkstraAlgorithm(pipeGraph)

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
					val Edge<Vector3fNode> pinsInOut = pinsToConnect(newCommu.source, newCommu.target)
					newCommu.points.add(start)

					if (newCommu.source != newCommu.target) {

						//						Logging.log("source: " + newCommu.source.name + " " + "target: " + newCommu.target.name)
//						if ((newCommu.source.name == "api") && (newCommu.target.name == "configuration")) {
							var List<Vector3fNode> path = dijky.dijkstra(pinsInOut.source, pinsInOut.target)

							for (Vector3f vertex : path) {
								newCommu.points.add(vertex)
							}
//						}
					}

					newCommu.points.add(end)

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

	def private static void cleanGraphX(Component component) {
		val float posX = mostLeftPosition(component)
		pipeGraph.edges.forEach [
			if (it.source.x - labelInsetSpace < posX || it.target.x - labelInsetSpace < posX) {
				pipeGraph.edges.remove(it)
			}
		]
	}

	def private static void cleanGraphZ(Component component) {
		val float posZ = ApplicationLayoutInterface.biggestZ(component)
		pipeGraph.edges.forEach [
			if (it.source.z > posZ || it.target.z > posZ) {
				pipeGraph.edges.remove(it)
			}
		]
	}

	def private static void cleanEdgeGraph() {
		pipeGraph.edges.sortInplace(new Comparator<Edge<Vector3fNode>>() {
			override compare(Edge<Vector3fNode> o1, Edge<Vector3fNode> o2) {
				return o1.source.sub(o1.target).length <=> o2.source.sub(o2.target).length
			}

			override equals(Object obj) {
				throw new UnsupportedOperationException("")
			}

		});
		pipeGraph.edges.forEach [
			var List<Vector3fNode> neighborsSource = pipeGraph.getNeighbors(it.source)
			if(neighborsSource.size > 4) {
			var int i = 0
				for(i = 0; i < neighborsSource.size; i++) {
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
