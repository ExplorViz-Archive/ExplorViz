package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.visualization.engine.Logging
import java.util.ArrayList
import java.util.HashMap

class Graph<V> {
	@Property val ArrayList<V> vertices = new ArrayList<V>() // Store vertices
	@Property val ArrayList<Edge<V>> edges = new ArrayList<Edge<V>>()
	val HashMap<V, ArrayList<V>> adjMatrix = new HashMap<V, ArrayList<V>>()
	
	new() {	
	}
	
	new(ArrayList<V> vertices) {
		vertices.addAll(vertices)	
	}

	
	new(ArrayList<V> vertices, ArrayList<Edge<V>> commu) {
		vertices.addAll(vertices)	
		edges.addAll(commu)	
	}
	
	def void addVertex(V vertex) {
		if(!vertices.contains(vertex)) {
			vertices.add(vertex)
		}
	}
	
	def void addVertices(ArrayList<V> pVertices) {
		pVertices.forEach [
			if(!vertices.contains(it)) {
				vertices.add(it)
			}	
		]
	}
	
	def void addEdge(Edge<V> edge) {
		if(!edges.contains(edge)) {
			edges.add(edge)
		}
	}
	
	def void addEdges(ArrayList<Edge<V>> pEdges) {
		pEdges.forEach [
			if(!edges.contains(it)) {
				edges.add(it)
			}	
		]
	}
	
		/** Return the neighbors of the specified vertex */
	def ArrayList<V> getNeighbors(V vertex) {
		var ArrayList<V> neighbors = new ArrayList<V>()
		
		for(Edge<V> edge : edges) {
			if(edge.source == vertex && !neighbors.contains(edge.source)) {
				neighbors.add(edge.source)
			} else if(edge.target == vertex && !neighbors.contains(edge.target)) {
				neighbors.add(edge.target)
			}
		}
		
		return neighbors
	}
	
	def ArrayList<Edge<V>> getEdgesFromVertex(V vertex) {
		val ArrayList<Edge<V>> edgesWithVertex = new ArrayList<Edge<V>>()
		
		edges.forEach [
			if(it.source == vertex || it.target == vertex) {
				edgesWithVertex.add(it)
			}
		]
		return edgesWithVertex
	}
	
	def int countVertices() {
		return vertices.size()
	}
	
	def int countEdges() {
		return edges.size()
	}
	
	def void createAdjacencyMatrix() {
		adjMatrix.clear()
		vertices.forEach [
			adjMatrix.put(it, getNeighbors(it))
		]
	}
	
	def void removeVertexFromMatrix(V vertex) {
		adjMatrix.remove(vertex)
		createAdjacencyMatrix()
	}
	
	def void addVertexToMatrix(V vertex) {
		adjMatrix.put(vertex, getNeighbors(vertex))
	}
	
	def int getWeight(V vertex) {
		return adjMatrix.get(vertex).size
	}
	
	def int getRank(V vertex) {
		var int fullRank = 0
		
		for(V vert : adjMatrix.get(vertex)) {
			fullRank = fullRank + (adjMatrix.get(vert).size-1)
		}
		
		return fullRank
	}
	
	def Graph<V> getSubgraph(ArrayList<V> vertices, ArrayList<Edge<V>> edges) {
		val Graph<V> subGraph = new Graph<V>()
		vertices.forEach [
			if(vertices.contains(it)) {
				subGraph.addVertex(it)
				Logging.log("huhu")
			}
		]
		
		subGraph.vertices.forEach [
			Logging.log("ich bin: " + it)
		]
		
		for(Edge<V> edge : edges) {
			if(subGraph.vertices.contains(edge.source) || subGraph.vertices.contains(edge.target)) {
				subGraph.edges.add(edge)
				Logging.log("mache hier was")	
			}
		}
		
		return subGraph
	}
	
	def void clear() {
		edges.clear()
		vertices.clear()
		adjMatrix.clear()
	}
	
	override String toString() {
		var String returnString = ""
		
			returnString = returnString + "Vertices: "+vertices + "\n"
			returnString = returnString + "Edges: " + edges +"\n"

		return returnString
	}
	
}