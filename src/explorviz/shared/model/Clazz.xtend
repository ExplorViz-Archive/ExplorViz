package explorviz.shared.model

import java.util.HashSet
import java.util.Set
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.renderer.ColorDefinitions

class Clazz extends Draw3DNodeEntity {
	@Property var int instanceCount = 0
	@Property val transient Set<Integer> objectIds = new HashSet<Integer>()

	@Property Component parent
	@Property var boolean visible = false

	override void destroy() {
		super.destroy()
	}

	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()
	}

	override void highlight() {
		this.primitiveObjects.forEach [
			it.highlight(ColorDefinitions::highlightColor)
		]
		highlighted = true
	}

	override unhighlight() {
		if (highlighted) {
			this.primitiveObjects.forEach [
				it.unhighlight()
			]
			highlighted = false
		}
	}
	
	override deepCopy() {
		val Clazz clone = new Clazz()	
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
		clone.parent = this.parent
		clone.instanceCount = this.instanceCount

		return clone;
	}

}
