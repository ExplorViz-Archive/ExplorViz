package explorviz.visualization.layout.datastructures.hypergraph;

import java.util.*;

/**
 * Generic class to represent a graph.
 *
 */
public class AbstractGraph<T> extends Graph<T> {

	/** Maps vertices to a ArrayList containing all adjacent vertices. */
	HashMap<T, ArrayList<T>> adjMatrix = new HashMap<T, ArrayList<T>>();
	ArrayList<Edge<T>> edges = new ArrayList<Edge<T>>();
	ArrayList<T> vertices = new ArrayList<T>();
	final List<List<T>> neighbors = new ArrayList<List<T>>();

	/** Default constructor for a graph. */
	public AbstractGraph() {
	}

	public AbstractGraph(final ArrayList<T> vertices) {
		vertices.addAll(vertices);
	}

	/**
	 * Constructs a graph identical to the given graph.
	 */
	public AbstractGraph(final AbstractGraph<T> source) {

		for (final T v : source.getVertices()) {
			for (final T u : source.getNeighbors(v)) {
				this.addEdge(v, u);
			}
		}
	}

	/**
	 * Constructs a graph identical to the given graph.
	 */

	public AbstractGraph(final T[][] connections, final T[] vertices) {

		for (final T[] edge : connections) {
			final T u = edge[0];
			final T v = edge[1];
			this.addEdge(u, v);

		}
		for (final T vertice : vertices) {
			this.vertices.add(vertice);
		}
		createAdjacencyLists(connections, vertices.length);
	}

	public void createAdjacencyLists(final T[][] edges, final int numberOfVertices) {
		// Create a linked list

		for (int i = 0; i < (numberOfVertices); i++) {

			neighbors.add(new ArrayList<T>());
		}
		for (final T[] edge : edges) {
			final T u = edge[0];
			final T v = edge[1];
			neighbors.get((Integer) u).add(v);

		}
	}

	public int[][] getAdjacencyMatrix() {

		final int[][] adjacencyMatrix = new int[adjMatrix.size()][adjMatrix.size()];
		for (int i = 0; i < (neighbors.size()); i++) {
			for (int j = 0; j < (neighbors.get(i).size() - 1); j++) {
				final int v = (Integer) neighbors.get(i).get(j);
				adjacencyMatrix[i][v] = 1;
				adjacencyMatrix[v][i] = 1;

			}
		}
		return adjacencyMatrix;
	}

	// This is only for the test purpose
	// It prints the Matrix in the console
	public void printAdjacencyMatrix() {

		final int[][] array = getAdjacencyMatrix();
		final int rowSize = array.length;
		final int columnSize = array[0].length;
		for (int i = 0; i <= (rowSize - 1); i++) {
			System.out.print("[");
			for (int j = 0; j <= (columnSize - 1); j++) {
				System.out.print(" " + array[i][j]);
			}
			System.out.println(" ]");
		}
		System.out.println();
	}

	// createAdjacencyLists();
	// } // /** Creates an adjacency Map lis
	// public void createAdjacencyLists

	// for (final T k : adjMatrix.keySet(
	// adjMatrix.put(k, getNeighbors(

	// System.out.println(adjMatr
	// }

	/**
	 * @return The number of vertices in the graph.
	 */
	public int countVertices() {
		return super.countVertices();
	}

	/**
	 * @return The number of edges in the graph.
	 */
	public int countEdges() {
		int count = 0;
		for (final ArrayList<T> edges : adjMatrix.values()) {
			count += edges.size();
		}
		return count / 2;
	}

	/** Return the number of vertices in the graph */
	public int getSize() {
		return adjMatrix.size();
	}

	/**
	 * Adds a vertex to the graph.
	 */
	public void addVertex(final T vertex) {
		if (!adjMatrix.containsKey(vertex)) {
			adjMatrix.put(vertex, new ArrayList<T>());
		}
	}

	/**
	 * Calculate the weight of a given vertex
	 */
	public int getWeight(final T vertex) {

		return adjMatrix.get(vertex).size();

	}

	/**
	 * Calculate the rank of a given vertex
	 */
	public int getRank(final T vertex) {
		int fullRank = 0;

		for (final T v : adjMatrix.get(vertex)) {
			fullRank = fullRank + (adjMatrix.get(v).size() - 1);
		}

		return fullRank;
	}

	/**
	 * Adds an undirected edge between two vertices.
	 *
	 */
	public void addEdge(final T u, final T v) {
		addVertex(u);
		addVertex(v);
		adjMatrix.get(u).add(v);
		adjMatrix.get(v).add(u);
	}

	/**
	 * Removes an undirected edge from between two vertices.
	 */
	public void removeEdge(final T v1, final T v2) {
		if (hasEdge(v1, v2) && hasEdge(v2, v1)) {
			adjMatrix.get(v1).remove(v2);
			adjMatrix.get(v2).remove(v1);
			if (adjMatrix.get(v1).size() == 0) {
				adjMatrix.remove(v1);
			}
			if (adjMatrix.get(v2).size() == 0) {
				adjMatrix.remove(v2);
			}
		}
	}

	/**
	 * Gets all the neighbors of a given node.
	 */
	// public ArrayList<T> getNeighbors(final T v) {
	//
	// return adjMatrix.get(v);
	//
	// }

	/**
	 * @param v
	 *            A vertex.
	 * @return The degree (number of neighbors) of the given vertex, or -1 if no
	 *         such vertex exists in this graph.
	 */
	public int getDegree(final T v) {
		if (adjMatrix.containsKey(v)) {
			return adjMatrix.get(v).size();
		} else {
			return -1;
		}
	}

	// /**
	// * @return A ArrayList containing all the vertices of this graph.
	// */
	// public Set<T> getVertices() {
	// return adjMatrix.keySet();
	// }

	/**
	 * @return Whether the given vertex is in this graph.
	 */
	public boolean hasVertex(final T v) {
		return adjMatrix.containsKey(v);
	}

	public ArrayList<T> getVertices() {

		return (ArrayList<T>) super.getVertices();
	}

	/**
	 * @return Whether the given vertex is in this graph.
	 */
	public boolean hasEdge(final T v1, final T v2) {
		return adjMatrix.containsKey(v1) && adjMatrix.get(v1).contains(v2);
	}

	// Static Methods

	/**
	 * Tests whether a connected graph is a cycle.
	 */
	public static <T> boolean isCycle(final AbstractGraph<T> graph) {
		boolean isCycle = graph.countVertices() > 2;
		for (final T v : graph.getVertices()) {
			isCycle = isCycle && (graph.getEdgesFromVertex(v).size() == 2);
		}
		return isCycle;
	}

	/**
	 * Tests whether a connected graph is a path.
	 */
	public static <T> boolean isPath(final AbstractGraph<T> graph) {
		final T start = null;
		int endPoints = 0;
		for (final T v : graph.getVertices()) {
			final int degree = graph.getEdgesFromVertex(v).size();
			if (degree == 1) {
				endPoints++;
			} else if (degree != 2) {
				return false;
			}
		}
		if (endPoints != 2) {
			return false;
		}
		return true;
	}

	/**
	 * Tests whether a connected graph is a bipartite.
	 *
	 */
	public static <T> boolean isBipartite(final AbstractGraph<T> graph) {
		return (new GraphTraverser<T>(graph)).isBipartite();
	}

} // TestPlanarity

