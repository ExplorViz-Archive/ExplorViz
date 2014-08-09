package explorviz.visualization.layout.datastructures.rtree

import explorviz.shared.model.Component
import explorviz.shared.model.helper.Draw3DNodeEntity

class RTree {
	@Property var RTreeNode root = null
	@Property var int minNodes = 1
	@Property var int maxNodes
		
	new(int pMaxNodes) {
		maxNodes = pMaxNodes
	}
	
	new(Component rootElement) {
		root = new RTreeNode(rootElement)
		maxNodes = rootElement.children.size + rootElement.clazzes.size
		rootElement.children.forEach [
			root.insert(it)
		]
		
		rootElement.clazzes.forEach [
			root.insert(it)
		]
	}
	
	def insert(Draw3DNodeEntity component, RTreeNode rTreeNode) {
		if(rTreeNode.parent == null) {
			rTreeNode.entries.forEach [
				if(it.bounds.overlaps(component.bounds)) {
					insert(component, rTreeNode)
				}
			]
		} else {
			if(rTreeNode.hasSpace(component.bounds) == true) {
				rTreeNode.entries.add(component)
			} else {
				rTreeNode.split(rTreeNode)
			}
		}
	}
	
	def delete(Draw3DNodeEntity component, RTreeNode rTreeNode) {
		if(rTreeNode.level > 0) {
			rTreeNode.entries.forEach [
				if(it.bounds.overlaps(component.bounds)) {
					delete(component, rTreeNode)
					//
					//
				}
			]
		} else {
			rTreeNode.entries.remove(component)
		}
	}
	
	def void split(RTree rTree) {
		
	}
}