package explorviz.visualization.layout.datastructures.graph.model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Erich
 */
import java.util.List;

/**
 * A graph G consists of a finite set of vertices, V, and edges, E. An edge in E
 * connects two vertices in V.
 * 
 * This interface makes no assumption about whether the graph is directed or
 * undirected; implementing classes make that specialization. Edges are
 * unweighted.
 */
public interface GraphInterface<T> {

	/**
	 * Insert <code>Vertex v</code> into this graph.
	 * 
	 * @param v
	 *            The <code>Vertex</code> to add to the graph; can't already be
	 *            in the graph.
	 * @throws IllegalArgumentException
	 *             if <code>v</code> is already in this graph.
	 */
	void addVertex(Vertex<T> v);

	/**
	 * Add an edge connecting vertex <code>v</code> to <code>v</code>. The edge
	 * must not already be in the graph. In an undirected graph, this edge is
	 * bidirectional.
	 * 
	 * @param v
	 *            The source vertex; must not be <code>null</code> and must be a
	 *            vertex in this graph.
	 * @param v
	 *            The destination vertex; must not be <code>null</code> and must
	 *            be a vertex in this graph.
	 * @throws IllegalArgumentException
	 *             if <code>v</code> or <code>v</code> are <code>null</code>,
	 *             are not in this graph, or if the edge already exists.
	 */
	void addEdge(Vertex<T> v1, Vertex<T> v2);

	/**
	 * Remove vertex <code>v</code> and all edges incident on <code>v</code>
	 * from this graph.
	 * 
	 * @param v
	 *            The vertex to remove; must not be <code>null</code and must be
	 *            a vertex in this graph.
	 * @throws IllegalArgumentException
	 *             if <code>v</code> is <code>null</code> or is not in this
	 *             graph.
	 */
	void removeVertex(Vertex<T> v);

	/**
	 * Remove the edge from <code>v</code> to <code>v</code> from this graph.
	 * 
	 * @param v
	 *            The source vertex for the edge to remove; must not be
	 *            <code>null</code> and must be a vertex in this graph.
	 * @param v
	 *            The destination vertex for the edge to remove; must not be
	 *            <code>null</code> and must be a vertex in this graph.
	 * @throws IllegalArgumentException
	 *             if <code>v</code> or <code>v</code> are <code>null</code>,
	 *             are not in this graph, or the edge doesn't exist.
	 */
	void removeEdge(Vertex<T> v1, Vertex<T> v2);

	/**
	 * Get the neighbors of Vertex <code>v</code> in this graph.
	 * 
	 * @param v
	 *            Vertex The vertex whose neighbors we want; must not be
	 *            <code>null</code> and must be a vertex in this graph.
	 * @return List The vertices incident on <code>v</code>.
	 * @throws IllegalArgumentException
	 *             if <code>v</code> is <code>null</code> or is not in this
	 *             graph.
	 */
	List<Vertex<T>> getNeighbors(Vertex<T> v);

	/**
	 * Get the number of vertices in this graph.
	 * 
	 * @return int The number of vertices in this graph.
	 */
	int getNumberOfVertices();

	/**
	 * Get the number of edges in this graph.
	 * 
	 * @return int The number of edges in this graph.
	 */
	int getNumberOfEdges();
}
