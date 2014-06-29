package explorviz.visualization.layout.application

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.main.MathHelpers
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.model.CommunicationClientSide
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.helper.CommunicationAppAccumulator
import explorviz.visualization.model.helper.Draw3DNodeEntity
import java.util.ArrayList
import java.util.List

class ApplicationLayoutInterface {

	public val static insetSpace = 2.0f
	public val static labelInsetSpace = 8.0f

	public val static externalPortsExtension = new Vector3f(3f, 3.5f, 3f)

	val static clazzWidth = 2.0f

	val static floorHeight = 0.75f

	val static clazzSizeDefault = 0.05f
	val static clazzSizeEachStep = 1.0f
	
	val static pipeSizeDefault = 0.05f
	val static pipeSizeEachStep = 0.3f

	val static comp = new ComponentAndClassComparator()

	def static applyLayout(ApplicationClientSide application) throws LayoutException {
		val foundationComponent = application.components.get(0)

		calcClazzHeight(foundationComponent)
		initNodes(foundationComponent)

		doLayout(foundationComponent)
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

	def private static void calcClazzHeight(ComponentClientSide component) {
		val clazzes = new ArrayList<ClazzClientSide>()
		getClazzList(component, clazzes, true)

		val instanceCountList = new ArrayList<Integer>()
		clazzes.forEach [
			instanceCountList.add(it.instanceCount)
		]

		val categories = MathHelpers::getCategoriesForMapping(instanceCountList)

		clazzes.forEach [
			it.height = clazzSizeEachStep * categories.get(it.instanceCount) + clazzSizeDefault
		]
	}

	def private static void getClazzList(ComponentClientSide component, List<ClazzClientSide> clazzes, boolean beginning) {
		component.children.forEach [
			getClazzList(it, clazzes, false)
		]

		component.clazzes.forEach [
			clazzes.add(it)
		]
	}

	def private static void initNodes(ComponentClientSide component) {
		component.children.forEach [
			initNodes(it)
		]

		component.clazzes.forEach [
			applyMetrics(it)
		]

		applyMetrics(component)
	}

	def private static applyMetrics(ClazzClientSide clazz) {
		clazz.width = clazzWidth
		clazz.depth = clazzWidth
	}

	def private static applyMetrics(ComponentClientSide component) {
		component.height = getHeightOfComponent(component)
		component.width = -1f
		component.depth = -1f
	}

	def private static getHeightOfComponent(ComponentClientSide component) {
		if (!component.opened) {
			var childrenHeight = 0.2f

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

	def private static void doLayout(ComponentClientSide component) {
		component.children.forEach [
			doLayout(it)
		]

		layoutChildren(component)
	}

	def private static layoutChildren(ComponentClientSide component) {
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

		rootSegment
	}

	def private static void setAbsoluteLayoutPosition(ComponentClientSide component) {
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

	def private static layoutEdges(ApplicationClientSide application) {
		application.communicationsAccumulated.forEach [
			it.clearAllPrimitiveObjects
			it.clearAllHandlers
		]
		application.communicationsAccumulated.clear

		application.communications.forEach [
			val source = if (it.source.parent.opened) it.source else findFirstOpenComponent(it.source.parent)
			val target = if (it.target.parent.opened) it.target else findFirstOpenComponent(it.target.parent)
			if (source != null && target != null && source != target) {
				var found = false
				for (commu : application.communicationsAccumulated) {
					if (found == false) {
						found = ((commu.source == source) && (commu.target == target))

						if (found) {
							commu.requests = commu.requests + it.requests
							commu.averageResponseTime = Math.max(commu.averageResponseTime, it.averageResponseTime)

						}
					}
				}

				if (found == false) {
					val newCommu = new CommunicationAppAccumulator()
					newCommu.source = source
					newCommu.target = target
					newCommu.requests = it.requests
					newCommu.averageResponseTime = it.averageResponseTime

					val start = new Vector3f(source.positionX + source.width / 2f, source.positionY + 0.8f,
						source.positionZ + source.depth / 2f)
					val end = new Vector3f(target.positionX + target.width / 2f, target.positionY + 0.8f,
						target.positionZ + target.depth / 2f)

					newCommu.points.add(start)
					newCommu.points.add(end)

					application.communicationsAccumulated.add(newCommu)
				}
			}
		]

		calculatePipeSizeFromQuantiles(application)
	}

	def private static ComponentClientSide findFirstOpenComponent(ComponentClientSide entity) {
		if (entity.parentComponent == null) {
			return null;
		}

		if (entity.parentComponent.opened) {
			return entity
		}

		return findFirstOpenComponent(entity.parentComponent)
	}

	private def static calculatePipeSizeFromQuantiles(ApplicationClientSide application) {
		val requestsList = new ArrayList<Integer>
		gatherRequestsIntoList(application, requestsList)

		val categories = MathHelpers::getCategoriesForMapping(requestsList)

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

	private def static gatherRequestsIntoList(ApplicationClientSide application, ArrayList<Integer> requestsList) {
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

	def private static void layoutIncomingCommunication(CommunicationClientSide commu, ComponentClientSide foundation) {
		val centerCommuIcon = new Vector3f(foundation.positionX - externalPortsExtension.x * 6f,
			foundation.positionY - foundation.extension.y + externalPortsExtension.y,
			foundation.positionZ + foundation.extension.z * 2f - externalPortsExtension.z)

		layoutInAndOutCommunication(commu, commu.targetClazz, centerCommuIcon)
	}

	def private static void layoutOutgoingCommunication(CommunicationClientSide commu, ComponentClientSide foundation) {
		val centerCommuIcon = new Vector3f(foundation.positionX + foundation.extension.x * 2f + externalPortsExtension.x * 4f,
			foundation.positionY - foundation.extension.y + externalPortsExtension.y,
			foundation.positionZ + foundation.extension.z * 2f - externalPortsExtension.z - 12f)

		layoutInAndOutCommunication(commu, commu.sourceClazz, centerCommuIcon)
	}

	def private static void layoutInAndOutCommunication(CommunicationClientSide commu, ClazzClientSide internalClazz,
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
