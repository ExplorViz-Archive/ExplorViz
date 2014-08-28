package explorviz.shared.model.helper

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.picking.EventObserver

abstract class Draw3DNodeEntity extends EventObserver implements Comparable<Draw3DNodeEntity> {
	@Property var String name
	@Property var String fullQualifiedName

	@Property transient float width
	@Property transient float height
	@Property transient float depth

	@Property transient float positionX
	@Property transient float positionY
	@Property transient float positionZ
	
	@Property transient Vector3f NP
	@Property transient Vector3f OP
	@Property transient Vector3f SP
	@Property transient Vector3f WP

	var boolean highlighted = false
	
	def boolean isHighlighted() {
		highlighted
	}
	
	def void setHighlighted(boolean highlightedParam) {
		this.highlighted = highlightedParam
	}

	override destroy() {
		super.destroy()
	}

	def getCenterPoint() {
		new Vector3f(this.positionX + this.width / 2f, this.positionY + this.height / 2f,
			this.positionZ + this.depth / 2f)
	}

	def getExtension() {
		new Vector3f(this.width / 2f, this.height / 2f, this.depth / 2f)
	}
	
	def getPosition() {
		new Vector3f(this.positionX, this.positionY, this.positionZ)
	}
	
	def abstract void highlight();
	def abstract void unhighlight();
	def abstract Draw3DNodeEntity deepCopy();
	
	
	
//	def Bounds getBounds() {
//		return new Bounds(positionX, positionY, positionZ, width, height, depth)
//	}
}
