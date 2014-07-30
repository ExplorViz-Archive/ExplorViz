package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.shared.model.helper.CommunicationAppAccumulator
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

	
	new(ArrayList<V> vertices, ArrayList<CommunicationAppAccumulator> commu) {
		vertices.addAll(vertices)	
		
		commu.forEach[
			edges.add(new Edge(it.source, it.target))		
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
	
	def Graph<V> getSubgraph(ArrayList<V> vertices) {
		val Graph<V> subGraph = new Graph<V>(vertices)
		
//		for(Edge<V> edge : this.edges) {
////			if(subGraph.vertices.contains(it.source) && subGraph.vertices.contains(it.target)) {
////				subGraph.edges.add(it)
////			}
//		}
		
		return subGraph
	}
	
	override String toString() {
		var String returnString = ""
		
			returnString = returnString + "Vertices: "+vertices + "\n"
			returnString = returnString + "Edges: " + edges +"\n"

		return returnString
	}
	
}