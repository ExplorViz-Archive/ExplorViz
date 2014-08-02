package explorviz.visualization.layout.datastructures.quadtree

import explorviz.shared.model.Component
import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.Logging
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.layout.datastructures.hypergraph.Edge
import explorviz.visualization.layout.datastructures.hypergraph.Graph
import explorviz.visualization.layout.datastructures.hypergraph.Graphzahn
import java.util.ArrayList

class QuadTree {
	@Property var int level
	@Property transient val float insetSpace = 2.0f
	@Property var ArrayList<Draw3DNodeEntity> objects
	@Property var boolean checked = false
	@Property Bounds bounds
	@Property Vector3f NP
	@Property Vector3f OP
	@Property Vector3f SP
	@Property Vector3f WP
	@Property Vector3f CP
	@Property Vector3f TLC
	@Property Vector3f TRC
	@Property Vector3f BLC
	@Property Vector3f BRC
//	@Property val Graph<Vector3f> graph = new Graph<Vector3f>()
	
	

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
		WP = new Vector3f(bounds.positionX + bounds.width/2f, bounds.positionY, bounds.positionZ)
		NP = new Vector3f(bounds.positionX + bounds.width, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		OP = new Vector3f(bounds.positionX + bounds.width/2f, bounds.positionY, bounds.positionZ+bounds.depth)
		SP = new Vector3f(bounds.positionX, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		CP = new Vector3f(bounds.positionX, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		CP = new Vector3f(bounds.positionX+bounds.width/2f, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		BLC = new Vector3f(bounds.positionX, bounds.positionY, bounds.positionZ)
		TLC = new Vector3f(bounds.positionX + bounds.width, bounds.positionY, bounds.positionZ)
		BRC = new Vector3f(bounds.positionX, bounds.positionY, bounds.positionZ+bounds.depth)
		TRC = new Vector3f(bounds.positionX + bounds.width, bounds.positionY, bounds.positionZ+bounds.depth)
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
			component.NP = quad.NP
			component.OP = quad.OP
			component.SP = quad.SP
			component.WP = quad.WP
			
//			Logging.log("NP: "+quad.NP + " OP. "+ quad.OP + " SP: "+quad.SP + " WP: "+quad.WP)
//			graph.addEdge(new Edge<Vector3f>(quad.NP, NP))
//			graph.addEdge(new Edge<Vector3f>(quad.OP, OP))
//			graph.addEdge(new Edge<Vector3f>(quad.SP, SP))
//			graph.addEdge(new Edge<Vector3f>(quad.WP, WP))
//			graph.addEdge(new Edge<Vector3f>(quad.NP, NP).swapVertices)
//			graph.addEdge(new Edge<Vector3f>(quad.OP, OP).swapVertices)
//			graph.addEdge(new Edge<Vector3f>(quad.SP, SP).swapVertices)
//			graph.addEdge(new Edge<Vector3f>(quad.WP, WP).swapVertices)
			
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

		val listPins = #[quad.TLC,quad.TRC,quad.BLC,quad.BRC,quad.NP,quad.OP,quad.SP,quad.WP,quad.CP]
				graph.addVertices(new ArrayList<Vector3f>(listPins))
				graph.addEdge(new Edge<Vector3f>(quad.TLC, quad.NP))
				graph.addEdge(new Edge<Vector3f>(quad.TLC, quad.NP).swapVertices)
				graph.addEdge(new Edge<Vector3f>(quad.NP,quad.TRC))
				graph.addEdge(new Edge<Vector3f>(quad.NP,quad.TRC).swapVertices)
				graph.addEdge(new Edge<Vector3f>(quad.TRC,quad.OP))
				graph.addEdge(new Edge<Vector3f>(quad.TRC,quad.OP).swapVertices)
				graph.addEdge(new Edge<Vector3f>(quad.OP,quad.BRC))
				graph.addEdge(new Edge<Vector3f>(quad.OP,quad.BRC).swapVertices)
				graph.addEdge(new Edge<Vector3f>(quad.BRC,quad.SP))
				graph.addEdge(new Edge<Vector3f>(quad.BRC,quad.SP).swapVertices)
				graph.addEdge(new Edge<Vector3f>(quad.SP,quad.BLC))
				graph.addEdge(new Edge<Vector3f>(quad.SP,quad.BLC).swapVertices)
				graph.addEdge(new Edge<Vector3f>(quad.BLC,quad.WP))
				graph.addEdge(new Edge<Vector3f>(quad.BLC,quad.WP).swapVertices)
				graph.addEdge(new Edge<Vector3f>(quad.WP,quad.TLC))
				graph.addEdge(new Edge<Vector3f>(quad.WP,quad.TLC).swapVertices)
				
				if(quad.objects.empty) {
					graph.addEdge(new Edge<Vector3f>(quad.NP,quad.CP))
					graph.addEdge(new Edge<Vector3f>(quad.NP,quad.CP).swapVertices)
					graph.addEdge(new Edge<Vector3f>(quad.OP,quad.CP))
					graph.addEdge(new Edge<Vector3f>(quad.OP,quad.CP).swapVertices)
					graph.addEdge(new Edge<Vector3f>(quad.WP,quad.CP))
					graph.addEdge(new Edge<Vector3f>(quad.WP,quad.CP).swapVertices)
					graph.addEdge(new Edge<Vector3f>(quad.SP,quad.CP))
					graph.addEdge(new Edge<Vector3f>(quad.SP,quad.CP).swapVertices)	
				}			
			if(quad.nodes.get(0) != null) {
//				Logging.log("Before merge: "+graph.vertices)
				graph.merge(getPipeEdges(quad.nodes.get(0)))
				graph.merge(getPipeEdges(quad.nodes.get(1)))
				graph.merge(getPipeEdges(quad.nodes.get(2)))
				graph.merge(getPipeEdges(quad.nodes.get(3)))
//				Logging.log("After merge: "+graph.vertices)
			}

//			if(quad.nodes.get(0) != null) {
//				graph.merge(quad.getPipeEdges(quad.nodes.get(0)))
//				graph.merge(quad.getPipeEdges(quad.nodes.get(1)))
//				graph.merge(quad.getPipeEdges(quad.nodes.get(2)))
//				graph.merge(quad.getPipeEdges(quad.nodes.get(3)))				
//			}
		return graph
	}	
	
}