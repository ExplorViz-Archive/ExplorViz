package explorviz.shared.model

import explorviz.shared.model.datastructures.quadtree.QuadTree
import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList

class Component extends Draw3DNodeEntity {
	@Property var String name
	@Property var String fullQualifiedName
	@Property var boolean synthetic = false
	@Property var boolean foundation = false
	@Property QuadTree quadTree
	@Property var children = new ArrayList<Component>
	@Property var clazzes = new ArrayList<Clazz>

	@Property Component parentComponent

	@Property Application belongingApplication

	@Property var Vector4f color

	@Property var Bounds oldBounds = new Bounds()

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
		positionX = quadTree.bounds.positionX - 8f
		positionZ = quadTree.bounds.positionZ

		//		positionY = quadTree.bounds.positionY
		width = quadTree.bounds.width + 8f
		depth = quadTree.bounds.depth
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
	
	
}
