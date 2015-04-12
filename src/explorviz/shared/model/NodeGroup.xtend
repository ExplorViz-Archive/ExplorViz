package explorviz.shared.model

import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.Collections

class NodeGroup extends DrawNodeEntity {
	@Accessors List<Node> nodes = new ArrayList<Node>
	
	@Accessors System parent
	
	@Accessors var boolean visible = true
	
	public static val Vector4f plusColor = ColorDefinitions::nodeGroupPlusColor
	public static val Vector4f backgroundColor = ColorDefinitions::nodeGroupBackgroundColor
	
	var boolean opened
	
	def boolean isOpened() {
	    opened
	}
	
	def void setOpened(boolean openedParam) {
		updateName()
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
	
	def void updateName() {
		val names = getAllNames
		Collections.sort(names)
		if (names.size() >= 2) {
			val first = names.get(0)
			val last = names.get(names.size() - 1)
			
			name = first + " - " + last
			return
		} else if (names.size() == 1) {
			name = names.get(0)
		} else {
			name =  "<NO-NAME>"
		}

	}	
	
	private def List<String> getAllNames() {
		val result = new ArrayList<String>()
		for (node : nodes) {
			result.add(node.displayName)
		}
		result
	}
		
	def setAllChildrenVisibility(boolean visiblity) {
        for (node : nodes)
    	   node.visible = visiblity
    }
    
	override void destroy() {
	   for (node : nodes)
		   node.destroy
		   
	    super.destroy()
	}
	
}