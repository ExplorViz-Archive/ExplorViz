package explorviz.visualization.model.helper

import de.cau.cs.kieler.klay.layered.graph.LEdge
import explorviz.visualization.engine.picking.EventObserver
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.math.Vector3f

class DrawEdgeEntity extends EventObserver {
	@Property val List<LEdge> kielerEdgeReferences = new ArrayList<LEdge>
	
	@Property var float lineThickness
	@Property val List<Point> points = new ArrayList<Point>
	
	@Property val List<Vector3f> pointsFor3D = new ArrayList<Vector3f>
	
	override void destroy() {
	    super.destroy()
	}
}