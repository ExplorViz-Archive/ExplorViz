package explorviz.visualization.layout.datastructures.quadtree

import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.Draw3DNodeEntity
import java.util.ArrayList
import explorviz.shared.model.Component
import explorviz.shared.model.Clazz

class QuadTree {
	@Property var int level
	@Property transient val float leaveSpace = 2f
	@Property var ArrayList<Draw3DNodeEntity> objects;

	@Property Bounds bounds

	@Property var QuadTree[] nodes;
	
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
		var float pHeight = bounds.depth / 2f
		var float x = bounds.positionX
		var float y = bounds.positionZ

		nodes.set(0, new QuadTree(this.level + 1, new Bounds(x + pWidth, y, pWidth , pHeight)))
		nodes.set(1, new QuadTree(this.level + 1, new Bounds(x, y, pWidth , pHeight)))
		nodes.set(2, new QuadTree(this.level + 1, new Bounds(x, y + pHeight , pWidth , pHeight)))
		nodes.set(3, new QuadTree(this.level + 1, new Bounds(x + pWidth , y + pHeight , pWidth , pHeight)))
	}	
	
	def int lookUpQuadrant(Bounds component, Bounds bthBounds, int level) {
		var depth = level;
		var float verticalMidpoint = bthBounds.positionX + (bthBounds.width / 2f)
		var float horizontalMidpoint = bthBounds.positionZ + (bthBounds.depth / 2f)
		var Bounds halfBounds = new Bounds((bthBounds.width / 2f), (bthBounds.depth / 2f))
		if (((component.positionX + component.width) < verticalMidpoint) && ((component.positionZ + component.depth) < horizontalMidpoint)) {
			depth = lookUpQuadrant(component, halfBounds, level + 1)
		}

		depth
	}

	
	def boolean insert(QuadTree quad, Draw3DNodeEntity component) {
		var Bounds rectWithSpace = new Bounds(component.width, component.depth)
		
		//if (haveSpace(quad, rectWithSpace) == false) return false
		if(quad.objects.size > 0) {
			return false
		}
		var rectDepth = lookUpQuadrant(rectWithSpace, new Bounds(quad.bounds.width, quad.bounds.depth), quad.level)
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
	
	def Component reconstruct(Component component) {
		component.children = reconstructComponents(this, component)
		component.clazzes = reconstructClazzes(this, component)
		
		return component;
	}	
	
	def ArrayList<Component> reconstructComponents(QuadTree quad, Component component) {
		val ArrayList<Component> children = new ArrayList<Component>();
		
		this.objects.forEach [
			if(it instanceof Component) children.add(it)
		]
		
		if (quad.nodes.get(0) != null) {
			children.addAll(reconstructComponents(quad.nodes.get(0), component))
			children.addAll(reconstructComponents(quad.nodes.get(1), component))
			children.addAll(reconstructComponents(quad.nodes.get(2), component))
			children.addAll(reconstructComponents(quad.nodes.get(3), component))
		}	
		
		return children			
	}
	
	def ArrayList<Clazz> reconstructClazzes(QuadTree quad, Component component) {
		val ArrayList<Clazz> clazzes = new ArrayList<Clazz>();
		
		this.objects.forEach [
			if(it instanceof Clazz) clazzes.add(it)
		]
		
		if (quad.nodes.get(0) != null) {
			clazzes.addAll(reconstructClazzes(quad.nodes.get(0), component))
			clazzes.addAll(reconstructClazzes(quad.nodes.get(1), component))
			clazzes.addAll(reconstructClazzes(quad.nodes.get(2), component))
			clazzes.addAll(reconstructClazzes(quad.nodes.get(3), component))
		}	
		
		return clazzes			
	}	
	
	def Draw3DNodeEntity getObjectsByName(QuadTree quad, Component component) {
		for(i : 0 ..< quad.objects.size) {
			if(quad.objects.get(i).name.equals(component.name)) return quad.objects.get(i)
		}
		
		if (quad.nodes.get(0) != null) {
			getObjectsByName(quad.nodes.get(0), component)
			getObjectsByName(quad.nodes.get(1), component)
			getObjectsByName(quad.nodes.get(2), component)
			getObjectsByName(quad.nodes.get(3), component)
		}	
		
		return component	
	}
	
	
}