package explorviz.visualization.layout.datastructures.hypergraph;

import java.util.HashSet;
import java.util.Set;

/**
 * Class for testing the planarity of a biconnected graph.The algorithm is based
 * on the idea of di Battista et al. upward planarization and inspired by
 * https://github.com/maxbogue/PlanarityTester/tree/master/java retrieved on
 * 02.08.2014
 */
public class PlanarityTest extends Graph<Object> {

	/**
	 * Set containing the symbol strings of length four that indicate a not
	 * interlaced graph.
	 */
	private static final Set<String> notInterlacedSet = makeNIS();

	/** Private method to create the above set. */
	private static Set<String> makeNIS() {
		final Set<String> set = new HashSet<String>();
		final String[] strings = { "xbyb", "bybx", "ybxb", "bxby" };
		for (final String s : strings) {
			set.add(s);
		}
		return set;
	}

	/**
	 * Main method.
	 */
	public static void main(final String[] args) {

		final Integer[] vertices = { 1, 2, 3, 4, 5, 6 };

		// Edge array k3,3
		final Integer[][] edges = { { 1, 4 }, { 1, 5 }, { 1, 6 }, { 2, 4 }, { 2, 5 }, { 2, 6 },
				{ 3, 4 }, { 3, 5 }, { 3, 6 } };

		@SuppressWarnings("unchecked")
		final AbstractGraph<Integer> graph = new AbstractGraph(edges, vertices);
		System.out.println("the graph size is:" + graph.getSize());
		System.out.println("the graph weight of vertex 1 is" + graph.getWeight(1));
		final AbstractGraph<Integer> cycle = (new GraphTraverser<Integer>(graph)).findCycle();
		System.out.println(testPlanarity(graph, cycle) ? "The graph  is planar"
				: "The graph is not planar");

	}

	/**
	 * Splits the graph into pieces using the cycle.
	 * 
	 */
	public static <T> Set<AbstractGraph<T>> splitIntoPieces(final AbstractGraph<T> graph,
			final AbstractGraph<T> cycle) {
		return new GraphTraverser<T>(graph).splitIntoPieces(cycle);
	}

	/**
	 * Adds two graphs together to produce a new graph with every vertex and
	 * edge contained in the original two.
	 * 
	 */
	public static <T> AbstractGraph<T> addGraphs(final AbstractGraph<T> g1,
			final AbstractGraph<T> g2) {
		final AbstractGraph<T> newGraph = new AbstractGraph<T>();
		for (final T v : g1.getVertices()) {
			for (final T u : g1.getNeighbors(v)) {
				newGraph.addEdge(v, u);
			}
		}
		for (final T v : g2.getVertices()) {
			for (final T u : g2.getNeighbors(v)) {
				newGraph.addEdge(v, u);
			}
		}
		return newGraph;
	}

	/**
	 * Subtract one graph from another to produce a subgraph with every edge in
	 * the first graph but not the second, and every remaining vertex with
	 * degree > 0.
	 */
	public static <T> AbstractGraph<T> getSubgraph(final AbstractGraph<T> g1,
			final AbstractGraph<T> g2) {
		final AbstractGraph<T> newGraph = new AbstractGraph<T>(g1);
		for (final T v : g2.getVertices()) {
			for (final T u : g2.getNeighbors(v)) {
				newGraph.removeEdge(v, u);
			}
		}
		return newGraph;
	}

	/**
	 * Tests the planarity of a BICONNECTED graph
	 */

	public static <T> boolean testPlanarity(final AbstractGraph<T> graph,
			final AbstractGraph<T> cycle) {
		if (graph.countEdges() > ((3 * graph.countVertices()) - 6)) {
			return false;
		}
		final Set<AbstractGraph<T>> pieces = splitIntoPieces(graph, cycle);
		for (final AbstractGraph<T> piece : pieces) {
			if (!AbstractGraph.isPath(piece)) { // Don't bother if the piece is
				// a
				// path.

				// Need a starting vertex that is an attachment point between
				// the piece and the cycle.
				T start = null;
				for (final T v : cycle.getVertices()) {
					if (piece.hasVertex(v)) {
						start = v;
						break;
					}
				}

				// Construct the part of the new cycle that is coming from the
				// old cycle.
				final AbstractGraph<T> cycleSegment = new AbstractGraph<T>(cycle);
				T prev = start;

				// Choose an arbitrary direction to traverse the cycle in.
				T curr = cycle.getNeighbors(prev).iterator().next();

				// Remove all the edges between the starting attachment point
				// and the
				// next found attachment point from the cycleSegment graph.
				cycleSegment.removeEdge(prev, curr);
				while (!piece.hasVertex(curr)) {
					for (final T v : cycle.getNeighbors(curr)) {
						if (!v.equals(prev)) {
							prev = curr;
							curr = v;
							break;
						}
					}
					cycleSegment.removeEdge(prev, curr);
				}
				final T end = curr; // end is the next attachment point found.

				// Find a path through the piece connecting the attachment
				// points, but
				// make sure that it doesn't go through a different attachment
				// point.
				final GraphTraverser<T> traverser = new GraphTraverser<T>(piece);
				final AbstractGraph<T> piecePath = traverser.findPath(start, end,
						cycle.getVertices());

				// Construct the new graph and the new cycle accordingly.
				final AbstractGraph<T> pp = addGraphs(cycle, piece);
				final AbstractGraph<T> cp = addGraphs(cycleSegment, piecePath);

				// Recurse using them as parameters.
				final boolean planar = testPlanarity(pp, cp);
				if (!planar) {
					return false;
				}
			}
		}

		// If all the piece/cycle combinations are planar, then test the
		// interlacement.
		final AbstractGraph<Integer> interlacement = new AbstractGraph<Integer>();
		final Object[] pieceArray = pieces.toArray();

		// For each pair of pieces, see if they're interlaced.
		for (int i = 0; i < pieceArray.length; i++) {
			final AbstractGraph<T> x = (AbstractGraph<T>) pieceArray[i];
			for (int j = i + 1; j < pieceArray.length; j++) {
				final AbstractGraph<T> y = (AbstractGraph<T>) pieceArray[j];

				char lastChar = ' '; // Store the last character added to make
										// things easier.
				String symList = ""; // The list of symbols representing the
										// interlacement of the pieces.
				int bCount = 0; // The number of 'b' symbols. Again, to make
								// things easy.

				// Walk around the cycle and construct the symbol list.
				final GraphTraverser<T> traverser = new GraphTraverser<T>(cycle);
				for (int k = 0; k < cycle.countVertices(); k++) {
					final T v = traverser.walkCycle();
					// If a node is in both pieces, then add a 'b'.
					if (x.hasVertex(v) && y.hasVertex(v)) {
						bCount++;
						symList += 'b';
						lastChar = 'b';
						// Else add if it's only in piece and it's not the
						// last symbol added.
					} else if (x.hasVertex(v) && (lastChar != 'x')) {
						symList += 'x';
						lastChar = 'x';
					} else if (y.hasVertex(v) && (lastChar != 'y')) {
						symList += 'y';
						lastChar = 'y';
					}
				}
				// Check for wrap-around adjacency of x's or y's.
				if (((lastChar == 'x') || (lastChar == 'y')) && (symList.charAt(0) == lastChar)) {
					symList = symList.substring(1);
				}
				boolean interlaced = false;
				if ((symList.length() > 4) || (bCount > 2)) {
					interlaced = true;
				} else if ((symList.length() == 4) && !notInterlacedSet.contains(symList)) {
					interlaced = true;
				}
				if (interlaced) {
					interlacement.addEdge(i, j);
				}
			}
		}
		return AbstractGraph.isBipartite(interlacement);
	}

} // TestPlanarity
