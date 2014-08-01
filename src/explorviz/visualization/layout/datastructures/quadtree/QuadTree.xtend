package explorviz.visualization.layout.datastructures.quadtree

import explorviz.shared.model.Component
import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.layout.datastructures.hypergraph.Graph
import explorviz.visualization.layout.datastructures.hypergraph.Graphzahn
import java.util.ArrayList
import java.util.Collection
import explorviz.visualization.layout.datastructures.hypergraph.Edge

class QuadTree {
	@Property var int level
	@Property transient val float insetSpace = 2.0f
	@Property var ArrayList<Draw3DNodeEntity> objects
	@Property var boolean checked = false
	@Property Bounds bounds
	@Property Graphzahn graph
	@Property Vector3f NP
	@Property Vector3f OP
	@Property Vector3f SP
	@Property Vector3f WP
	@Property Vector3f TLC
	@Property Vector3f TRC
	@Property Vector3f BLC
	@Property Vector3f BRC
	
	

	@Property var QuadTree[] nodes
	
	new(int pLevel, Bounds pBounds) {
		level = pLevel
		objects = new ArrayList<Draw3DNodeEntity>()
		bounds = pBounds
		nodes = newArrayOfSize(4)
		setPins()
	}
	
	/*
	 * Splits the node into 4 subnodes
	 */
	def void split() {
		var float pWidth = bounds.width / 2f
		var float pHeight = bounds.depth / 2f
		var float x = bounds.positionX
		var float z = bounds.positionZ

		nodes.set(0, new QuadTree(this.level + 1, new Bounds(x + pWidth, z, pWidth , pHeight)))
		nodes.set(1, new QuadTree(this.level + 1, new Bounds(x, z, pWidth, pHeight)))
		nodes.set(2, new QuadTree(this.level + 1, new Bounds(x, z + pHeight, pWidth , pHeight)))
		nodes.set(3, new QuadTree(this.level + 1, new Bounds(x + pWidth, z + pHeight , pWidth , pHeight)))
	}
	
	def void setPins() {
		NP = new Vector3f(bounds.positionX + bounds.width/2f, bounds.positionY, bounds.positionZ)
		WP = new Vector3f(bounds.positionX + bounds.width, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		SP = new Vector3f(bounds.positionX + bounds.width/2f, bounds.positionY, bounds.positionZ+bounds.depth)
		WP = new Vector3f(bounds.positionX, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		TLC = new Vector3f(bounds.positionX, bounds.positionY, bounds.positionZ)
		TRC = new Vector3f(bounds.positionX + bounds.width, bounds.positionY, bounds.positionZ)
		BLC = new Vector3f(bounds.positionX, bounds.positionY, bounds.positionZ+bounds.depth)
		BRC = new Vector3f(bounds.positionX + bounds.width, bounds.positionY, bounds.positionZ+bounds.depth)
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
		var Bounds rectWithSpace
		
			rectWithSpace = new Bounds(component.width +insetSpace, component.depth +insetSpace)

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
			component.positionX = quad.bounds.positionX + (quad.bounds.width - component.width)/2f
			component.positionZ = quad.bounds.positionZ + (quad.bounds.depth - component.depth)/2f
			quad.objects.add(component)
			return true
		}
	}
	
	def boolean intersectObject(Bounds lookUpArea) {
		var boolean found = false
		
			if(nodes.get(0) == null) {
				if(!objects.empty) {
					if(intersect(lookUpArea, new Bounds(objects.get(0).positionX,objects.get(0).positionZ,objects.get(0).width, objects.get(0).depth)))
					{
						found = true
					}
				}
			} else {
				if(intersect(lookUpArea, nodes.get(0).bounds)) {
					found = nodes.get(0).intersectObject(lookUpArea)
				} else if(intersect(lookUpArea, nodes.get(1).bounds)) {
					found = nodes.get(1).intersectObject(lookUpArea)
				} else if(intersect(lookUpArea, nodes.get(2).bounds)) {
					found = nodes.get(2).intersectObject(lookUpArea)
				} else if(intersect(lookUpArea, nodes.get(3).bounds)) {
					found = nodes.get(3).intersectObject(lookUpArea)
				}
			}
		
		return found
	}
	
	def boolean intersect(Bounds toCheck, Bounds given) {
		return !(toCheck.positionX+toCheck.width < given.positionX || toCheck.positionX > given.positionX+given.width
		|| toCheck.positionZ+toCheck.width < given.positionZ || toCheck.positionZ > given.positionZ+given.width)
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
	
	def Graph<Vector3f> getPipeEdges(QuadTree quad) {
		var Graph<Vector3f> graph = new Graph<Vector3f>()
//		graph.addVertex(new Vector3f(1f,1f,1f))
			if(quad.nodes.get(0) == null) {
				val listPins = #[TLC, TRC,BLC,BRC,NP,OP,SP,WP]
				graph.addVertices(new ArrayList<Vector3f>(listPins))
				graph.addEdge(new Edge<Vector3f>(TLC, NP))
				graph.addEdge(new Edge<Vector3f>(TLC, NP).swapVertices)
				graph.addEdge(new Edge<Vector3f>(NP,TRC))
				graph.addEdge(new Edge<Vector3f>(NP,TRC).swapVertices)
				graph.addEdge(new Edge<Vector3f>(TRC,OP))
				graph.addEdge(new Edge<Vector3f>(TRC,OP).swapVertices)
				graph.addEdge(new Edge<Vector3f>(OP,BRC))
				graph.addEdge(new Edge<Vector3f>(OP,BRC).swapVertices)
				graph.addEdge(new Edge<Vector3f>(BRC,SP))
				graph.addEdge(new Edge<Vector3f>(BRC,SP).swapVertices)
				graph.addEdge(new Edge<Vector3f>(SP,BLC))
				graph.addEdge(new Edge<Vector3f>(SP,BLC).swapVertices)
				graph.addEdge(new Edge<Vector3f>(BLC,WP))
				graph.addEdge(new Edge<Vector3f>(BLC,WP).swapVertices)
				graph.addEdge(new Edge<Vector3f>(WP,TLC))
				graph.addEdge(new Edge<Vector3f>(WP,TLC).swapVertices)
			} else {
				graph.merge(getPipeEdges(quad.nodes.get(0)))
				graph.merge(getPipeEdges(quad.nodes.get(1)))
				graph.merge(getPipeEdges(quad.nodes.get(2)))
				graph.merge(getPipeEdges(quad.nodes.get(3)))
			}
		return graph
	}	
	
}