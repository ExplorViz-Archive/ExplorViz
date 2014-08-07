package explorviz.visualization.layout.datastructures.hypergraph;

import java.util.*;

/**
 * A class to perform various operations that involve traversing a graph. This
 * exists to prevent having to pass multiple state variables to recursive calls
 * when performing the traversals. Most operations have a corresponding static
 * method in the Graph class for easier calling.
 * 
 */
public class GraphTraverser<T> {

	/** The graph that this object works on. */
	private final AbstractGraph<T> graph;

	/** A set to track nodes already searched in the current traversal. */
	private final Set<T> searched = new HashSet<T>();

	/** A map used for colorings of the graph. */
	private Map<T, Integer> coloring = null;

	/** A variable to hold the graph resulting from an operation. */
	private AbstractGraph<T> result = null;

	/** A target vertex. */
	private T goal = null;

	/** The next vertex in a traversal (used for walkCycle). */
	private T next = null;

	/** The previous vertex in a traversal (used for walkCycle). */
	private T prev = null;

	/**
	 * Constructor for a GraphTraverser object.
	 */
	public GraphTraverser(final AbstractGraph<T> graph) {
		this.graph = graph;
	}

	/**
	 * Tests whether this graph is bipartite.
	 * 
	 * @return True if it is bipartite.
	 */
	public boolean isBipartite() {
		if (graph.countVertices() == 0) {
			return true;
		}
		coloring = new HashMap<T, Integer>();
		return isBipartite(graph.getVertices().iterator().next(), true);
	}

	/**
	 * Private worker function for isBipartite.
	 * 
	 * @return True if no conflicts were found.
	 */
	private boolean isBipartite(final T v, final boolean color) {
		if (coloring.containsKey(v)) {
			if (!coloring.get(v).equals(color ? 1 : 0)) {
				return false;
			} else {
				return true;
			}
		} else {
			coloring.put(v, color ? 1 : 0);
			boolean bipartite = true;
			for (final T n : graph.getNeighbors(v)) {
				bipartite = bipartite && isBipartite(n, !color);
			}
			return bipartite;
		}
	}

	/**
	 * Walks around a cycle, starting from an arbitrary vertex and going in an
	 * arbitrary direction.
	 * 
	 * @return The next vertex in the walk.
	 */
	public T walkCycle() {
		if (next == null) {
			prev = graph.getVertices().iterator().next();
			next = graph.getNeighbors(prev).iterator().next();
		} else {
			for (final T n : graph.getNeighbors(next)) {
				if (!n.equals(prev)) {
					prev = next;
					next = n;
					break;
				}
			}
		}
		return prev;
	}

	/**
	 * Finds a path between two vertices in the graph..
	 */
	public AbstractGraph<T> findPath(final T start, final T end, final Collection<T> banned) {
		searched.clear();
		searched.addAll(banned);
		result = new AbstractGraph<T>();
		goal = end;
		final boolean pathFound = findPath(start);
		return pathFound ? result : null;
	}

	/**
	 * Private worker function for findPath.
	 */
	private boolean findPath(final T v) {
		searched.add(v);
		for (final T n : graph.getNeighbors(v)) {
			if (n.equals(goal)) {
				result.addEdge(v, n);
				return true;
			} else if (!searched.contains(n)) {
				result.addEdge(v, n);
				final boolean pathFound = findPath(n);
				if (pathFound) {
					return true;
				}
				result.removeEdge(v, n);
			}
		}
		return false;
	}

	/**
	 * Finds an arbitrary cycle in a biconnected graph.
	 */
	public AbstractGraph<T> findCycle() {
		searched.clear();
		result = new AbstractGraph<T>();
		goal = graph.vertices.iterator().next();
		return findCycle(goal);
	}

	/**
	 * Private worker function for findCycle.
	 */
	private AbstractGraph<T> findCycle(final T v) {
		searched.add(v);
		for (final T n : graph.getNeighbors(v)) {
			if (n.equals(goal) && (result.countVertices() > 2)) {
				result.addEdge(v, n);
				return result;
			} else if (!searched.contains(n)) {
				result.addEdge(v, n);
				final AbstractGraph<T> completedCycle = findCycle(n);
				if (completedCycle != null) {
					return completedCycle;
				}
				result.removeEdge(v, n);
			}
		}
		return null;
	}

	/**
	 * Splits the graph into pieces using the given cycle.
	 */
	public Set<AbstractGraph<T>> splitIntoPieces(final AbstractGraph<T> cycle) {
		searched.clear();
		final Set<AbstractGraph<T>> pieces = new HashSet<AbstractGraph<T>>();
		for (final T v : cycle.getVertices()) {
			searched.add(v);
			for (final T n : graph.getNeighbors(v)) {
				if (!searched.contains(n) && !cycle.hasEdge(n, v)) {
					result = new AbstractGraph<T>();
					result.addEdge(v, n);
					makePiece(cycle, n);
					pieces.add(result);
				}
			}
		}
		return pieces;
	}

	/**
	 * Private helper function for splitIntoPieces. Creates a piece (connected
	 * without going through the cycle) of the graph from a cycle and a starting
	 * node.
	 */
	private void makePiece(final AbstractGraph<T> cycle, final T v) {
		if (cycle.hasVertex(v)) {
			return;
		}
		searched.add(v);
		for (final T n : graph.getNeighbors(v)) {
			if (!result.hasEdge(n, v)) {
				result.addEdge(v, n);
				makePiece(cycle, n);
			}
		}
	}

} // GraphTraverser