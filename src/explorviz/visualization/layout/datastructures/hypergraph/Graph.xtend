package explorviz.visualization.layout.datastructures.hypergraph

import java.util.ArrayList
import java.util.Hashtable

class Graph<V> {
	@Property val ArrayList<V> vertices = new ArrayList<V>() // Store vertices
	@Property val ArrayList<Edge<V>> edges = new ArrayList<Edge<V>>()
	val Hashtable<V, ArrayList<V>> adjMatrix = new Hashtable<V, ArrayList<V>>()
	
	new() {	
	}
	
	new(ArrayList<V> vertices) {
		vertices.clear
		vertices.addAll(vertices)	
	}
	
	new(ArrayList<V> vertices, ArrayList<Edge<V>> edges) {
		vertices.clear
		vertices.addAll(vertices)	
		
		edges.clear
		edges.addAll(edges)
	}
	
	def boolean containsVertex(V vertex) {
		return vertices.contains(vertex)
	}
	
	def boolean containsEdge(Edge<V> edge) {
		return edges.contains(edge)
	}
	
	def void addVertex(V vertex) {
		vertices.add(vertex)
	}
	
	def void removeVertex(V vertex) {
		vertices.remove(vertex)
	}
	
	def void addEdge(Edge<V> edge) {
		edges.add(edge)
	}
	
	def void removeEdge(Edge<V> edge) {
		edges.remove(edge)
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
	
	override String toString() {
		var String returnString = ""
		
			returnString = returnString + "Vertices: "+vertices + "\n"
			returnString = returnString + "Edges: " + edges +"\n"

		return returnString
	}
	
}