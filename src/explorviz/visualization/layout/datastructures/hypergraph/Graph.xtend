package explorviz.visualization.layout.datastructures.hypergraph

import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Comparator

class Graph<V> {
	@Property val List<V> vertices = new ArrayList<V>()
	@Property val List<Edge<V>> edges = new ArrayList<Edge<V>>()
	@Property val HashMap<V, List<V>> adjMatrix = new HashMap<V, List<V>>()
	
	new() {	
	}
    
	new(List<V> vertices) {
		vertices.addAll(vertices)	
	}

	
	new(List<V> vertices, List<Edge<V>> commu) {
		vertices.addAll(vertices)	
		edges.addAll(commu)	
	}
	
	def void addVertex(V vertex) {
		if(!vertices.contains(vertex)) {
			vertices.add(vertex)
		}
	}
	
	def void addVertices(List<V> pVertices) {
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
	
	def void addEdges(List<Edge<V>> pEdges) {
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
			if(edge.hasVertex(vertex)) {
				if(!neighbors.contains(edge.getPath(vertex))) neighbors.add(edge.getPath(vertex))
			}
		}
		
		return neighbors
	}
	
	def List<V> getNeighborsFast(V vertex) {
		return adjMatrix.get(vertex)
	}
	
	def ArrayList<Edge<V>> getEdgesFromVertex(V vertex) {
		val ArrayList<Edge<V>> edgesWithVertex = new ArrayList<Edge<V>>()
		
		edges.forEach [
			if(it.source.equals(vertex) || it.target.equals(vertex)) {
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
		vertices.forEach [
			adjMatrix.put(it, getNeighbors(it))
		]
	}
	
	def void removeVertexFromMatrix(V vertex) {
		adjMatrix.remove(vertex)
		createAdjacencyMatrix()
	}
	
	
	def int getWeight(V vertex) {
		if(adjMatrix.get(vertex) != null) {
		return adjMatrix.get(vertex).size
		
		} else {
			return 0
		}
	}
	
	def V getHeighestWeight() {
		var Map.Entry<V, List<V>> maxWeight = null
		for(Map.Entry<V, List<V>> entry : adjMatrix.entrySet) {
			if(maxWeight == null || entry.getValue().size.compareTo(maxWeight.getValue().size) > 0) {
				maxWeight = entry
			}
		}
		
		return maxWeight.key
	}
	
	def List<V> getMNeighborsByWeights(V vertex, int maxNeighbors) {
		adjMatrix.clear
		createAdjacencyMatrix
		
		val List<V> neighbors = adjMatrix.get(vertex)
		
			neighbors.sortInplace(new Comparator<V>() {
				
				override compare(V o1, V o2) {
					getWeight(o1) <=> getWeight(o2)
				}
				
				override equals(Object obj) {
					throw new UnsupportedOperationException("TODO: auto-generated method stub")
				}
				
			})
			
			if(neighbors.size < maxNeighbors) {
				return neighbors
			} else {
				return neighbors.subList(0, maxNeighbors-1)
			}
	}
	
	def int getRank(V vertex) {
		var int fullRank = 0
		
		for(V vert : adjMatrix.get(vertex)) {
			if(adjMatrix.get(vert) != null) {
				fullRank = fullRank + (adjMatrix.get(vert).size-1)
			}
		}
		
		return fullRank
	}
	
	def Graph<V> getSubgraph(List<V> pVertices, List<Edge<V>> edges) {
		val Graph<V> subGraph = new Graph<V>()
		pVertices.forEach [
			if(vertices.contains(it)) {
				subGraph.addVertex(it)
			}
		]
		
		for(Edge<V> edge : edges) {
			if(subGraph.vertices.contains(edge.source) || subGraph.vertices.contains(edge.target)) {
				subGraph.addEdge(edge)
			}
		}
		
		return subGraph
	}
	
	def void clear() {
		edges.clear()
		vertices.clear()
		adjMatrix.clear()
	}
	
	def void merge(Graph<V> pGraph) {
		if(pGraph.vertices != null) {
			addVertices(pGraph.vertices)	
		}
		
		if(pGraph.edges !=null) {
			pGraph.edges.forEach[
//				var boolean insert = false
//				var List<V> neighborsSource = pGraph.getNeighbors(it.source)
//				
//				for(V neighbor : neighborsSource) {
//					if(!pGraph.containsUndirectedEdge(pGraph.edges, neighbor, it.target)) {
//						insert = true
//					}
//				}
//				
//				if(insert)
				 addEdge(it)
			]
		}
	}
	def boolean containsUndirectedEdge(List<Edge<V>> pEdges, V source, V target) {
		return pEdges.contains(new Edge<V>(source, target)) || pEdges.contains(new Edge<V>(target,source))
	}
	
	override String toString() {
		var String returnString = ""
		
			returnString = returnString + "Vertices: "+vertices + "\n"
			returnString = returnString + "Edges: " + edges +"\n"

		return returnString
	}
	
	
}