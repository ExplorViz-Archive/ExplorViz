package explorviz.visualization.layout.graph;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGraph<V> implements Graph<V> {
	protected List<V> vertices = new ArrayList<V>(); // Store vertices
	protected List<List<Integer>> neighbors = new ArrayList<List<Integer>>(); // Adjacency

	// lists

	/** Construct an empty graph */
	protected AbstractGraph() {
	}

	/** Construct a graph from edges and vertices stored in arrays */
	protected AbstractGraph(final int[][] edges, final V[] vertices) {
		for (final V vertice : vertices) {
			this.vertices.add(vertice);
		}

		createAdjacencyLists(edges, vertices.length);
	}

	/** Construct a graph from edges and vertices stored in List */
	protected AbstractGraph(final List<Edge> edges, final List<V> vertices) {
		for (int i = 0; i < vertices.size(); i++) {
			this.vertices.add(vertices.get(i));
		}

		createAdjacencyLists(edges, vertices.size());
	}

	/** Construct a graph for integer vertices , , and edge list */
	protected AbstractGraph(final List<Edge> edges, final int numberOfVertices) {
		for (int i = 0; i < numberOfVertices; i++) {
			vertices.add((V) (new Integer(i))); // vertices is {, , ...}
		}

		createAdjacencyLists(edges, numberOfVertices);
	}

	/** Construct a graph from integer vertices , , and edge array */
	protected AbstractGraph(final int[][] edges, final int numberOfVertices) {
		for (int i = 0; i < numberOfVertices; i++) {
			vertices.add((V) (new Integer(i))); // vertices is {, , ...}
		}

		createAdjacencyLists(edges, numberOfVertices);
	}

	/** Create adjacency lists for each vertex */
	private void createAdjacencyLists(final int[][] edges, final int numberOfVertices) {
		// Create a linked list
		for (int i = 0; i < numberOfVertices; i++) {
			neighbors.add(new ArrayList<Integer>());
		}

		for (final int[] edge : edges) {
			final int u = edge[0];
			final int v = edge[1];
			neighbors.get(u).add(v);
		}
	}

	/** Create adjacency lists for each vertex */
	private void createAdjacencyLists(final List<Edge> edges, final int numberOfVertices) {
		// Create a linked list for each vertex
		for (int i = 0; i < numberOfVertices; i++) {
			neighbors.add(new ArrayList<Integer>());
		}

		for (final Edge edge : edges) {
			neighbors.get(edge.u).add(edge.v);
		}
	}

	@Override
	/** Return the number of vertices in the graph */
	public int getSize() {
		return vertices.size();
	}

	@Override
	/** Return the vertices in the graph */
	public List<V> getVertices() {
		return vertices;
	}

	@Override
	/** Return the object for the specified vertex */
	public V getVertex(final int index) {
		return vertices.get(index);
	}

	@Override
	/** Return the index for the specified vertex object */
	public int getIndex(final V v) {
		return vertices.indexOf(v);
	}

	@Override
	/** Return the neighbors of the specified vertex */
	public List<Integer> getNeighbors(final int index) {
		return neighbors.get(index);
	}

	@Override
	/** Return the degree for a specified vertex */
	public int getDegree(final int v) {
		return neighbors.get(v).size();
	}

	public int[][] getAdjacencyMatrix() {
		final int[][] adjacencyMatrix = new int[getSize()][getSize()];
		for (int i = 0; i < neighbors.size(); i++) {
			for (int j = 0; j < neighbors.get(i).size(); j++) {
				final int v = neighbors.get(i).get(j);
				adjacencyMatrix[i][v] = 1;
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

	public int[] getRank(final int[][] adjMatrix) {

		final int[] rank = new int[adjMatrix.length];

		int rowsumm = 0;
		int i;
		int j;
		for (i = 0; i < adjMatrix[0].length; i++) {
			final List<Integer> neighbors = getNeighbors(i);

			for (j = 0; j < neighbors.size(); j++) {
				rowsumm += (getNeighbors(neighbors.get(j)).size() - 1);
			}
			rank[i] = rowsumm;
			rowsumm = 0;
		}
		System.out.println("The rank is:\t");
		printWeights(rank);
		return rank;
	}

	// Method for translating the matrix
	public int[] calculateColSumm(final int[][] adjMatrix) {

		final int[] temp = new int[adjMatrix.length];

		int spaltensumme = 0;
		int spalte;
		int zeile; // array = new int[2][2];
		for (spalte = 0; spalte < adjMatrix[0].length; spalte++) {
			for (zeile = 0; zeile < adjMatrix.length; zeile++) {
				spaltensumme += adjMatrix[zeile][spalte];
			}
			temp[spalte] = spaltensumme;
			spaltensumme = 0;
		}
		System.out.println("The weights are :");
		printWeights(temp);
		return temp;
	}

	// Method for showing the calculated sum als Array
	public void printWeights(final int[] field) {

		final int size = field.length;
		System.out.print("[");
		for (final int element : field) {
			System.out.print(" " + element);
		}
		System.out.println(" ]");

		System.out.println();
	}

	@Override
	/** Print the edges */
	public void printEdges() {
		for (int u = 0; u < neighbors.size(); u++) {
			System.out.print(getVertex(u) + " (" + u + "): ");
			for (int j = 0; j < neighbors.get(u).size(); j++) {
				System.out.print("(" + u + ", " + neighbors.get(u).get(j) + ") ");
			}
			System.out.println();
		}
	}

	@Override
	/** Clear graph */
	public void clear() {
		vertices.clear();
		neighbors.clear();
	}

	@Override
	/** Add a vertex to the graph */
	public void addVertex(final V vertex) {
		vertices.add(vertex);
		neighbors.add(new ArrayList<Integer>());
	}

	@Override
	/** Add an edge to the graph */
	public void addEdge(final int u, final int v) {
		neighbors.get(u).add(v);
		neighbors.get(v).add(u);
	}

	@Override
	/** Obtain a DFS tree starting from vertex v */
	public Tree dfs(final int v) {
		final List<Integer> searchOrder = new ArrayList<Integer>();
		final int[] parent = new int[vertices.size()];
		for (int i = 0; i < parent.length; i++) {
			parent[i] = -1; // Initialize parent[i] to -
		}

		// Mark visited vertices
		final boolean[] isVisited = new boolean[vertices.size()];

		// Recursively search
		dfs(v, parent, searchOrder, isVisited);

		// Return a search tree
		return new Tree(v, parent, searchOrder);
	}

	/** Recursive method for DFS search */
	private void dfs(final int v, final int[] parent, final List<Integer> searchOrder,
			final boolean[] isVisited) {
		// Store the visited vertex
		searchOrder.add(v);
		isVisited[v] = true; // Vertex v visited

		for (final int i : neighbors.get(v)) {
			if (!isVisited[i]) {
				parent[i] = v; // The parent of vertex i is v
				dfs(i, parent, searchOrder, isVisited); // Recursive search
			}
		}
	}

	@Override
	/** Starting BFS search from vertex v */
	public Tree bfs(final int v) {
		final List<Integer> searchOrder = new ArrayList<Integer>();
		final int[] parent = new int[vertices.size()];
		for (int i = 0; i < parent.length; i++) {
			parent[i] = -1; // Initialize parent[i] to -
		}

		final java.util.LinkedList<Integer> queue = new java.util.LinkedList<Integer>(); // list
		// used
		// as
		// a
		// queue
		final boolean[] isVisited = new boolean[vertices.size()];
		queue.offer(v); // Enqueue v
		isVisited[v] = true; // Mark it visited

		while (!queue.isEmpty()) {
			final int u = queue.poll(); // Dequeue to u
			searchOrder.add(u); // u searched
			for (final int w : neighbors.get(u)) {
				if (!isVisited[w]) {
					queue.offer(w); // Enqueue w
					parent[w] = u; // The parent of w is u
					isVisited[w] = true; // Mark it visited
				}
			}
		}

		return new Tree(v, parent, searchOrder);
	}

	/** Tree inner class inside the AbstractGraph class */
	/** To be discussed in Section . */
	public class Tree {
		private final int root; // The root of the tree
		private final int[] parent; // Store the parent of each vertex
		private final List<Integer> searchOrder; // Store the search order

		/** Construct a tree with root, parent, and searchOrder */
		public Tree(final int root, final int[] parent, final List<Integer> searchOrder) {
			this.root = root;
			this.parent = parent;
			this.searchOrder = searchOrder;
		}

		/** Return the root of the tree */
		public int getRoot() {
			return root;
		}

		/** Return the parent of vertex v */
		public int getParent(final int v) {
			return parent[v];
		}

		/** Return an array representing search order */
		public List<Integer> getSearchOrder() {
			return searchOrder;
		}

		/** Return number of vertices found */
		public int getNumberOfVerticesFound() {
			return searchOrder.size();
		}

		/** Return the path of vertices from a vertex to the root */
		public List<V> getPath(int index) {
			final ArrayList<V> path = new ArrayList<V>();

			do {
				path.add(vertices.get(index));
				index = parent[index];
			} while (index != -1);

			return path;
		}

		/** Print a path from the root to vertex v */
		public void printPath(final int index) {
			final List<V> path = getPath(index);
			System.out.print("A path from " + vertices.get(root) + " to " + vertices.get(index)
					+ ": ");
			for (int i = path.size() - 1; i >= 0; i--) {
				System.out.print(path.get(i) + " ");
			}
		}

		/** Print the whole tree */
		public void printTree() {
			System.out.println("Root is: " + vertices.get(root));
			System.out.print("Edges: ");
			for (int i = 0; i < parent.length; i++) {
				if (parent[i] != -1) {
					// Display an edge
					System.out.print("(" + vertices.get(parent[i]) + ", " + vertices.get(i) + ") ");
				}
			}
			System.out.println();
		}
	}
}
