package explorviz.visualization.layout.datastructures.hypergraph

public class Edge<E> {
	var E source
	var E target

	new(E pSource, E pTarget) {
		this.source = pSource;
		this.target = pTarget
	}
}