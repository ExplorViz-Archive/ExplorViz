package explorviz.shared.model

import java.util.ArrayList
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.math.Vector4f

class Component extends Draw3DNodeEntity {
    @Property var String name
    @Property var String fullQualifiedName
    @Property var boolean synthetic = false
    
    @Property var children = new ArrayList<Component>
    @Property var clazzes = new ArrayList<Clazz>
    
	@Property var Component parentComponent
	
	@Property Application belongingApplication
	
	@Property Vector4f color
    
	var transient boolean opened
	
	def boolean isOpened() {
	    opened
	}
	
	def void setOpened(boolean openedParam) {
		if (!openedParam) setAllChildrenUnopened()
	    
	   this.opened = openedParam
	}
	
    private def setAllChildrenUnopened() {
        children.forEach[
        	it.setOpened(false)
        ]
    }
	
	override void destroy() {
//		children.forEach [it.destroy()]
//		clazzes.forEach [it.destroy()]
//	    super.destroy()
	}
	
	def void clearAllPrimitiveObjects() {
//		this.primitiveObjects.clear()
//		
//		children.forEach [it.clearAllPrimitiveObjects()]
//		clazzes.forEach [it.clearAllPrimitiveObjects()]
	}
}