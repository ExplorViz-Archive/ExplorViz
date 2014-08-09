package explorviz.visualization.layout.datastructures.rtree

import explorviz.shared.model.Component
import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.Draw3DNodeEntity
import java.util.ArrayList

class RTreeNode {
	@Property var RTreeNode parent
	@Property var Bounds bounds
	@Property var ArrayList<RTreeNode> entries
	@Property var ArrayList<RTreeNode> children
	
	new (Bounds pBounds) {
		bounds = pBounds
	}
	
	new (Component component) {
		bounds = component.bounds
	}
	
	def boolean hasSpace(Bounds checkBounds) {
		val float area = bounds.width * bounds.depth
		
		var float filledArea = 0f
		
		for(Draw3DNodeEntity entity : entries) {
			filledArea = filledArea + (entity.width * entity.depth)
		}
		return (area - filledArea - entries.size * 4f) > (bounds.width * bounds.depth)
	}
}