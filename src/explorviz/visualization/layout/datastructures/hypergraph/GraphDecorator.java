package explorviz.visualization.layout.datastructures.hypergraph;

import java.io.Serializable;
import java.util.Collection;

import explorviz.visualization.layout.datastructures.hypergraph.util.EdgeType;
import explorviz.visualization.layout.datastructures.hypergraph.util.Pair;

/**
 * An implementation of <code>Graph</code> that delegates its method calls to a
 * constructor-specified <code>Graph</code> instance. This is useful for adding
 * additional behavior (such as synchronization or unmodifiability) to an
 * existing instance.
 */
@SuppressWarnings("serial")
public class GraphDecorator<V, E> implements Graph<V, E>, Serializable {

	protected Graph<V, E> delegate;

	/**
	 * Creates a new instance based on the provided {@code delegate}.
	 * 
	 * @param delegate
	 */
	public GraphDecorator(final Graph<V, E> delegate) {
		this.delegate = delegate;
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#addEdge(java.lang.Object,
	 *      java.util.Collection)
	 */
	public boolean addEdge(final E edge, final Collection<? extends V> vertices) {
		return delegate.addEdge(edge, vertices);
	}

	/**
	 * @see Hypergraph#addEdge(Object, Collection, EdgeType)
	 */
	public boolean addEdge(final E edge, final Collection<? extends V> vertices,
			final EdgeType edge_type) {
		return delegate.addEdge(edge, vertices, edge_type);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#addEdge(java.lang.Object,
	 *      java.lang.Object, java.lang.Object,
	 *      edu.uci.ics.jung.graph.util.EdgeType)
	 */
	public boolean addEdge(final E e, final V v1, final V v2, final EdgeType edgeType) {
		return delegate.addEdge(e, v1, v2, edgeType);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#addEdge(java.lang.Object,
	 *      java.lang.Object, java.lang.Object)
	 */
	public boolean addEdge(final E e, final V v1, final V v2) {
		return delegate.addEdge(e, v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#addVertex(java.lang.Object)
	 */
	public boolean addVertex(final V vertex) {
		return delegate.addVertex(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#isIncident(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean isIncident(final V vertex, final E edge) {
		return delegate.isIncident(vertex, edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#isNeighbor(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean isNeighbor(final V v1, final V v2) {
		return delegate.isNeighbor(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#degree(java.lang.Object)
	 */
	public int degree(final V vertex) {
		return delegate.degree(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#findEdge(java.lang.Object,
	 *      java.lang.Object)
	 */
	public E findEdge(final V v1, final V v2) {
		return delegate.findEdge(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#findEdgeSet(java.lang.Object,
	 *      java.lang.Object)
	 */
	public Collection<E> findEdgeSet(final V v1, final V v2) {
		return delegate.findEdgeSet(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getDest(java.lang.Object)
	 */
	public V getDest(final E directed_edge) {
		return delegate.getDest(directed_edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getEdgeCount()
	 */
	public int getEdgeCount() {
		return delegate.getEdgeCount();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getEdgeCount(EdgeType)
	 */
	public int getEdgeCount(final EdgeType edge_type) {
		return delegate.getEdgeCount(edge_type);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getEdges()
	 */
	public Collection<E> getEdges() {
		return delegate.getEdges();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getEdges(edu.uci.ics.jung.graph.util.EdgeType)
	 */
	public Collection<E> getEdges(final EdgeType edgeType) {
		return delegate.getEdges(edgeType);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getEdgeType(java.lang.Object)
	 */
	public EdgeType getEdgeType(final E edge) {
		return delegate.getEdgeType(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getDefaultEdgeType()
	 */
	public EdgeType getDefaultEdgeType() {
		return delegate.getDefaultEdgeType();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getEndpoints(java.lang.Object)
	 */
	public Pair<V> getEndpoints(final E edge) {
		return delegate.getEndpoints(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getIncidentCount(java.lang.Object)
	 */
	public int getIncidentCount(final E edge) {
		return delegate.getIncidentCount(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getIncidentEdges(java.lang.Object)
	 */
	public Collection<E> getIncidentEdges(final V vertex) {
		return delegate.getIncidentEdges(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getIncidentVertices(java.lang.Object)
	 */
	public Collection<V> getIncidentVertices(final E edge) {
		return delegate.getIncidentVertices(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getInEdges(java.lang.Object)
	 */
	public Collection<E> getInEdges(final V vertex) {
		return delegate.getInEdges(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getNeighborCount(java.lang.Object)
	 */
	public int getNeighborCount(final V vertex) {
		return delegate.getNeighborCount(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getNeighbors(java.lang.Object)
	 */
	public Collection<V> getNeighbors(final V vertex) {
		return delegate.getNeighbors(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getOpposite(java.lang.Object,
	 *      java.lang.Object)
	 */
	public V getOpposite(final V vertex, final E edge) {
		return delegate.getOpposite(vertex, edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getOutEdges(java.lang.Object)
	 */
	public Collection<E> getOutEdges(final V vertex) {
		return delegate.getOutEdges(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getPredecessorCount(java.lang.Object)
	 */
	public int getPredecessorCount(final V vertex) {
		return delegate.getPredecessorCount(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getPredecessors(java.lang.Object)
	 */
	public Collection<V> getPredecessors(final V vertex) {
		return delegate.getPredecessors(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getSource(java.lang.Object)
	 */
	public V getSource(final E directed_edge) {
		return delegate.getSource(directed_edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getSuccessorCount(java.lang.Object)
	 */
	public int getSuccessorCount(final V vertex) {
		return delegate.getSuccessorCount(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getSuccessors(java.lang.Object)
	 */
	public Collection<V> getSuccessors(final V vertex) {
		return delegate.getSuccessors(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getVertexCount()
	 */
	public int getVertexCount() {
		return delegate.getVertexCount();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getVertices()
	 */
	public Collection<V> getVertices() {
		return delegate.getVertices();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#inDegree(java.lang.Object)
	 */
	public int inDegree(final V vertex) {
		return delegate.inDegree(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#isDest(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean isDest(final V vertex, final E edge) {
		return delegate.isDest(vertex, edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#isPredecessor(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean isPredecessor(final V v1, final V v2) {
		return delegate.isPredecessor(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#isSource(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean isSource(final V vertex, final E edge) {
		return delegate.isSource(vertex, edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#isSuccessor(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean isSuccessor(final V v1, final V v2) {
		return delegate.isSuccessor(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#outDegree(java.lang.Object)
	 */
	public int outDegree(final V vertex) {
		return delegate.outDegree(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#removeEdge(java.lang.Object)
	 */
	public boolean removeEdge(final E edge) {
		return delegate.removeEdge(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#removeVertex(java.lang.Object)
	 */
	public boolean removeVertex(final V vertex) {
		return delegate.removeVertex(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#containsEdge(java.lang.Object)
	 */
	public boolean containsEdge(final E edge) {
		return delegate.containsEdge(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#containsVertex(java.lang.Object)
	 */
	public boolean containsVertex(final V vertex) {
		return delegate.containsVertex(vertex);
	}
}
