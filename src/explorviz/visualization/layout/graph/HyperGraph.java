package explorviz.visualization.layout.graph;

import java.util.List;

public class HyperGraph<V> extends AbstractGraph<V> {
	/** Construct an empty graph */
	public HyperGraph() {
	}

	/** Construct a graph from edges and vertices stored in arrays */
	public HyperGraph(final int[][] edges, final V[] vertices) {
		super(edges, vertices);
	}

	/** Construct a graph from edges and vertices stored in List */
	public HyperGraph(final List<Edge> edges, final List<V> vertices) {
		super(edges, vertices);
	}

	/** Construct a graph for integer vertices , , and edge list */
	public HyperGraph(final List<Edge> edges, final int numberOfVertices) {
		super(edges, numberOfVertices);
	}

	/** Construct a graph from integer vertices , , and edge array */
	public HyperGraph(final int[][] edges, final int numberOfVertices) {
		super(edges, numberOfVertices);
	}
}