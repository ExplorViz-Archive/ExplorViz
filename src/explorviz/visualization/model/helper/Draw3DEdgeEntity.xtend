package explorviz.visualization.model.helper

import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.picking.EventObserver
import explorviz.visualization.engine.math.Vector3f

class Draw3DEdgeEntity extends EventObserver {
	@Property val List<Vector3f> points = new ArrayList<Vector3f>
	
	override void destroy() {
	    super.destroy()
	}
}