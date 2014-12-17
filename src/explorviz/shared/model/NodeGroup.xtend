package explorviz.shared.model

import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList
import java.util.List

class NodeGroup extends DrawNodeEntity {
	@Property List<Node> nodes = new ArrayList<Node>
	
	@Property System parent
	
	@Property var boolean visible = true
	
	public static val Vector4f plusColor = ColorDefinitions::nodeGroupPlusColor
	public static val Vector4f backgroundColor = ColorDefinitions::nodeGroupBackgroundColor
	
	var boolean opened
	
	def boolean isOpened() {
	    opened
	}
	
	def void setOpened(boolean openedParam) {
	    if (openedParam) {
	       setAllChildrenVisibility(true)
	    } else {
	       setAllChildrenVisibility(false)
	       if (nodes.size() > 0) {
	          val firstNode = nodes.get(0)
    	      firstNode.visible = true
	       }
	    }
	    
	    this.opened = openedParam
	}
	
	def setAllChildrenVisibility(boolean visiblity) {
        nodes.forEach [
    	   it.visible = visiblity
    	]
    }
    
	override void destroy() {
		nodes.forEach [it.destroy()]
	    super.destroy()
	}
	
}