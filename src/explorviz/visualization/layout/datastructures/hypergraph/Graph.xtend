package explorviz.visualization.layout.datastructures.hypergraph

import java.util.ArrayList

class Graph<V, E> {
	val ArrayList<V> vertices = new ArrayList<V>() // Store vertices
	val ArrayList<E> edges = new ArrayList<E>()
	
	new() {	
	}
	
	new(ArrayList<V> vertices) {
		vertices.clear
		vertices.addAll(vertices)	
	}
	
	new(ArrayList<V> vertices, ArrayList<E> edges) {
		vertices.clear
		vertices.addAll(vertices)	
		
		edges.clear
		edges.addAll(edges)
	}
	
	
	def void addVertex(V vertex) {
		vertices.add(vertex)
	}
	
	def void removeVertex(V vertex) {
		vertices.remove(vertex)
	}
	
	def void addEdge(E edge) {
		edges.add(edge)
	}
	
	def void removeEdge(E edge) {
		edges.remove(edge)
	}
	
	
}