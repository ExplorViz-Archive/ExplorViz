package explorviz.visualization.model.helper

import de.cau.cs.kieler.klay.layered.graph.LEdge
import explorviz.visualization.engine.picking.EventObserver
import java.util.ArrayList
import java.util.List

class DrawEdgeEntity extends EventObserver {
	@Property val List<LEdge> kielerEdgeReferences = new ArrayList<LEdge>
	
	@Property var float lineThickness
	@Property val List<Point> points = new ArrayList<Point>
	
	override void destroy() {
	    super.destroy()
	}
}