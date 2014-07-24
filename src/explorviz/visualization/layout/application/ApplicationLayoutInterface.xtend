package explorviz.visualization.layout.application

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.main.MathHelpers
import java.util.ArrayList
import java.util.List
//import java.util.HashMap

import explorviz.visualization.engine.Loggingimport explorviz.shared.model.helper.Bounds
import explorviz.visualization.layout.datastructures.quadtree.QuadTree

class ApplicationLayoutInterface {

	public val static insetSpace = 2.0f
	public val static labelInsetSpace = 8.0f

	public val static externalPortsExtension = new Vector3f(3f, 3.5f, 3f)

	val static clazzWidth = 2.0f

	val static floorHeight = 0.75f

	val static clazzSizeDefault = 0.05f
	val static clazzSizeEachStep = 1.1f

	val static pipeSizeDefault = 0.05f
	val static pipeSizeEachStep = 0.32f

	val static comp = new ComponentAndClassComparator()

	def static applyLayout(Application application) throws LayoutException {
		var foundationComponent = application.components.get(0)
		// list contains only 1 Element, 
		//	root/application contains itself as the most outer component
		
		
		calcClazzHeight(foundationComponent)
		initNodes(foundationComponent)
//		foundationComponent.width = foundationComponent.width + labelInsetSpace
		foundationComponent.positionX = 0f
		foundationComponent.positionY = 0f
        createQuadTree(foundationComponent)
//		doLayout(foundationComponent)
//		setAbsoluteLayoutPosition(foundationComponent)
		////////////////////
		// Log Component Tree
		val stringList = new ArrayList<String>()
		componentTreeToString(foundationComponent, stringList)
		var s = "Component-Tree:\n"
		for(component : stringList) {
			s = s + component + "\n"
		}
		//Logging.log(s)
				Logging.log("foundationElement name: "+ foundationComponent.name)
		///////////////////////////		
		layoutEdges(application)

		application.incomingCommunications.forEach [
			layoutIncomingCommunication(it, application.components.get(0))
		]

		application.outgoingCommunications.forEach [
			layoutOutgoingCommunication(it, application.components.get(0))
		]
		
		application
	}


	def private static void componentTreeToString(Component component, ArrayList<String> stringList) {
		stringList.add(" \n Component " + component.name)
		component.clazzes.forEach [
			stringList.add(it.name + " is class of " + component.name + " width: " +it.width + " Cords: "+it.positionX +":"+positionY)
		]
		component.children.forEach [
			stringList.add(it.name + " is child-component of " + component.name + " width: " +it.width +  " Cords: "+it.positionX +":"+positionY)
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
			it.height = clazzSizeEachStep * categories.get(it.instanceCount)
			 + clazzSizeDefault
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

//	def private static applyMetrics(Component component) {
//		component.height = getHeightOfComponent(component)
//		component.width = -1f
//		component.depth = -1f
//}
	def private static void applyMetrics(Component component) {
		component.height = getHeightOfComponent(component)
		var float size = 0f;
		
		component.children.forEach [
			applyMetrics(it)
		]
		
		size = calculateSize(component) + Math.sqrt(component.clazzes.size * calculateArea(clazzWidth+3f, clazzWidth+3f).doubleValue).floatValue
		 
					
		if(component.children.size < 2) {
			if(component.children.size == 1) {
				size += component.children.get(0).width + 10f	
			}
		} else if(component.children.size > 1) {
 			val Component biggestLooser = biggestLooser(component.children)
			if(size < 2f * biggestLooser.width) {
				size = 2.3f * (biggestLooser.width +  Math.sqrt(component.clazzes.size * calculateArea(clazzWidth+3f, clazzWidth+3f).doubleValue).floatValue)
			}
			
		} else {
		}
		
		component.width = size
		component.depth = size
		
	
	}
	
	def private static float calculateSize(Component component) {
		var float size = 0f
		
		for(Component comp : component.children) {
			size += calculateSize(comp)
		}
		
			size = Math.sqrt(component.clazzes.size * calculateArea(clazzWidth+3f, clazzWidth+3f).doubleValue).floatValue	
		return size
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

	def private static void doTreeLayout(Component component) {
		component.children.forEach [
			doTreeLayout(it)
		]
		
		createQuadTree(component)
	}


	def private static void createQuadTree(Component component) {
		component.width = component.width +labelInsetSpace
		component.depth = component.depth +labelInsetSpace
		if(!component.children.empty) {
			component.children.sortInplace(comp)	
		}
		val QuadTree quad = new QuadTree(0, new Bounds(component.positionX, component.positionZ, component.width, component.depth))
		
		component.children.forEach [
						it.depth = it.depth+labelInsetSpace
						it.width = it.width+labelInsetSpace
						it.positionX = it.positionX+labelInsetSpace
			
			quad.insert(quad, it)
			
			it.width = it.width+labelInsetSpace
			it.positionX = it.positionX + labelInsetSpace
			it.positionY = it.positionY + component.positionY
			if (component.opened) {
				it.positionY = it.positionY + component.height
			}
			
			createQuadTree(it)
		]
		
				component.clazzes.forEach [
			quad.insert(quad,it)
			it.positionY = it.positionY + component.positionY
			
			if (component.opened) {
				it.positionY = it.positionY + component.height
			}
		]
	}
	
	def private static Component biggestLooser(ArrayList<Component> objects) {
		var Component biggy = objects.get(0);

		for (i : 0 ..< (objects.size() - 1)) {
			if (calculateArea(objects.get(i).width, objects.get(i).depth) < calculateArea(objects.get(i+1).width, objects.get(i+1).depth)) {
				if (calculateArea(biggy.width, biggy.depth) < calculateArea(objects.get(i + 1).width, objects.get(i+1).depth)) {
					biggy = objects.get(i + 1)
				}
			} else if (calculateArea(objects.get(i).width, objects.get(i).height) > calculateArea(biggy.width, biggy.height)) {
				biggy = objects.get(i)
			}
		}

		return biggy
	}
	
	def private static void doLayout(Component component) {
		component.children.forEach [
			doLayout(it)
		]

		layoutChildren(component)
	}

	def private static layoutChildren(Component component) {
		val tempList = new ArrayList<Draw3DNodeEntity>()
		tempList.addAll(component.clazzes)
		tempList.addAll(component.children)

		val segment = layoutGeneric(tempList, component.opened)

		component.width = segment.width
		component.depth = segment.height
	}

	def private static layoutGeneric(List<Draw3DNodeEntity> children, boolean openedComponent) {
		val rootSegment = createRootSegment(children)

		var maxX = 0f
		var maxZ = 0f

		children.sortInplace(comp)

		for (child : children) {
			val childWidth = (child.width + insetSpace * 2)
			val childHeight = (child.depth + insetSpace * 2)
			child.positionY = 0f

			val foundSegment = rootSegment.insertFittingSegment(childWidth, childHeight)

			child.positionX = foundSegment.startX + insetSpace
			child.positionZ = foundSegment.startZ + insetSpace

			if (foundSegment.startX + childWidth > maxX) {
				maxX = foundSegment.startX + childWidth
			}
			if (foundSegment.startZ + childHeight > maxZ) {
				maxZ = foundSegment.startZ + childHeight
			}
		}

		rootSegment.width = maxX
		rootSegment.height = maxZ

		addLabelInsetSpace(rootSegment, children)
	
		rootSegment
	}

	def static addLabelInsetSpace(LayoutSegment segment, List<Draw3DNodeEntity> entities) {
		entities.forEach [
			it.positionX = it.positionX + labelInsetSpace
		]

		segment.width = segment.width + labelInsetSpace
	}

	private def static createRootSegment(List<Draw3DNodeEntity> children) {
		var worstCaseWidth = 0f
		var worstCaseHeight = 0f

		for (child : children) {
			worstCaseWidth = worstCaseWidth + (child.width + insetSpace * 2)
			worstCaseHeight = worstCaseHeight + (child.depth + insetSpace * 2)
		}

		val rootSegment = new LayoutSegment()
		rootSegment.startX = 0f
		rootSegment.startZ = 0f

		rootSegment.width = worstCaseWidth
		rootSegment.height = worstCaseHeight
		
		/*
		val s = "\n\n\n" + "HIER" 
					+ "\n toString: " + rootSegment.toString()
					+ "\n startX: " + rootSegment.startX
					+ "\n startZ: " + rootSegment.startZ
					+ "\n width: " + rootSegment.width
 					+ "\nENDE\n\n\n"; 
		Logging.log(s)
		*/
		
		rootSegment
	}

	def private static void setAbsoluteLayoutPosition(Component component) {
		component.children.forEach [
			it.positionX = it.positionX + component.positionX
			it.positionY = it.positionY + component.positionY
			if (component.opened) {
				it.positionY = it.positionY + component.height
			}
			it.positionZ = it.positionZ + component.positionZ
			setAbsoluteLayoutPosition(it)
		]

		component.clazzes.forEach [
			it.positionX = it.positionX + component.positionX
			it.positionY = it.positionY + component.positionY
			if (component.opened) {
				it.positionY = it.positionY + component.height
			}
			it.positionZ = it.positionZ + component.positionZ
		]
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

					newCommu.points.add(start)
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

	private def static calculatePipeSizeFromQuantiles(Application application) {
		val requestsList = new ArrayList<Integer>
		gatherRequestsIntoList(application, requestsList)

		val categories = MathHelpers::getCategoriesForCommunication(requestsList)

		application.communicationsAccumulated.forEach [
			it.pipeSize = categories.get(it.requests) * pipeSizeEachStep + pipeSizeDefault
		]

		application.incomingCommunications.forEach [
			it.lineThickness = categories.get(it.requests) * pipeSizeEachStep + pipeSizeDefault
		]

		application.outgoingCommunications.forEach [
			requestsList.add(it.requests)
			it.lineThickness = categories.get(it.requests) * pipeSizeEachStep + pipeSizeDefault
		]
	}

	private def static gatherRequestsIntoList(Application application, ArrayList<Integer> requestsList) {
		application.communicationsAccumulated.forEach [
			requestsList.add(it.requests)
		]

		application.incomingCommunications.forEach [
			requestsList.add(it.requests)
		]

		application.outgoingCommunications.forEach [
			requestsList.add(it.requests)
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
}
