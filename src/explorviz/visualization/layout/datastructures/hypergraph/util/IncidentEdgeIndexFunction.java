/*
 * Created on Sep 24, 2005
 *
 * Copyright (c) 2005, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package explorviz.visualization.layout.datastructures.hypergraph.util;

import java.util.*;

import explorviz.visualization.layout.datastructures.hypergraph.Graph;

/**
 * A class which creates and maintains indices for incident edges.
 * 
 * @author Tom Nelson
 * 
 */
public class IncidentEdgeIndexFunction<V, E> implements EdgeIndexFunction<V, E> {
	protected Map<E, Integer> edge_index = new HashMap<E, Integer>();

	private IncidentEdgeIndexFunction() {
	}

	/**
	 * Returns an instance of this type.
	 * 
	 * @param <V>
	 *            the vertex type
	 * @param <E>
	 *            the edge type
	 */
	public static <V, E> IncidentEdgeIndexFunction<V, E> getInstance() {
		return new IncidentEdgeIndexFunction<V, E>();
	}

	/**
	 * Returns the index for the specified edge. Calculates the indices for
	 * <code>e</code> and for all edges parallel to <code>e</code>.
	 */
	public int getIndex(final Graph<V, E> graph, final E e) {
		Integer index = edge_index.get(e);
		if (index == null) {
			final Pair<V> endpoints = graph.getEndpoints(e);
			final V u = endpoints.getFirst();
			final V v = endpoints.getSecond();
			if (u.equals(v)) {
				index = getIndex(graph, e, v);
			} else {
				index = getIndex(graph, e, u, v);
			}
		}
		return index.intValue();
	}

	protected int getIndex(final Graph<V, E> graph, final E e, final V u, final V v) {
		final Collection<E> commonEdgeSet = new HashSet<E>(graph.getIncidentEdges(u));
		int count = 0;
		for (final E other : commonEdgeSet) {
			if (e.equals(other) == false) {
				edge_index.put(other, count);
				count++;
			}
		}
		edge_index.put(e, count);
		return count;
	}

	protected int getIndex(final Graph<V, E> graph, final E e, final V v) {
		final Collection<E> commonEdgeSet = new HashSet<E>();
		for (final E another : graph.getIncidentEdges(v)) {
			final V u = graph.getOpposite(v, another);
			if (u.equals(v)) {
				commonEdgeSet.add(another);
			}
		}
		int count = 0;
		for (final E other : commonEdgeSet) {
			if (e.equals(other) == false) {
				edge_index.put(other, count);
				count++;
			}
		}
		edge_index.put(e, count);
		return count;
	}

	/**
	 * Resets the indices for this edge and its parallel edges. Should be
	 * invoked when an edge parallel to <code>e</code> has been added or
	 * removed.
	 * 
	 * @param e
	 */
	public void reset(final Graph<V, E> graph, final E e) {
		final Pair<V> endpoints = graph.getEndpoints(e);
		getIndex(graph, e, endpoints.getFirst());
		getIndex(graph, e, endpoints.getFirst(), endpoints.getSecond());
	}

	/**
	 * Clears all edge indices for all edges in all graphs. Does not recalculate
	 * the indices.
	 */
	public void reset() {
		edge_index.clear();
	}
}
