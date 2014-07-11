package explorviz.shared.model.helper

import explorviz.visualization.engine.picking.EventObserver
import explorviz.visualization.engine.math.Vector3f

class Draw3DNodeEntity extends EventObserver {
	@Property var String name
	@Property var String fullQualifiedName

	@Property transient float width
	@Property transient float height
	@Property transient float depth

	@Property transient float positionX
	@Property transient float positionY
	@Property transient float positionZ

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
}
