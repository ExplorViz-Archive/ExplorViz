package explorviz.visualization.layout.datastructures.quadtree

import explorviz.shared.model.Component
import explorviz.shared.model.helper.Bounds
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.layout.datastructures.hypergraph.Edge
import explorviz.visualization.layout.datastructures.hypergraph.Graph
import explorviz.visualization.layout.datastructures.hypergraph.Vector3fNode
import java.util.ArrayList

class QuadTree {
	@Property var int level
	@Property transient val float insetSpace = 2.0f
	@Property var ArrayList<Draw3DNodeEntity> objects
	@Property var boolean checked = false
	@Property var Bounds bounds
	@Property Vector3fNode NP
	@Property Vector3fNode OP
	@Property Vector3fNode SP
	@Property Vector3fNode WP
	@Property Vector3fNode CP
	@Property Vector3fNode TLC
	@Property Vector3fNode TRC
	@Property Vector3fNode BLC
	@Property Vector3fNode BRC
	@Property val Graph<Vector3fNode> graph = new Graph<Vector3fNode>()
	
	

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
		var float pDepth = bounds.depth / 2f
		var float x = bounds.positionX
		var float z = bounds.positionZ

		nodes.set(0, new QuadTree(this.level + 1, new Bounds(x + pWidth, bounds.positionY, z, pWidth , bounds.height, pDepth)))
		nodes.set(1, new QuadTree(this.level + 1, new Bounds(x,bounds.positionY, z, pWidth,bounds.height, pDepth)))
		nodes.set(2, new QuadTree(this.level + 1, new Bounds(x,bounds.positionY, z + pDepth, pWidth ,bounds.height, pDepth)))
		nodes.set(3, new QuadTree(this.level + 1, new Bounds(x + pWidth,bounds.positionY, z + pDepth , pWidth ,bounds.height, pDepth)))
	}
	
	def void setPins() {
		WP = new Vector3fNode(bounds.positionX + bounds.width/2f, bounds.positionY, bounds.positionZ)
		NP = new Vector3fNode(bounds.positionX + bounds.width, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		OP = new Vector3fNode(bounds.positionX + bounds.width/2f, bounds.positionY, bounds.positionZ+bounds.depth)
		SP = new Vector3fNode(bounds.positionX, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		CP = new Vector3fNode(bounds.positionX, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		CP = new Vector3fNode(bounds.positionX+bounds.width/2f, bounds.positionY, bounds.positionZ+bounds.depth/2f)
		BLC = new Vector3fNode(bounds.positionX, bounds.positionY, bounds.positionZ)
		TLC = new Vector3fNode(bounds.positionX + bounds.width, bounds.positionY, bounds.positionZ)
		BRC = new Vector3fNode(bounds.positionX, bounds.positionY, bounds.positionZ+bounds.depth)
		TRC = new Vector3fNode(bounds.positionX + bounds.width, bounds.positionY, bounds.positionZ+bounds.depth)
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
			component.positionY = quad.bounds.positionY
			component.NP = quad.NP
			component.OP = quad.OP
			component.SP = quad.SP
			component.WP = quad.WP
			quad.objects.add(component)
			
			if(component instanceof Component) {
				if(component.opened) {
			graph.addEdge(new Edge<Vector3fNode>(quad.WP, new Vector3fNode(component.positionX+component.width/2f, component.positionY, component.positionZ)))
			graph.addEdge(new Edge<Vector3fNode>(quad.OP, new Vector3fNode(component.positionX+component.width/2f, component.positionY, component.positionZ+component.depth)))
			graph.addEdge(new Edge<Vector3fNode>(quad.SP, new Vector3fNode(component.positionX+component.width, component.positionY, component.positionZ+component.depth/2f)))
			graph.addEdge(new Edge<Vector3fNode>(quad.NP, new Vector3fNode(component.positionX, component.positionY, component.positionZ+component.width/2f)))
			}
}
			
			return true
		}
	}
	
	def Graph<Vector3fNode> getPipeEdges(QuadTree quad) {
//		var Graph<Vector3f> graph = new Graph<Vector3f>()	
 			val listPins = #[quad.TLC,quad.TRC,quad.BLC,quad.BRC,quad.NP,quad.OP,quad.SP,quad.WP,quad.CP]
			
				graph.addVertices(new ArrayList<Vector3fNode>(listPins))
				graph.addEdge(new Edge<Vector3fNode>(quad.TLC, quad.NP))
				graph.addEdge(new Edge<Vector3fNode>(quad.NP,quad.TRC))
				graph.addEdge(new Edge<Vector3fNode>(quad.TRC,quad.OP))
				graph.addEdge(new Edge<Vector3fNode>(quad.OP,quad.BRC))
				graph.addEdge(new Edge<Vector3fNode>(quad.BRC,quad.SP))
				graph.addEdge(new Edge<Vector3fNode>(quad.SP,quad.BLC))
				graph.addEdge(new Edge<Vector3fNode>(quad.BLC,quad.WP))
				graph.addEdge(new Edge<Vector3fNode>(quad.WP,quad.TLC))	
				graph.addEdge(new Edge<Vector3fNode>(quad.NP,quad.CP))
				graph.addEdge(new Edge<Vector3fNode>(quad.OP,quad.CP))
				graph.addEdge(new Edge<Vector3fNode>(quad.WP,quad.CP))
				graph.addEdge(new Edge<Vector3fNode>(quad.SP,quad.CP))			
			if(quad.nodes.get(0) != null) {

				graph.merge(getPipeEdges(quad.nodes.get(0)))
				graph.merge(getPipeEdges(quad.nodes.get(1)))
				graph.merge(getPipeEdges(quad.nodes.get(2)))
				graph.merge(getPipeEdges(quad.nodes.get(3)))
			}

		return graph
	}	
	
}