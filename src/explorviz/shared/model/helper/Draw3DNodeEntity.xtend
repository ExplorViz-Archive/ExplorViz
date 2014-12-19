package explorviz.shared.model.helper

import explorviz.visualization.engine.picking.EventObserver
import explorviz.visualization.engine.math.Vector3f
import org.eclipse.xtend.lib.annotations.Accessors

abstract class Draw3DNodeEntity extends EventObserver {
	@Accessors var String name
	@Accessors var String fullQualifiedName

	@Accessors transient float width
	@Accessors transient float height
	@Accessors transient float depth

	@Accessors transient float positionX
	@Accessors transient float positionY
	@Accessors transient float positionZ

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
}
