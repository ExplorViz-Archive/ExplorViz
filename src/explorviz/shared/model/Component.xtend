package explorviz.shared.model

import explorviz.shared.model.datastructures.quadtree.QuadTree
import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.Logging
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.List

class Component extends Draw3DNodeEntity {
	@Property var String name
	@Property var String fullQualifiedName
	@Property var boolean synthetic = false
	@Property var boolean foundation = false
	@Property QuadTree quadTree
	@Property var children = new ArrayList<Component>
	@Property var List<Component> previousChildren
	@Property var clazzes = new ArrayList<Clazz>
	@Property var List<Clazz> previousClazzes

	@Property Component parentComponent
	@Property List<Draw3DNodeEntity> insertionOrderList = new ArrayList<Draw3DNodeEntity>
	@Property Application belongingApplication

	@Property var Vector4f color

	@Property var Bounds oldBounds

	var boolean opened = false

	def boolean isOpened() {
		opened
	}

	def void setOpened(boolean openedParam) {
		if (!openedParam) setAllChildrenUnopened()

		this.opened = openedParam
	}

	private def setAllChildrenUnopened() {
		children.forEach [
			it.setOpened(false)
		]
	}

	def void openAllComponents() {
		opened = true
		children.forEach[it.openAllComponents()]
	}

	override void destroy() {
		children.forEach[it.destroy()]
		clazzes.forEach[it.destroy()]
		super.destroy()
	}

	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()

		children.forEach[it.clearAllPrimitiveObjects()]
		clazzes.forEach[it.clearAllPrimitiveObjects()]
	}

	def void adjust() {
		positionX = quadTree.bounds.positionX - 4f
		positionZ = quadTree.bounds.positionZ - 4f

		//		positionY = quadTree.bounds.positionY
		width = quadTree.bounds.width + 8f
		depth = quadTree.bounds.depth + 8f
	}

	override void highlight() {
		this.primitiveObjects.forEach [
			it.highlight(ColorDefinitions::highlightColor)
		]
		highlighted = true
	}

	override void unhighlight() {
		if (highlighted) {
			this.primitiveObjects.forEach [
				it.unhighlight()
			]
			highlighted = false
		} else {
			children.forEach[it.unhighlight()]
			clazzes.forEach[it.unhighlight()]
		}
	}
	
	override deepCopy() {
		val Component clone = new Component()	
		clone.name = this.name
		clone.fullQualifiedName = this.fullQualifiedName
		clone.width = this.positionX
		clone.height = this.height
		clone.depth = this.depth
		clone.positionX = this.positionX
		clone.positionY = this.positionY
		clone.positionZ = this.positionZ
		clone.NP = this.NP
		clone.WP = this.WP
		clone.SP = this.SP
		clone.OP = this.OP
		clone.quadTree = this.quadTree
		clone.color = this.color
		clone.oldBounds = this.oldBounds
		
		this.children.forEach [
			clone.children.add(it.deepCopy() as Component)
		]

		this.clazzes.forEach [
			clone.clazzes.add(it.deepCopy() as Clazz)
		]
		
		clone.parentComponent = this.parentComponent
		clone.belongingApplication = this.belongingApplication

		return clone;
	}
	
	def void putOldBounds() {
		this.oldBounds = new Bounds(this.positionX, this.positionY, this.positionZ, this.width, this.height, this.depth)
	}
	
	def void putPreviousLists() {
		val List<Component> prevChildren = new ArrayList<Component>
		val List<Clazz> prevClazzes = new ArrayList<Clazz>
		this.children.forEach [
			prevChildren.add(it.deepCopy as Component)
		]
		
		this.clazzes.forEach [
			prevClazzes.add(it.deepCopy as Clazz)
		]
		
		previousChildren = prevChildren
		previousClazzes = prevClazzes
	}
	
	def void sortChildrenByPrevious() {
		Collections.sort(this.children, new Comparator<Component>() {
			
			override compare(Component o1, Component o2) {
				 return Integer.compare(previousChildren.indexOf(o1), previousChildren.indexOf(o2));
			}
			
			override equals(Object obj) {
				throw new UnsupportedOperationException("TODO: auto-generated method stub")
			}

			
		})
		
		Collections.sort(this.clazzes, new Comparator<Clazz>() {
			
			override compare(Clazz o1, Clazz o2) {
				 return Integer.compare(previousClazzes.indexOf(o1), previousClazzes.indexOf(o2));
			}
			
			override equals(Object obj) {
				throw new UnsupportedOperationException("TODO: auto-generated method stub")
			}

			
		})		
	}
	
	override boolean equals(Object comp) {
   		if(comp instanceof Component) {
   			return comp.name == name && comp.parentComponent == parentComponent
   		}else {        
   			return false
   		}
    }
				
				override compareTo(Draw3DNodeEntity o) {
					throw new UnsupportedOperationException("TODO: auto-generated method stub")
				}
				
}
