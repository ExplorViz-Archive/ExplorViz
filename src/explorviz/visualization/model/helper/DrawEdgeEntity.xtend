package explorviz.visualization.model.helper

import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.picking.EventObserver
import de.cau.cs.kieler.core.kgraph.KEdge

class DrawEdgeEntity extends EventObserver {
	@Property val List<KEdge> kielerEdgeReferences = new ArrayList<KEdge>
	
	@Property val List<Point> points = new ArrayList<Point>
	
	override void destroy() {
	    super.destroy()
	}
}