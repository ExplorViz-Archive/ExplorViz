package explorviz.visualization.model

import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.model.helper.Draw3DNodeEntity

class ClazzClientSide extends Draw3DNodeEntity {
	@Property var int instanceCount = 0
	
	@Property boolean visible = false
	
	@Property Vector4f color
	
	@Property ComponentClientSide parent
	
	override void destroy() {
	    super.destroy()
	}
	
	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()
	}
	
}