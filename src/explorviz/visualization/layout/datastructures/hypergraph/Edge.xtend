package explorviz.visualization.layout.datastructures.hypergraph

public class Edge<V> {
	@Property var V source
	@Property var V target
	@Property var int weight

	new(V pSource, V pTarget) {
		this.source = pSource
		this.target = pTarget
		this.weight = 1
	}
	
	new(V pSource, V pTarget, int pWeight) {
		this.source = pSource
		this.target = pTarget
		this.weight = pWeight
	}
	
	def boolean hasVertex(V vertex) {
		return (source.equals(vertex) || target.equals(vertex))
	}
	
	def void increase() {
		weight = weight + 1 
	}
	
	def void decrease() {
		weight = weight - 1 
	}
	
	def Edge<V> swapVertices() {
		return new Edge<V>(target, source)
	}
 	
	override String toString() {
		return "Source: " + source + " Target: " + target
	}
}