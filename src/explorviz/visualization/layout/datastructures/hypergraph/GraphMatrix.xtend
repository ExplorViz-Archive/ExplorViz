package explorviz.visualization.layout.datastructures.hypergraph

import java.util.ArrayList
import java.util.Hashtable

class GraphMatrix<V> {
	val Hashtable<V, ArrayList<V>> adjMatrix = new Hashtable<V, ArrayList<V>>()
	
	new(Graph<V> graph) {
		graph.vertices.forEach [
			var ArrayList<V> neighbors = graph.getNeighbors(it)
			adjMatrix.put(it, neighbors)
		]
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
}