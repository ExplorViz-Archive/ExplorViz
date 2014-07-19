package explorviz.shared.model.helper

import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.picking.EventObserver
import explorviz.visualization.engine.math.Vector3f

abstract class Draw3DEdgeEntity extends EventObserver {
	@Property transient val List<Vector3f> points = new ArrayList<Vector3f>
	@Property transient var float pipeSize

	@Property var EdgeState state = EdgeState.NORMAL

	override void destroy() {
		super.destroy()
	}
}

enum EdgeState {
	NORMAL, TRANSPARENT, SHOW_DIRECTION_IN, SHOW_DIRECTION_OUT, SHOW_DIRECTION_IN_AND_OUT 
}