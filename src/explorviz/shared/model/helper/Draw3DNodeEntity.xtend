package explorviz.shared.model.helper

import explorviz.visualization.engine.picking.EventObserver
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Box

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

	def createBox(Vector3f viewCenterPoint, Vector4f color) {
		val extensionX = width / 2f
		val extensionY = height / 2f
		val extensionZ = depth / 2f

		val centerX = positionX + extensionX - viewCenterPoint.x
		val centerY = positionY + extensionY - viewCenterPoint.y
		val centerZ = positionZ + extensionZ - viewCenterPoint.z

		new Box(new Vector3f(centerX, centerY, centerZ),
			new Vector3f(extensionX, extensionY, extensionZ), color)
	}
}
