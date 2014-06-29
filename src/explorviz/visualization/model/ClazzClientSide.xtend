package explorviz.visualization.model

import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.model.helper.Draw3DNodeEntity
import explorviz.visualization.renderer.ColorDefinitions

class ClazzClientSide extends Draw3DNodeEntity {
	@Property var int instanceCount = 0
	
	@Property boolean visible = false
	
	public static Vector4f color = ColorDefinitions::clazzColor
	
	@Property ComponentClientSide parent
	
	override void destroy() {
	    super.destroy()
	}
	
	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()
	}
	
}