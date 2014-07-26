package explorviz.visualization.layout.application

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.visualization.engine.Logging
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.layout.datastructures.quadtree.QuadTree
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.main.MathHelpers
import java.util.ArrayList
import java.util.List
import explorviz.shared.model.helper.Draw3DNodeEntity

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
		foundationComponent.positionX = 0f
		createQuadTree(foundationComponent)
//		addLabelInsetSpace(foundationComponent, 1)
		setAbsoluteLayoutPosition(foundationComponent)
		layoutEdges(application)

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
		component.children.forEach [
			applyMetrics(it)
		]
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

		size = component.clazzes.size * calculateArea(clazzWidth + insetSpace, clazzWidth + insetSpace)

		for (child : component.children) {
			size = size + calculateArea(child.width + insetSpace, child.depth + insetSpace)
		}

		var Draw3DNodeEntity smallestElement = smallestElement(component)
		var int i = 0
		var boolean found = false;
		if (component.children.size > 1) {
			while (found == false) {
				if (size <
					calculateArea(smallestElement.width, smallestElement.depth) *
						calculateArea(Math.pow(2, i).floatValue, Math.pow(2, i).floatValue)) {
					found = true
				} else {
					i = i + 1
				}
			}
			size = (smallestElement.width + insetSpace) * Math.pow(2, i).floatValue
			if (size <= 2 * component.children.get(0).width) {
				size = 2 * (component.children.get(0).width + insetSpace)
			}
		} else if (component.children.size == 1) {
			size = component.children.get(0).width + (2 * insetSpace)
		} else {
			size = component.clazzes.size * (clazzWidth + insetSpace) + labelInsetSpace
		}

		component.width = size
		component.depth = size
	}

	def private static Draw3DNodeEntity smallestElement(Component component) {
		if (!component.clazzes.empty) {
			return component.clazzes.get(0)
		} else {
			if (!component.children.empty) {
				return component.children.last
			}
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
//		if(getMaxDepth(component) > 0) {
//			component.width = component.width + (getMaxDepth(component) * labelInsetSpace)
//		} 
		var float leaveSpace = 0
		
		if(component.children.size == 1) {
			leaveSpace = labelInsetSpace
			component.width = component.width + labelInsetSpace
		}
		val QuadTree quad = new QuadTree(0,
			new Bounds(component.positionX + leaveSpace, component.positionZ, component.width, component.depth))

		component.children.forEach [
			quad.insert(quad, it)
			createQuadTree(it)
		]

		component.clazzes.forEach [
			quad.insert(quad, it)
		]

		if (quad.nodes.get(0) != null) {
			if (emptyQuad(quad.nodes.get(2)) == true && emptyQuad(quad.nodes.get(3)) == true) {
				component.depth = component.depth / 2f + 2 * insetSpace
			}
		} else {
			if (!quad.objects.empty) {
				component.depth = quad.objects.get(0).depth + insetSpace
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

	def static void addLabelInsetSpace(Component component, int level) {
		component.children.forEach [
//			if(level > 0) {
				it.positionX = it.positionX + (level * labelInsetSpace)
//			}
			addLabelInsetSpace(it, level + 1)
		]

		component.clazzes.forEach [
			it.positionX = it.positionX + labelInsetSpace
		]
		
		if(getMaxDepth(component) > 0) {
			component.width = component.width + (getMaxDepth(component) * labelInsetSpace)
		} else {
			component.width = component.width + labelInsetSpace
		}
	}

	def private static void setAbsoluteLayoutPosition(Component component) {
		component.children.forEach [
			it.positionY = it.positionY + component.positionY
			if (component.opened) {
				it.positionY = it.positionY + component.height
			}
			setAbsoluteLayoutPosition(it)
		]

		component.clazzes.forEach [
			it.positionY = it.positionY + component.positionY
			if (component.opened) {
				it.positionY = it.positionY + component.height
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
