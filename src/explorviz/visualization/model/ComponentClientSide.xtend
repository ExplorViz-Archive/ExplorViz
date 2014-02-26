package explorviz.visualization.model

import java.util.ArrayList
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.model.helper.Draw3DNodeEntity

class ComponentClientSide extends Draw3DNodeEntity {
	@Property var children = new ArrayList<ComponentClientSide>
	
	@Property val clazzes = new ArrayList<ClazzClientSide>
	
	@Property ComponentClientSide parentComponent
	
	@Property ApplicationClientSide belongingApplication
	
	@Property Vector4f color
	
	var boolean opened
	
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
		children.forEach [it.destroy()]
		clazzes.forEach [it.destroy()]
	    super.destroy()
	}
	
	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()
		
		children.forEach [it.clearAllPrimitiveObjects()]
		clazzes.forEach [it.clearAllPrimitiveObjects()]
	}

		
}