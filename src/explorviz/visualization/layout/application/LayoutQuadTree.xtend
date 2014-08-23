package explorviz.visualization.layout.application

import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.datastructures.quadtree.QuadTree
import explorviz.shared.model.helper.Bounds
import java.util.ArrayList
import java.util.List

class LayoutQuadTree {

	public val insetSpace = 4.0f
	public val labelInsetSpace = 8.0f
	val floorHeight = 0.75f
	val static comp = new ComponentAndClassComparator()

	new(Component component) {
		createQuadTree(component)
	}

	def private void createQuadTree(Component component) {

		val QuadTree quad = new QuadTree(0,
			new Bounds(component.positionX + labelInsetSpace, component.positionY + floorHeight, component.positionZ,
				component.width - labelInsetSpace, 0, component.depth))

		//		val compi = new RankComperator(graph)
		component.children.sortInplace(comp).reverse

		//		var List<Component> compList = graph.orderComponents(component)
		component.children.forEach [
			quad.insert(quad, it)
			createQuadTree(it)
		]
		
		component.clazzes.forEach [
			quad.insert(quad, it)
		]

		//			if (component.opened) {
		//				pipeGraph.merge(quad.getPipeEdges(quad))
		//			}
		if (quad.nodes.get(0) != null) {
			quad.merge(quad)
			quad.adjustQuadTree(quad)
		}
		
		component.quadTree = quad
		component.adjust
	}
}
