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
	
	public val static buh = Math.log(2.0)
	public val static insetSpace = 4f
	public val static labelInsetSpace = 4f

	public val static externalPortsExtension = new Vector3f(3f, 3.5f, 3f)

	val static clazzWidth = 2f
	
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
		applyMetrics(foundationComponent)
		calculateSize(foundationComponent)

		foundationComponent.positionX = 0f
		foundationComponent.positionY = 0f
		foundationComponent.positionZ = 0f
		pipeGraph.clear		

		createQuadTree(foundationComponent)

		
		putOldComps(foundationComponent)

		createPins(foundationComponent)
		createPipes(foundationComponent)
		pipeGraph.createAdjacencyMatrix
		layoutEdges(application)


		application.incomingCommunications.forEach [
			layoutIncomingCommunication(it, application.components.get(0))
		]

		application.outgoingCommunications.forEach [
			layoutOutgoingCommunication(it, application.components.get(0))
		]

		application

	}
	
	def private static void putOldComps(Component comp) {
		comp.children.forEach [
			putOldComps(it)
		]
		
			comp.putPreviousLists
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

	def private static void applyMetrics(Component component) {
		component.children.forEach [
			applyMetrics(it)
		]
		
		component.clazzes.forEach [
			it.width = clazzWidth
			it.depth = clazzWidth
		]

		calculateSize(component)
	}

	def private static void calculateSize(Component component) {
		var float size = 0f
		var float sideLength = 0f
		if (!component.children.empty) {
			component.children.sortInplace(comp)
		}

		for (child : component.children) {
			size = size + calculateArea(child.width+insetSpace, child.depth+insetSpace)
		}

		for (clazz : component.clazzes) {
			size = size + calculateArea(clazzWidth+insetSpace, clazzWidth+insetSpace)
		}

		var Draw3DNodeEntity smallestElement
		if (!component.clazzes.empty) {
			smallestElement = component.clazzes.get(0)
		} else {
			smallestElement = component.children.last
		}

		var int i = 0
		var boolean found = false;
		
			while (found == false) {
				if (size < calculateArea(smallestElement.width + insetSpace, smallestElement.depth + insetSpace) *
					calculateArea(Math.pow(2, i).floatValue, Math.pow(2, i).floatValue)) {
					found = true
				} else {
					i = i + 1
				}
			}
			
			sideLength = (smallestElement.width + insetSpace) * (Math.pow(2, i).floatValue)
					
		if (component.children.size > 1) {
			if (sideLength/2f <= (component.children.get(0).width) + insetSpace) {
				var float factorLength = component.children.get(0).width + insetSpace
				
				if(component.children.size == 4 && !component.clazzes.empty) {
					if(2f * (component.children.last.width) >= factorLength) {
						factorLength = 2f * (component.children.last.width + insetSpace)
					}
				}		
				
				sideLength = 2f * (smallestElement.width + insetSpace) * Math.pow(2, Math.log(Math.ceil(((factorLength)/(smallestElement.width + insetSpace)).doubleValue))/Math.log(2.0)).floatValue
			}
			
			if(component.children.get(0).width.equals(component.children.last.width) &&
					component.children.get(0).depth.equals(component.children.last.depth)) {
					
					if (!component.clazzes.empty && (Math.log(component.children.size)/Math.log(2.0))%2 == 0) {
							sideLength = 2f * (sideLength + insetSpace)
					}					
			}

		} else if (component.children.size == 1) {

			if (!component.clazzes.empty) {
				if((component.children.get(0).width + insetSpace) >= sideLength/2f) {
					sideLength = 2f * (smallestElement.width + insetSpace) * Math.pow(2, Math.log(Math.ceil(((component.children.get(0).width+insetSpace)/(smallestElement.width + insetSpace)).doubleValue))/Math.log(2.0)).floatValue
				}
			} else {
				sideLength = component.children.get(0).width + insetSpace
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
			sideLength = Math.pow(2, p).floatValue * (clazzWidth + insetSpace)

		}
		
		sideLength = sideLength + 2f* labelInsetSpace
		
		if (component.oldBounds != null) {
			var Draw3DNodeEntity largestElement
			if (component.children.empty) {
				largestElement = component.clazzes.get(0)
			} else {
				largestElement = component.children.get(0)
			}
			if (sideLength > component.oldBounds.width && sideLength/2f <= largestElement.width + insetSpace) {
				sideLength = 2f * (component.oldBounds.width + insetSpace + labelInsetSpace)
			} else if (component.oldBounds.width > sideLength) {
				sideLength = component.oldBounds.width
			}
		}
				
		component.width = sideLength 
		component.depth = sideLength
		component.height = getHeightOfComponent(component)
	}

	def static Component findCompByComp(Component toFind, Component prevComp) {
		if (prevComp != null) {
			var Component prev = new Component()

			if (prevComp.name == toFind.name) {
				prev = prevComp
			} else if (prevComp.children.contains(toFind)) {
				for (Component child : prevComp.children) {
					if (child.name.equals(toFind.name)) {
						prev = child
					}
				}
			} else {
				for (Component child : prevComp.children) {
					prev = findCompByComp(toFind, child)
				}
			}
			return prev
		} else {
			return null
		}
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
		var QuadTree quad
			quad = new QuadTree(0,
			new Bounds(component.positionX + labelInsetSpace , component.positionY + floorHeight, component.positionZ + labelInsetSpace,
				component.width - 2f*labelInsetSpace , 0, component.depth - 2f*labelInsetSpace))
		
		if(component.insertionOrderList.empty) {
				component.insertionOrderList.addAll(component.children)
				component.insertionOrderList.addAll(component.clazzes)
		} else {
			component.children.forEach [
				if(!component.insertionOrderList.contains(it)) {
					component.insertionOrderList.add(it)
				}
			]
			
			component.clazzes.forEach [
				if(!component.insertionOrderList.contains(it)) {
					component.insertionOrderList.add(it)
				}
			]	
		}
		
		var boolean inserted = false
		for(Draw3DNodeEntity entity : component.insertionOrderList) {
			inserted = quad.insert(quad, entity)
			
			if(entity instanceof Component) {
				createQuadTree(entity)
			}
		}
		
		if(inserted == true) {
		component.putOldBounds
		
		quad.merge(quad)
		quad.adjustQuadTree(quad)
		component.quadTree = quad
		component.adjust
		}

	}

	def private static void createPins(Component component) {
		component.quadTree.setPins(component.quadTree)
		component.children.forEach [
			createPins(it)
		]
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
					
											var List<Vector3fNode> path = dijky.dijkstra(pinsInOut.source, pinsInOut.target)
					
											for (Vector3f vertex : path) {
												newCommu.points.add(vertex)
											}
					
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
}
