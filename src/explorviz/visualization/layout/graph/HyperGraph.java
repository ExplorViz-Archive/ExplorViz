package explorviz.visualization.layout.graph;

import java.util.ArrayList;
import java.util.List;

public class HyperGraph<V, E> extends AbstractGraph<V, E> {
	/** Construct an empty graph */
	public HyperGraph() {
	}

	/** Construct a graph from edges and vertices stored in arrays */
	public HyperGraph(final E[][] edges, final V[] vertices) {
		super(edges, vertices);
	}

	/** Construct a graph from edges and vertices stored in List */
	public HyperGraph(final List<Edge> edges, final List<V> vertices) {
		super(edges, vertices);
	}

	@Override
	public void createAdjacencyLists(final E[][] edges, final int numberOfVertices) {
		// Create a linked list

		for (int i = 0; i < numberOfVertices; i++) {
			neighbors.add(new ArrayList<E>());
		}
		for (final E[] edge : edges) {
			final E u = edge[0];
			final E v = edge[1];

			neighbors.get((Integer) u).add(v);

		}
	}

}