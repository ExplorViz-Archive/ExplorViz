package explorviz.shared.model.helper

import de.cau.cs.kieler.klay.layered.graph.LEdge
import explorviz.visualization.engine.picking.EventObserver
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.math.Vector3f
import org.eclipse.xtend.lib.annotations.Accessors

abstract class DrawEdgeEntity extends EventObserver {
	@Accessors transient val List<LEdge> kielerEdgeReferences = new ArrayList<LEdge>
	
	@Accessors transient var float lineThickness
	@Accessors transient val List<Point> points = new ArrayList<Point>
	
	@Accessors transient val List<Vector3f> pointsFor3D = new ArrayList<Vector3f>
	
	override void destroy() {
	    super.destroy()
	}
}