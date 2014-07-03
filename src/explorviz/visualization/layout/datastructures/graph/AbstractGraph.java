/*
 * Created on Apr 2, 2006
 *
 * Copyright (c) 2006, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package explorviz.visualization.layout.datastructures.graph;

import java.io.Serializable;
import java.util.*;

import explorviz.visualization.layout.datastructures.hypergraph.EdgeType;

/**
 * Abstract implementation of the <code>Graph</code> interface. Designed to
 * simplify implementation of new graph classes.
 * 
 * @author Joshua O'Madadhain
 */
@SuppressWarnings("serial")
public abstract class AbstractGraph<V, E> implements Graph<V, E>, Serializable {
	public boolean addEdge(final E edge, final Collection<? extends V> vertices) {
		return addEdge(edge, vertices, getDefaultEdgeType());
	}

	@SuppressWarnings("unchecked")
	public boolean addEdge(final E edge, final Collection<? extends V> vertices,
			final EdgeType edgeType) {
		if (vertices == null) {
			throw new IllegalArgumentException("'vertices' parameter must not be null");
		}
		if (vertices.size() == 2) {
			return addEdge(edge, vertices instanceof Pair ? (Pair<V>) vertices : new Pair<V>(
					vertices), edgeType);
		} else if (vertices.size() == 1) {
			final V vertex = vertices.iterator().next();
			return addEdge(edge, new Pair<V>(vertex, vertex), edgeType);
		} else {
			throw new IllegalArgumentException(
					"Graph objects connect 1 or 2 vertices; vertices arg has " + vertices.size());
		}
	}

	public boolean addEdge(final E e, final V v1, final V v2) {
		return addEdge(e, v1, v2, getDefaultEdgeType());
	}

	public boolean addEdge(final E e, final V v1, final V v2, final EdgeType edge_type) {
		return addEdge(e, new Pair<V>(v1, v2), edge_type);
	}

	/**
	 * Adds {@code edge} to this graph with the specified {@code endpoints},
	 * with the default edge type.
	 * 
	 * @return {@code} true iff the graph was modified as a result of this call
	 */
	public boolean addEdge(final E edge, final Pair<? extends V> endpoints) {
		return addEdge(edge, endpoints, getDefaultEdgeType());
	}

	/**
	 * Adds {@code edge} to this graph with the specified {@code endpoints} and
	 * {@code EdgeType}.
	 * 
	 * @return {@code} true iff the graph was modified as a result of this call
	 */
	public abstract boolean addEdge(E edge, Pair<? extends V> endpoints, EdgeType edgeType);

	protected Pair<V> getValidatedEndpoints(final E edge, final Pair<? extends V> endpoints) {
		if (edge == null) {
			throw new IllegalArgumentException("input edge may not be null");
		}

		if (endpoints == null) {
			throw new IllegalArgumentException("endpoints may not be null");
		}

		final Pair<V> new_endpoints = new Pair<V>(endpoints.getFirst(), endpoints.getSecond());
		if (containsEdge(edge)) {
			final Pair<V> existing_endpoints = getEndpoints(edge);
			if (!existing_endpoints.equals(new_endpoints)) {
				throw new IllegalArgumentException("edge " + edge
						+ " already exists in this graph with endpoints " + existing_endpoints
						+ " and cannot be added with endpoints " + endpoints);
			} else {
				return null;
			}
		}
		return new_endpoints;
	}

	public int inDegree(final V vertex) {
		return getInEdges(vertex).size();
	}

	public int outDegree(final V vertex) {
		return getOutEdges(vertex).size();
	}

	public boolean isPredecessor(final V v1, final V v2) {
		return getPredecessors(v1).contains(v2);
	}

	public boolean isSuccessor(final V v1, final V v2) {
		return getSuccessors(v1).contains(v2);
	}

	public int getPredecessorCount(final V vertex) {
		return getPredecessors(vertex).size();
	}

	public int getSuccessorCount(final V vertex) {
		return getSuccessors(vertex).size();
	}

	public boolean isNeighbor(final V v1, final V v2) {
		if (!containsVertex(v1) || !containsVertex(v2)) {
			throw new IllegalArgumentException("At least one of these not in this graph: " + v1
					+ ", " + v2);
		}
		return getNeighbors(v1).contains(v2);
	}

	public boolean isIncident(final V vertex, final E edge) {
		if (!containsVertex(vertex) || !containsEdge(edge)) {
			throw new IllegalArgumentException("At least one of these not in this graph: " + vertex
					+ ", " + edge);
		}
		return getIncidentEdges(vertex).contains(edge);
	}

	public int getNeighborCount(final V vertex) {
		if (!containsVertex(vertex)) {
			throw new IllegalArgumentException(vertex + " is not a vertex in this graph");
		}
		return getNeighbors(vertex).size();
	}

	public int degree(final V vertex) {
		if (!containsVertex(vertex)) {
			throw new IllegalArgumentException(vertex + " is not a vertex in this graph");
		}
		return getIncidentEdges(vertex).size();
	}

	public int getIncidentCount(final E edge) {
		final Pair<V> incident = getEndpoints(edge);
		if (incident == null) {
			return 0;
		}
		if (incident.getFirst() == incident.getSecond()) {
			return 1;
		} else {
			return 2;
		}
	}

	public V getOpposite(final V vertex, final E edge) {
		final Pair<V> incident = getEndpoints(edge);
		final V first = incident.getFirst();
		final V second = incident.getSecond();
		if (vertex.equals(first)) {
			return second;
		} else if (vertex.equals(second)) {
			return first;
		} else {
			throw new IllegalArgumentException(vertex + " is not incident to " + edge
					+ " in this graph");
		}
	}

	public E findEdge(final V v1, final V v2) {
		for (final E e : getOutEdges(v1)) {
			if (getOpposite(v1, e).equals(v2)) {
				return e;
			}
		}
		return null;
	}

	public Collection<E> findEdgeSet(final V v1, final V v2) {
		if (!getVertices().contains(v1)) {
			throw new IllegalArgumentException(v1 + " is not an element of this graph");
		}

		if (!getVertices().contains(v2)) {
			throw new IllegalArgumentException(v2 + " is not an element of this graph");
		}

		final Collection<E> edges = new ArrayList<E>();
		for (final E e : getOutEdges(v1)) {
			if (getOpposite(v1, e).equals(v2)) {
				edges.add(e);
			}
		}
		return Collections.unmodifiableCollection(edges);
	}

	public Collection<V> getIncidentVertices(final E edge) {
		final Pair<V> endpoints = getEndpoints(edge);
		final Collection<V> incident = new ArrayList<V>();
		incident.add(endpoints.getFirst());
		incident.add(endpoints.getSecond());

		return Collections.unmodifiableCollection(incident);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Vertices:");
		for (final V v : getVertices()) {
			sb.append(v + ",");
		}
		sb.setLength(sb.length() - 1);
		sb.append("\nEdges:");
		for (final E e : getEdges()) {
			final Pair<V> ep = getEndpoints(e);
			sb.append(e + "[" + ep.getFirst() + "," + ep.getSecond() + "] ");
		}
		return sb.toString();
	}

}
