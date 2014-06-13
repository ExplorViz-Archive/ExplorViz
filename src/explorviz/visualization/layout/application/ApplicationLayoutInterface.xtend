package explorviz.visualization.layout.application

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.helper.CommunicationAppAccumulator
import explorviz.visualization.model.helper.Draw3DNodeEntity
import java.util.ArrayList
import java.util.List

class ApplicationLayoutInterface {

	public val static insetSpace = 2.0f
	public val static labelInsetSpace = 8.0f

	val static clazzWidth = 2.0f

	val static floorHeight = 0.75f

	val static comp = new ComponentAndClassComparator()

	def static applyLayout(ApplicationClientSide application) throws LayoutException {
		val foundationComponent = application.components.get(0)

		addNodes(foundationComponent)

		doLayout(foundationComponent)
		setAbsoluteLayoutPosition(foundationComponent)

		layoutEdges(application)

		application
	}

	def private static void addNodes(ComponentClientSide component) {
		component.children.forEach [
			addNodes(it)
		]

		component.clazzes.forEach [
			applyMetrics(it)
		]

		applyMetrics(component)
	}

	def private static applyMetrics(ClazzClientSide clazz) {
		clazz.height = 3.0f * (clazz.instanceCount / 40f) // TODO quantiles...

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
		application.communications.forEach [
			val source = if (it.source.parent.opened) it.source else findFirstOpenComponent(it.source.parent)
			val target = if (it.target.parent.opened) it.target else findFirstOpenComponent(it.target.parent)
			if (source != null && target != null && source != target) {
				var found = false
				for (commu : application.communicationsAccumulated) {
					if (found == false) {
						found = ((commu.source == source) && (commu.target == target))

						if (found) {
							commu.requestCount = commu.requestCount + it.requestsPerSecond
							commu.averageResponseTime = Math.max(commu.averageResponseTime, it.averageResponseTime)

						}
					}
				}

				if (found == false) {
					val newCommu = new CommunicationAppAccumulator()
					newCommu.source = source
					newCommu.target = target
					newCommu.requestCount = it.requestsPerSecond
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
		application.communicationsAccumulated.forEach [
			requestsList.add(it.requestCount)
		]
		
		if (requestsList.empty) {
			return
		}

		requestsList.sortInplace
		val int quart = requestsList.size / 4

		val q0 = requestsList.get(0)
		val q25 = if (requestsList.size > 1) requestsList.get(quart) else q0
		val q50 = if (requestsList.size > 2) requestsList.get(quart * 2) else q25
		val q75 = if (requestsList.size > 3) requestsList.get(quart * 3) else q50

		application.communicationsAccumulated.forEach [
			it.pipeSize = getCategoryForCommuincation(it.requestCount, q0, q25, q50, q75) * 0.15f + 0.05f
		]
	}

	def private static int getCategoryForCommuincation(int requestsPerSecond, int q0, int q25, int q50, int q75) {
		if (requestsPerSecond == 0) {
			return 0
		} else if ((0 < requestsPerSecond) && (requestsPerSecond <= q0)) {
			return 1
		} else if ((q0 < requestsPerSecond) && (requestsPerSecond <= q25)) {
			return 2
		} else if ((q25 < requestsPerSecond) && (requestsPerSecond <= q50)) {
			return 3
		} else if ((q50 < requestsPerSecond) && (requestsPerSecond <= q75)) {
			return 4
		} else {
			return 5
		}
	}
}
