package explorviz.visualization.layout.datastructures.graph.model;

import java.util.*;

import explorviz.visualization.layout.datastructures.graph.view.MatrixOutput;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author Erich
 */
/**
 * An implementation of the <code>GraphInterface</code> interface for a directed
 * graph using an adjacency matrix to indicate the presence/absence of edges
 * connecting vertices in the graph.
 */
public class AdjMatrixGraph<T> implements GraphInterface<T> {

	protected int numberOfVertices;
	protected int numberOfEdges;
	/**
	 * adjMatrix[i][j] =1 ; an edge exists FROM vertex i TO vertex j
	 * adjMatrix[i][j] =0 ; NO edge exists from vertex i to vertex j
	 */
	public static double[][] adjMatrix;
	/**
	 * Stores the vertices that are part of this graph. There is no requirement
	 * that the vertices be in adjacent cells of the array; as vertices are
	 * deleted, some gaps may appear.
	 */
	protected Vertex<T>[] vertices;
	/**
	 * vPos and vPos represent a position in adjMatrix. This class and
	 * subclasses use it to access an edge.
	 */
	protected int v1Pos, v2Pos;
	public static int SIZE = 50;
	double[][] array;
	public static String tmp;
	public static String tmps;
	@SuppressWarnings("unused")
	private static Random rnd;

	/**
	 * Constructor. Create an empty/initialize instance of a undirected graph.
	 */
	@SuppressWarnings("unchecked")
	public AdjMatrixGraph() {

		this.numberOfVertices = 0;
		this.numberOfEdges = 0;
		AdjMatrixGraph.adjMatrix = new double[AdjMatrixGraph.SIZE][AdjMatrixGraph.SIZE];
		this.vertices = new Vertex[AdjMatrixGraph.SIZE];
	}

	@SuppressWarnings("unchecked")
	public AdjMatrixGraph(final int numberOfVertices) {

		if (numberOfVertices < 0) {
			throw new RuntimeException("Number of vertices must be nonnegative");
		} else {
			AdjMatrixGraph.SIZE = numberOfVertices;
			this.numberOfVertices = 0;
			this.numberOfEdges = 0;
			AdjMatrixGraph.adjMatrix = new double[SIZE][SIZE];
			this.vertices = new Vertex[SIZE];
		}

	}

	@SuppressWarnings("static-access")
	public void addEdge(final Vertex<T> v1, final Vertex<T> v2) {
		v1Pos = getVerticesIndexFor(v1);
		v2Pos = getVerticesIndexFor(v2);

		if ((v1Pos == -1) || (v2Pos == -1)) {

			throw new IllegalArgumentException("vertex not found");
		}
		// avoid adding duplicate edges
		if (this.adjMatrix[v1Pos][v2Pos] == 0) {
			this.adjMatrix[v1Pos][v2Pos] = 1;
			this.adjMatrix[v2Pos][v1Pos] = 1;
			this.numberOfEdges++;
			System.out.println();
		} else {

			// ignore the Edge. The number of Edges does not need to change
			this.adjMatrix[v1Pos][v2Pos] = 1;
			this.numberOfEdges = numberOfEdges + 0;
			// throw new IllegalArgumentException("duplicate edges "
			// + v1 + " and " + v2);
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public void removeEdge(final Vertex<T> v1, final Vertex<T> v2) {
		v1Pos = getVerticesIndexFor(v1);
		v2Pos = getVerticesIndexFor(v2);

		if ((v1Pos == -1) || (v2Pos == -1)) {
			throw new IllegalArgumentException("vertex not found");
		}
		if (this.adjMatrix[v1Pos][v2Pos] == 1) {
			this.adjMatrix[v1Pos][v2Pos] = 0;
			this.adjMatrix[v2Pos][v1Pos] = 0;
			this.numberOfEdges--;
		} else {
			throw new IllegalArgumentException("edge not found");
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public void removeVertex(final Vertex<T> v) {
		final int pos = getVerticesIndexFor(v);
		if (pos == -1) {
			throw new IllegalArgumentException("vertex not found");
		}

		this.numberOfVertices--;
		this.vertices[pos] = null;

		// now we need to go through the adjacency matrix and
		// remove all edges incident on v. We do this by walking g
		// alon the row and column for v in the adjacency matrix
		for (int i = 0; i < vertices.length; i++) {
			if (this.adjMatrix[pos][i] == 1) { // row check
				this.adjMatrix[pos][i] = 0;
				this.numberOfEdges--;
			}
			if (this.adjMatrix[i][pos] == 1) { // column check
				this.adjMatrix[i][pos] = 0;
				this.numberOfEdges--;
			}
		}
	}

	@Override
	public void addVertex(final Vertex<T> v) {
		final int posNeighborVertex = getVerticesIndexFor(v);

		if (posNeighborVertex != -1) {

			// vertices[getFreeVertexPosition()] = v;
			// this.numberOfVertices = numberOfVertices;
			// System.out.println("ignore");
			// throw new IllegalArgumentException(
			// "duplicate vertex " + v);
		}

		final int posNewVertex = getFreeVertexPosition();
		vertices[posNewVertex] = v;
		this.numberOfVertices++;

	}

	public List<Vertex<T>> getNeighbors(final Vertex<T> v) {
		final int pos = getVerticesIndexFor(v);
		if (pos == -1) {
			throw new IllegalArgumentException("vertex not found");
		}

		final List<Vertex<T>> neighbors = new ArrayList<Vertex<T>>();
		for (int i = 0; i < vertices.length; i++) {
			if (AdjMatrixGraph.adjMatrix[pos][i] == 1) {
				neighbors.add(vertices[i]);
			}
		}

		return neighbors;
	}

	/**
	 * Get the number of edges in this graph.
	 * 
	 * @return int The number of edges in this graph
	 */
	@Override
	public int getNumberOfEdges() {
		return this.numberOfEdges;
	}

	/**
	 * Get the number of vertices in this graph.
	 * 
	 * @return int The number of vertices in this graph
	 */
	@Override
	public int getNumberOfVertices() {
		return this.numberOfVertices;
	}

	/**
	 * Find the first free position in vertices.
	 * 
	 * @return int Index of the the first free position in vertices or - if
	 *         there are none.
	 */
	protected int getFreeVertexPosition() {
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i] == null) {
				return i;
			}
		}
		return -1;
	}

	protected int getVerticesIndexFor(final Vertex<T> v) {
		if (v == null) {
			throw new IllegalArgumentException("null vertex");
		}
		System.out.println(vertices.length);
		for (int i = 0; i < vertices.length; i++) {

			if ((vertices[i] != null) && vertices[i].equals(v)) {
				return i;
			}
		}
		return -1;
	}

	// Method for printing and showing the matrix in a JTextfield
	public void showMatrixTxtArea(final double[][] array, final MatrixOutput ta) {

		// tea.setMargin(new Insets(400, 50, 0, 0));

		final int rowSize = array.length;
		final int columnSize = array[0].length;
		tmp = "";

		for (int i = 0; i <= (rowSize - 1); i++) {

			tmp += "|";
			System.out.print("|");
			for (int j = 0; j <= (columnSize - 1); j++) {

				final int converted = (int) array[i][j];
				tmp += " " + Integer.toString(converted);
				System.out.print(" " + array[i][j]);

			}
			tmp += "|\n";
			// Nur für die Konsole
			System.out.println(" |");

		}
		System.out.println();
		tmp += "\n";

		ta.setText(tmp);

	}
}
