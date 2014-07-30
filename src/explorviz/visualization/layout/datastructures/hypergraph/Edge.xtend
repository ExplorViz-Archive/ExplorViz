package explorviz.visualization.layout.datastructures.hypergraph

public class Edge<V> {
	@Property var V source
	@Property var V target

	new(V pSource, V pTarget) {
		this.source = pSource;
		this.target = pTarget
	}
	
	def boolean hasVertex(V vertex) {
		return (source.equals(vertex) || target.equals(vertex))
	}
	
	override String toString() {
		return "Source: " + source + " Target: " + target
	}
}