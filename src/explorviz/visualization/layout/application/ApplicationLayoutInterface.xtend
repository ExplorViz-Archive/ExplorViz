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
import explorviz.visualization.layout.datastructures.quadtree.QuadTree
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.main.MathHelpers
import java.util.ArrayList
import java.util.LinkedList
import java.util.List

class ApplicationLayoutInterface {

	public val static insetSpace = 4.0f
	public val static labelInsetSpace = 8.0f

	public val static externalPortsExtension = new Vector3f(3f, 3.5f, 3f)

	val static clazzWidth = 2.0f

	val static floorHeight = 0.75f

	val static clazzSizeDefault = 0.05f
	val static clazzSizeEachStep = 1.1f

	val static pipeSizeDefault = 0.05f
	val static pipeSizeEachStep = 0.32f
	
	val static Graphzahn graph = new Graphzahn()
	
	val static Graph<Vector3f> pipeGraph = new Graph<Vector3f>()

	val static comp = new ComponentAndClassComparator()

	def static applyLayout(Application application) throws LayoutException {
		var foundationComponent = application.components.get(0)

		// list contains only 1 Element, 
		//	root/application contains itself as the most outer component
		calcClazzHeight(foundationComponent)
		initNodes(foundationComponent)
//		calculateSize(foundationComponent)
		foundationComponent.positionX = 0f
		pipeGraph.clear
		graph.clear
		graph.fillGraph(foundationComponent, application)
		graph.createAdjacencyMatrix
		createQuadTree(foundationComponent, application.communicationsAccumulated)
//		addLabelInsetSpace(foundationComponent)
//		foundationComponent.width = foundationComponent.width + 8f
//		addLabelInsetSpaceFoundation(foundationComponent)
//		cutQuadX(foundationComponent)
//		cutQuadZ(foundationComponent)
		layoutEdges(application)
		setAbsoluteLayoutPosition(foundationComponent)

		application.incomingCommunications.forEach [
			layoutIncomingCommunication(it, application.components.get(0))
		]

		application.outgoingCommunications.forEach [
			layoutOutgoingCommunication(it, application.components.get(0))
		]

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
	
	def private static void componentTreeToString(Component component, ArrayList<String> stringList) {
		stringList.add(" \n Component " + component.name)
		component.clazzes.forEach [
			stringList.add(
				it.name + " is class of " + component.name + " width: " + it.width + " Cords: " + it.positionX + ":" +
					positionY)
		]
		component.children.forEach [
			stringList.add(
				it.name + " is child-component of " + component.name + " width: " + it.width + " Cords: " + it.positionX +
					":" + positionY)
			componentTreeToString(it, stringList)
		]
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
			size = size + calculateArea(child.width + insetSpace, child.depth + insetSpace)
		}

		var Draw3DNodeEntity smallestElement = component.children.last
		var int i = 0
		var boolean found = false;
		if (component.children.size > 1) {
			while (found == false) {
				if (size <
					calculateArea(smallestElement.width+insetSpace, smallestElement.depth+insetSpace) *
						calculateArea(Math.pow(2, i).floatValue, Math.pow(2, i).floatValue)) {
					found = true
				} else {
					i = i + 1
				}
			}
			
				size = (smallestElement.width+insetSpace) * (Math.pow(2, i).floatValue)
				
//				if(!component.clazzes.empty) {
//					size = size + Math.ceil(component.clazzes.size/2).floatValue * (clazzWidth + insetSpace)
//				}	
			
			if (size < 2f * component.children.get(0).width) {
				size = 2f * (component.children.get(0).width + labelInsetSpace)
			}
			
		} else if (component.children.size == 1) {
			size = component.children.get(0).width + labelInsetSpace

			if(!component.clazzes.empty) {
				size = 2f*size
			}	
		} else {
			if(component.clazzes.size > 2) {
				size = Math.ceil(Math.sqrt(component.clazzes.size as double)).floatValue * (clazzWidth + insetSpace)
			} else {
				size = component.clazzes.size * (clazzWidth + insetSpace)	
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

	def private static void createQuadTree(Component component, ArrayList<CommunicationAppAccumulator> communications) {

		val QuadTree quad = new QuadTree(0,
			new Bounds(component.positionX, component.positionZ, component.width, component.depth))

		val compi = new RankComperator(graph)
		component.children.sortInplace(compi)

		component.children.forEach [
			quad.insert(quad, it)
			createQuadTree(it, communications)
//			it.width = it.width + labelInsetSpace
		]

		component.clazzes.forEach [
			quad.insert(quad, it)
		]
		
//		component.quad = quad
		pipeGraph.merge(quad.getPipeEdges(quad))
		
		if (quad.nodes.get(0) != null) {
//			moveQuads(quad)
			
			if (emptyQuad(quad.nodes.get(2)) == true && emptyQuad(quad.nodes.get(3)) == true) {
				component.depth = component.depth / 2f + labelInsetSpace
			}
			
			if (emptyQuad(quad.nodes.get(1)) == true && emptyQuad(quad.nodes.get(2)) == true) {
					component.width = component.width / 2f
					component.positionX = component.positionX + component.width
			}
			
				
		} else {
			if (!quad.objects.empty && getMaxDepth(component) > 1) {
				component.depth = quad.objects.get(0).depth + labelInsetSpace
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

	def static boolean emptyQuadLayer(QuadTree quad,int quadrant) {
		if(quad.nodes.get(0) != null) {
			if(quad.nodes.get(quadrant).objects.empty) {
				return true
			}
		} else if(quad.objects.empty){
			return true
		}
		
		return false
	}
	
	def static boolean moveQuads(QuadTree quad) {
		var Component component
		if (quad.nodes.get(0) != null) {
			if(!quad.nodes.get(0).objects.empty && !(quad.nodes.get(0).objects.get(0) instanceof Clazz)) {
				quad.nodes.get(0).objects.get(0).positionX = quad.nodes.get(0).objects.get(0).positionX+labelInsetSpace
				component = quad.nodes.get(0).objects.get(0) as Component
				
				if(component.children.empty) {
				component.clazzes.forEach [
					it.positionX = it.positionX + labelInsetSpace
				]
				}
				
			}
						
			if(!quad.nodes.get(3).objects.empty && !(quad.nodes.get(3).objects.get(0) instanceof Clazz)) {
				quad.nodes.get(3).objects.get(0).positionX = quad.nodes.get(3).objects.get(0).positionX+labelInsetSpace
				component = quad.nodes.get(3).objects.get(0) as Component
				
				if(component.children.empty) {
				component.clazzes.forEach [
					it.positionX = it.positionX + labelInsetSpace
				]
				
				}			
			}

			
			moveQuads(quad.nodes.get(0))
			moveQuads(quad.nodes.get(3))	
			
			return true
		} 

		
		return false
	}
	
	def static void addLabelInsetSpace(Component component) {
		component.children.forEach [
			addLabelInsetSpace(it)
		]
		component.width = component.width + (getMaxDepth(component) * labelInsetSpace)
	}

	def static void addLabelInsetSpaceFoundation(Component component) {
		if(!component.children.empty) {
		var Component biggestLooser = biggestLooser(component.children)
		if (biggestLooser != null) {
			if((component.positionX + component.width) < (biggestLooser.positionX + biggestLooser.width)) {
				component.width = (biggestLooser.positionX + biggestLooser.width) + 8f
			}
		}
		
		}
	}

	def private static void setAbsoluteLayoutPosition(Component component) {
		component.children.forEach [
			it.positionY = it.positionY + component.positionY
			it.liftPins()
			if (component.opened) {
				it.positionY = it.positionY + component.height
				it.liftPins()
			}
			setAbsoluteLayoutPosition(it)
		]

		component.clazzes.forEach [
			it.positionY = it.positionY + component.positionY
			it.liftPins()
			if (component.opened) {
				it.positionY = it.positionY + component.height
				it.liftPins()
			}
		]
	}

	def private static Component biggestLooser(ArrayList<Component> objects) {
		var Component biggy = objects.get(0);

		for (i : 0 ..< (objects.size() - 1)) {
			if (calculateArea(objects.get(i).width, objects.get(i).depth) <
				calculateArea(objects.get(i + 1).width, objects.get(i + 1).depth)) {
				if (calculateArea(biggy.width, biggy.depth) <
					calculateArea(objects.get(i + 1).width, objects.get(i + 1).depth)) {
					biggy = objects.get(i + 1)
				}
			} else if (calculateArea(objects.get(i).width, objects.get(i).height) >
				calculateArea(biggy.width, biggy.height)) {
				biggy = objects.get(i)
			}
		}

		return biggy
	}
	def private static float mostLeftPosition(Component component) {
		var float mostLeftPosition = 0f
		
		if(!component.children.empty) {
			mostLeftPosition = component.children.get(0).positionX
		}
		
		for(Component child : component.children) {
			if(child.positionX < mostLeftPosition) mostLeftPosition = child.positionX
		}
		
		return mostLeftPosition
	}
	
	def private static void cutQuadX(Component component) {
		var float mostLeftPosition = mostLeftPosition(component)
		
		if(mostLeftPosition > component.positionX + 3*labelInsetSpace) {
			component.width = component.width-mostLeftPosition + labelInsetSpace
		}
		
		moveComponentsX(component, -mostLeftPosition + labelInsetSpace)
	}
	
	def private static void moveComponentsX(Component component, float moveParameter) {
		component.children.forEach [
			moveComponentsX(it, moveParameter)
			it.positionX = it.positionX + moveParameter
		]
				
		component.clazzes.forEach [
			it.positionX = it.positionX+moveParameter
		]
	}
	
	def private static float mostBottomPosition(Component component) {
		var float mostBottomPosition = 0f
		
		if(!component.children.empty) {
			mostBottomPosition = component.children.get(0).positionX+component.children.get(0).depth
		}
		
		for(Component child : component.children) {
			if(child.positionZ+child.depth > mostBottomPosition) mostBottomPosition = child.positionZ+child.depth
		}
		
		return mostBottomPosition
	}
	
	def private static void cutQuadZ(Component component) {
		component.depth = mostBottomPosition(component)
	}
	
	
	def private static layoutEdges(Application application) {
		application.communicationsAccumulated.forEach [
			it.clearAllPrimitiveObjects
			it.clearAllHandlers
		]
		application.communicationsAccumulated.clear

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
					val end = new Vector3f(target.positionX + target.width / 2f, target.positionY + 0.05f,
						target.positionZ + target.depth / 2f)
					val Edge<Vector3f> pinsInOut = pinsToConnect(newCommu.source, newCommu.target)
					newCommu.points.add(start)
//					
					if(!(newCommu.source == newCommu.target)) {
						var DijkstraAlgorithm<Vector3f> dijky = new DijkstraAlgorithm<Vector3f>(pipeGraph)
						dijky.dijkstra(pinsInOut.source)
						var LinkedList<Vector3f> path = dijky.getPath(pinsInOut.target)
						
						if(path != null) {
							for (Vector3f vertex : path) {
						      newCommu.points.add(vertex)
						    }
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
	
	def private static Edge<Vector3f> pinsToConnect(Draw3DNodeEntity start, Draw3DNodeEntity end) {
		var Edge<Vector3f> toConnect = null
		val ArrayList<Vector3f> startPins = new ArrayList<Vector3f>(#[start.NP, start.OP, start.SP, start.WP])
		val ArrayList<Vector3f> endPins = new ArrayList<Vector3f>(#[end.NP, end.OP, end.SP, end.WP])
		var float minimumDistance = -1f
		
		for(Vector3f startV : startPins) {
			for(Vector3f endV : endPins) {
				if(minimumDistance == -1f) {
					minimumDistance = startV.distanceTo(endV)
					toConnect = new Edge<Vector3f>(startV, endV)
				} else if(minimumDistance > startV.distanceTo(endV)) {
					minimumDistance = startV.distanceTo(endV)
					toConnect = new Edge<Vector3f>(startV,endV)
				}
			}
		}
		return toConnect
	}
}
