package explorviz.visualization.layout.datastructures.quadtree

import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.Draw3DNodeEntity
import java.util.ArrayList
import explorviz.shared.model.Component

class QuadTree {
	@Property var int level
	@Property var ArrayList<Draw3DNodeEntity> objects;

	@Property Bounds bounds
	@Property Component foundationComponent

	@Property var QuadTree[] nodes;
	
	new(Component component) {
		level = 0
	}
	new(int pLevel, Bounds pBounds) {
		level = pLevel
		objects = new ArrayList<Draw3DNodeEntity>()
		bounds = pBounds
		nodes = newArrayOfSize(4)
	}
	
	/*
	 * Splits the node into 4 subnodes
	 */
	def void split() {
		var float pWidth = bounds.width / 2f
		var float pHeight = bounds.height / 2f
		var float x = bounds.positionX
		var float y = bounds.positionZ

		nodes.set(0, new QuadTree(this.level + 1, new Bounds(x + pWidth, y, pWidth , pHeight)))
		nodes.set(1, new QuadTree(this.level + 1, new Bounds(x, y, pWidth , pHeight)))
		nodes.set(2, new QuadTree(this.level + 1, new Bounds(x, y + pHeight , pWidth , pHeight)))
		nodes.set(3, new QuadTree(this.level + 1, new Bounds(x + pWidth , y + pHeight , pWidth , pHeight)))
	}	
	
	def int lookUpQuadrant(Bounds component, Bounds bthBounds, int level) {
		var depth = level;
		var double verticalMidpoint = bthBounds.positionX + (bthBounds.width / 2f)
		var double horizontalMidpoint = bthBounds.positionZ + (bthBounds.height / 2f)
		var Bounds halfBounds = new Bounds((bthBounds.width / 2f) as int, (bthBounds.height / 2f) as int)
		if (((component.positionX + component.width) < verticalMidpoint) && ((component.positionZ + component.height) < horizontalMidpoint)) {
			depth = lookUpQuadrant(component, halfBounds, level + 1)
		}

		depth
	}
	
	def boolean insert(QuadTree quad, Draw3DNodeEntity component) {
		var Bounds rectWithSpace = new Bounds(component.width+10, component.height+10)
		
		//if (haveSpace(quad, rectWithSpace) == false) return false
		if(quad.objects.size > 0) {
			return false
		}
		var rectDepth = lookUpQuadrant(rectWithSpace, new Bounds(quad.bounds.width, quad.bounds.height), quad.level)
		if (rectDepth == quad.level && quad.nodes.get(0) != null) return false
		
		if (rectDepth > quad.level) {
			if (quad.nodes.get(0) == null) {
				quad.split()
			}
			if (insert(quad.nodes.get(0), component) == true)
				return true
			else if (insert(quad.nodes.get(1), component) == true)
				return true
			else if (insert(quad.nodes.get(2), component) == true)
				return true
			else if (insert(quad.nodes.get(3), component) == true) 
				return true 
			else 
				return false
		} else {
			if(quad.nodes.get(0) != null) return false
			component.positionX = quad.bounds.positionX
			component.positionZ = quad.bounds.positionZ
			quad.objects.add(component)
			return true
		}
	}
}