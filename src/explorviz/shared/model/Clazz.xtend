package explorviz.shared.model

import java.util.HashSet
import java.util.Set
import explorviz.shared.model.helper.Draw3DNodeEntity

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
}