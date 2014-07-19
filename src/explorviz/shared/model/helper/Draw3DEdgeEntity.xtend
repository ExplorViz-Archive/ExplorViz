package explorviz.shared.model.helper

import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.picking.EventObserver
import explorviz.visualization.engine.math.Vector3f

abstract class Draw3DEdgeEntity extends EventObserver {
	@Property transient val List<Vector3f> points = new ArrayList<Vector3f>
	@Property transient var float pipeSize
	
	var boolean highlighted = false
	
	def boolean isHighlighted() {
		highlighted
	}
	
	def void setHighlighted(boolean highlightedParam) {
		this.highlighted = highlightedParam
	}
	
	override void destroy() {
	    super.destroy()
	}
	
	def abstract void highlight();
	def abstract void unhighlight();
}