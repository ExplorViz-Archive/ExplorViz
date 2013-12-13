package explorviz.visualization.layout

import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.ClazzClientSide
import java.util.ArrayList
import java.util.List
import explorviz.visualization.model.helper.Draw3DNodeEntity

class ApplicationLayoutInterface {
	
	val static insetSpace = 0.1f
	
//	val static minSpacing = 0.5f
	
	val static clazzWidth = 0.6f
	
	val static floorHeight = 0.1f
	
    def static applyLayout(ApplicationClientSide application) throws LayoutException {
		val foundationComponent = new ComponentClientSide()
		foundationComponent.setOpened(true)
		foundationComponent.name = "foundation"
		foundationComponent.fullQualifiedName = "foundation"
		
		foundationComponent.children = application.components
		
        addNodes(foundationComponent)
		addEdges(application)
		
		doLayout(foundationComponent)

		foundationComponent.children.forEach[
			setAbsoluteLayoutPosition(it)
		]
		
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
		clazz.height = 2.5f * (clazz.instanceCount / 40f)
		clazz.width = clazzWidth
	}
	
	def private static applyMetrics(ComponentClientSide component) {
		component.height = getHeightOfComponent(component)
		component.width = -1f
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
    
    def private static addEdges(ApplicationClientSide application) {
    	// TODO
    }
    
    def private static void doLayout(ComponentClientSide component) {
    	component.children.forEach [
	    	doLayout(it)
    	]
    	
    	layoutDirectChildren(component)
    }
	
	def private static layoutDirectChildren(ComponentClientSide component) {
		layoutChildren(component)
	}

	def private static layoutChildren(ComponentClientSide component) {
		val tempList = new ArrayList<Draw3DNodeEntity>()
		tempList.addAll(component.clazzes)
		tempList.addAll(component.children)
		
		component.width = layoutGeneric(tempList)
	}
	
	def private static float layoutGeneric(List<Draw3DNodeEntity> children) {
		var countInEachLine = getNextSquaredNumber(children.size())
		
		val widthPerSlot = findMaxWidth(children) + insetSpace * 2
		
		var lineIterator = 0
		var rowIterator = 0
		
		for (child : children) {
			positionChildInSlot(child, lineIterator, rowIterator, widthPerSlot, countInEachLine)
			
			lineIterator = lineIterator + 1
			if (lineIterator == countInEachLine) {
				lineIterator = 0
				rowIterator = rowIterator + 1
			}
		}
		
		widthPerSlot * countInEachLine
	}
	
    def private static getNextSquaredNumber(int size) {
        var result = 0
        while (size > (result * result)) {
            result = result + 1
        }
        result
    }
	
	def private static findMaxWidth(List<Draw3DNodeEntity> components) {
		if (components.empty) return null
		
		var result = components.get(0)
		
		for (component : components) {
			if (result.width < component.width) {
				result = component
			}
		}
		
		result.width
	}

	def private static positionChildInSlot(Draw3DNodeEntity child, int lineIterator, int rowIterator, float widthPerSlot, int maxSlotsInLineOrRow) {
		var relativePosX = 0f
		var relativePosZ = 0f
		
		if (lineIterator == 0) {
			// align to left edge
			relativePosX = 0  + insetSpace
		} else if (lineIterator == maxSlotsInLineOrRow -1) {
			// align to right edge
			relativePosX = widthPerSlot - child.width - insetSpace
		} else {
			// center
			relativePosX = (widthPerSlot - child.width) / 2f
		}
		
		if (rowIterator == 0) {
			// align to upper edge
			relativePosZ = 0 + insetSpace
		} else if (rowIterator == maxSlotsInLineOrRow -1) {
			// align to lower edge
			relativePosZ = widthPerSlot - child.width - insetSpace
		} else {
			// center
			relativePosZ = (widthPerSlot - child.width) / 2f
		}
		
		child.positionX = relativePosX + lineIterator * widthPerSlot
		child.positionY = 0f
		child.positionZ = relativePosZ + rowIterator * widthPerSlot
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
}