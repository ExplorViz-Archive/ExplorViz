package explorviz.visualization.layout.datastructures.hypergraph;

/*
 * Created on Feb 4, 2007
 *
 * Copyright (c) 2007, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */

import java.io.Serializable;
import java.util.*;

//import org.apache.commons.collections15.Factory;

/**
 * An implementation of <code>Hypergraph</code> that is suitable for sparse
 * graphs and permits parallel edges.
 */
@SuppressWarnings("serial")
public class SetHypergraph<V, H> implements Hypergraph<V, H>, Serializable {
	protected Map<V, Set<H>> vertices; // Map of vertices to incident hyperedge
										// sets
	protected Map<H, Set<V>> edges; // Map of hyperedges to incident vertex sets

	/**
	 * Returns a <code>Factory</code> which creates instances of this class.
	 * 
	 * @param <V>
	 *            vertex type of the hypergraph to be created
	 * @param <H>
	 *            edge type of the hypergraph to be created
	 * @return a <code>Factory</code> which creates instances of this class
	 */

	/*
	 * public static <V, H> Factory<Hypergraph<V, H>> getFactory() { return new
	 * Factory<Hypergraph<V, H>>() { public Hypergraph<V, H> create() { return
	 * new SetHypergraph<V, H>(); } }; }
	 */

	/**
	 * Creates a <code>SetHypergraph</code> and initializes the internal data
	 * structures.
	 */
	public SetHypergraph() {
		vertices = new HashMap<V, Set<H>>();
		edges = new HashMap<H, Set<V>>();
	}

	/**
	 * Adds <code>hyperedge</code> to this graph and connects them to the vertex
	 * collection <code>to_attach</code>. Any vertices in <code>to_attach</code>
	 * that appear more than once will only appear once in the incident vertex
	 * collection for <code>hyperedge</code>, that is, duplicates will be
	 * ignored.
	 * 
	 * @see Hypergraph#addEdge(Object, Collection)
	 */
	public boolean addEdge(final H hyperedge, final Collection<? extends V> to_attach) {
		if (hyperedge == null) {
			throw new IllegalArgumentException("input hyperedge may not be null");
		}

		if (to_attach == null) {
			throw new IllegalArgumentException("endpoints may not be null");
		}

		if (to_attach.contains(null)) {
			throw new IllegalArgumentException("cannot add an edge with a null endpoint");
		}

		final Set<V> new_endpoints = new HashSet<V>(to_attach);
		if (edges.containsKey(hyperedge)) {
			final Collection<V> attached = edges.get(hyperedge);
			if (!attached.equals(new_endpoints)) {
				throw new IllegalArgumentException("Edge " + hyperedge
						+ " exists in this graph with endpoints " + attached);
			} else {
				return false;
			}
		}
		edges.put(hyperedge, new_endpoints);
		for (final V v : to_attach) {
			// add v if it's not already in the graph
			addVertex(v);

			// associate v with hyperedge
			vertices.get(v).add(hyperedge);
		}
		return true;
	}

	/**
	 * @see Hypergraph#addEdge(Object, Collection, EdgeType)
	 */
	public boolean addEdge(final H hyperedge, final Collection<? extends V> to_attach,
			final EdgeType edge_type) {
		if (edge_type != EdgeType.UNDIRECTED) {
			throw new IllegalArgumentException("Edge type for this "
					+ "implementation must be EdgeType.HYPER, not " + edge_type);
		}
		return addEdge(hyperedge, to_attach);
	}

	/**
	 * @see Hypergraph#getEdgeType(Object)
	 */
	public EdgeType getEdgeType(final H edge) {
		if (containsEdge(edge)) {
			return EdgeType.UNDIRECTED;
		} else {
			return null;
		}
	}

	public boolean containsVertex(final V vertex) {
		return vertices.keySet().contains(vertex);
	}

	public boolean containsEdge(final H edge) {
		return edges.keySet().contains(edge);
	}

	public Collection<H> getEdges() {
		return edges.keySet();
	}

	public Collection<V> getVertices() {
		return vertices.keySet();
	}

	public int getEdgeCount() {
		return edges.size();
	}

	public int getVertexCount() {
		return vertices.size();
	}

	public Collection<V> getNeighbors(final V vertex) {
		if (!containsVertex(vertex)) {
			return null;
		}

		final Set<V> neighbors = new HashSet<V>();
		for (final H hyperedge : vertices.get(vertex)) {
			neighbors.addAll(edges.get(hyperedge));
		}
		return neighbors;
	}

	public Collection<H> getIncidentEdges(final V vertex) {
		return vertices.get(vertex);
	}

	public Collection<V> getIncidentVertices(final H edge) {
		return edges.get(edge);
	}

	public H findEdge(final V v1, final V v2) {
		if (!containsVertex(v1) || !containsVertex(v2)) {
			return null;
		}

		for (final H h : getIncidentEdges(v1)) {
			if (isIncident(v2, h)) {
				return h;
			}
		}
		return null;
	}

	public Collection<H> findEdgeSet(final V v1, final V v2) {
		if (!containsVertex(v1) || !containsVertex(v2)) {
			return null;
		}

		final Collection<H> edges = new ArrayList<H>();
		for (final H h : getIncidentEdges(v1)) {
			if (isIncident(v2, h)) {
				edges.add(h);
			}
		}
		return Collections.unmodifiableCollection(edges);
	}

	public boolean addVertex(final V vertex) {
		if (vertex == null) {
			throw new IllegalArgumentException("cannot add a null vertex");
		}
		if (containsVertex(vertex)) {
			return false;
		}
		vertices.put(vertex, new HashSet<H>());
		return true;
	}

	public boolean removeVertex(final V vertex) {
		if (!containsVertex(vertex)) {
			return false;
		}
		for (final H hyperedge : vertices.get(vertex)) {
			edges.get(hyperedge).remove(vertex);
		}
		vertices.remove(vertex);
		return true;
	}

	public boolean removeEdge(final H hyperedge) {
		if (!containsEdge(hyperedge)) {
			return false;
		}
		for (final V vertex : edges.get(hyperedge)) {
			vertices.get(vertex).remove(hyperedge);
		}
		edges.remove(hyperedge);
		return true;
	}

	public boolean isNeighbor(final V v1, final V v2) {
		if (!containsVertex(v1) || !containsVertex(v2)) {
			return false;
		}

		if (vertices.get(v2).isEmpty()) {
			return false;
		}
		for (final H hyperedge : vertices.get(v1)) {
			if (edges.get(hyperedge).contains(v2)) {
				return true;
			}
		}
		return false;
	}

	public boolean isIncident(final V vertex, final H edge) {
		if (!containsVertex(vertex) || !containsEdge(edge)) {
			return false;
		}

		return vertices.get(vertex).contains(edge);
	}

	public int degree(final V vertex) {
		if (!containsVertex(vertex)) {
			return 0;
		}

		return vertices.get(vertex).size();
	}

	public int getNeighborCount(final V vertex) {
		if (!containsVertex(vertex)) {
			return 0;
		}

		return getNeighbors(vertex).size();
	}

	public int getIncidentCount(final H edge) {
		if (!containsEdge(edge)) {
			return 0;
		}

		return edges.get(edge).size();
	}

	public int getEdgeCount(final EdgeType edge_type) {
		if (edge_type == EdgeType.UNDIRECTED) {
			return edges.size();
		}
		return 0;
	}

	public Collection<H> getEdges(final EdgeType edge_type) {
		if (edge_type == EdgeType.UNDIRECTED) {
			return edges.keySet();
		}
		return null;
	}

	public EdgeType getDefaultEdgeType() {
		return EdgeType.UNDIRECTED;
	}

	public Collection<H> getInEdges(final V vertex) {
		return getIncidentEdges(vertex);
	}

	public Collection<H> getOutEdges(final V vertex) {
		return getIncidentEdges(vertex);
	}

	public int inDegree(final V vertex) {
		return degree(vertex);
	}

	public int outDegree(final V vertex) {
		return degree(vertex);
	}

	public V getDest(final H directed_edge) {
		return null;
	}

	public V getSource(final H directed_edge) {
		return null;
	}

	public Collection<V> getPredecessors(final V vertex) {
		return getNeighbors(vertex);
	}

	public Collection<V> getSuccessors(final V vertex) {
		return getNeighbors(vertex);
	}
}
