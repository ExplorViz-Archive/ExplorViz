package explorviz.visualization.layout.control;

import java.util.Collection;
import java.util.Iterator;

import explorviz.visualization.layout.datastructures.graph.Graph;
import explorviz.visualization.layout.datastructures.graph.UndirectedSparseGraph;
import explorviz.visualization.layout.datastructures.hypergraph.EdgeType;

public class testGraph {

	public static void main(final String[] args) {
		/**
		 * Vertex<Integer> v1 = new Vertex<Integer>(1); Vertex<Integer> v2 = new
		 * Vertex<Integer>(2); Vertex<Integer> v3 = new Vertex<Integer>(3);
		 * Vertex<Integer> v4 = new Vertex<Integer>(4); Vertex<Integer> v5 = new
		 * Vertex<Integer>(5);
		 * 
		 * Edge e1 = new Edge(v1, v2); Edge e2 = new Edge(v2, v3); Edge e3 = new
		 * Edge(v3, v4); Edge e4 = new Edge(v4, v5); Edge e5 = new Edge(v5, v1);
		 * 
		 * 
		 * Graph<Vertex<Integer>, Edge> graph = new
		 * UndirectedSparseGraph<Vertex<Integer>,Edge>();
		 * 
		 * graph.addVertex(v1); graph.addVertex(v2); graph.addVertex(v3);
		 * graph.addVertex(v4); graph.addVertex(v5);
		 * 
		 * graph.addEdge(e1, v1, v2, EdgeType.UNDIRECTED); graph.addEdge(e1, v1,
		 * v2, EdgeType.UNDIRECTED); graph.addEdge(e1, v1, v2,
		 * EdgeType.UNDIRECTED); graph.addEdge(e1, v1, v2, EdgeType.UNDIRECTED);
		 * 
		 * printgraph(graph);
		 */
		final Graph<Vertex<Integer>, Edge> graphk5 = getk5();
		printgraph(graphk5, "K5");

		System.out.println();
		final Graph<Vertex<Integer>, Edge> graphk33 = getk33();
		printgraph(graphk33, "k3,3");

	}

	public static Graph<Vertex<Integer>, Edge> getk5() {

		final Graph<Vertex<Integer>, Edge> graph = new UndirectedSparseGraph<Vertex<Integer>, Edge>();
		// Knoten
		final Vertex<Integer> v1 = new Vertex<Integer>(1);
		final Vertex<Integer> v2 = new Vertex<Integer>(2);
		final Vertex<Integer> v3 = new Vertex<Integer>(3);
		final Vertex<Integer> v4 = new Vertex<Integer>(4);
		final Vertex<Integer> v5 = new Vertex<Integer>(5);

		graph.addVertex(v1);
		graph.addVertex(v2);
		graph.addVertex(v3);
		graph.addVertex(v4);
		graph.addVertex(v5);

		// Kanten

		final Edge e1 = new Edge(v1, v2);
		final Edge e2 = new Edge(v1, v3);
		final Edge e3 = new Edge(v1, v4);
		final Edge e4 = new Edge(v1, v5);

		final Edge e5 = new Edge(v2, v3);
		final Edge e6 = new Edge(v2, v4);
		final Edge e7 = new Edge(v2, v5);

		final Edge e8 = new Edge(v3, v4);
		final Edge e9 = new Edge(v3, v5);

		final Edge e10 = new Edge(v4, v5);

		graph.addEdge(e1, v1, v2, EdgeType.UNDIRECTED);
		graph.addEdge(e2, v1, v3, EdgeType.UNDIRECTED);
		graph.addEdge(e3, v1, v4, EdgeType.UNDIRECTED);
		graph.addEdge(e4, v1, v5, EdgeType.UNDIRECTED);

		graph.addEdge(e5, v2, v3, EdgeType.UNDIRECTED);
		graph.addEdge(e6, v2, v4, EdgeType.UNDIRECTED);
		graph.addEdge(e7, v2, v5, EdgeType.UNDIRECTED);

		graph.addEdge(e8, v3, v4, EdgeType.UNDIRECTED);
		graph.addEdge(e9, v3, v5, EdgeType.UNDIRECTED);

		graph.addEdge(e10, v4, v5, EdgeType.UNDIRECTED);

		return graph;

	}

	public static Graph<Vertex<Integer>, Edge> getk33() {
		final Graph<Vertex<Integer>, Edge> graph = new UndirectedSparseGraph<Vertex<Integer>, Edge>();

		// Knoten
		final Vertex<Integer> v1 = new Vertex<Integer>(1);
		final Vertex<Integer> v2 = new Vertex<Integer>(2);
		final Vertex<Integer> v3 = new Vertex<Integer>(3);
		final Vertex<Integer> v4 = new Vertex<Integer>(4);
		final Vertex<Integer> v5 = new Vertex<Integer>(5);
		final Vertex<Integer> v6 = new Vertex<Integer>(6);

		graph.addVertex(v1);
		graph.addVertex(v2);
		graph.addVertex(v3);

		graph.addVertex(v4);
		graph.addVertex(v5);
		graph.addVertex(v6);

		// Kanten
		final Edge e1 = new Edge(v1, v4);
		final Edge e2 = new Edge(v1, v5);
		final Edge e3 = new Edge(v1, v6);

		final Edge e4 = new Edge(v2, v4);
		final Edge e5 = new Edge(v2, v5);
		final Edge e6 = new Edge(v2, v6);

		final Edge e7 = new Edge(v3, v4);
		final Edge e8 = new Edge(v3, v5);
		final Edge e9 = new Edge(v3, v6);

		graph.addEdge(e1, v1, v4, EdgeType.UNDIRECTED);
		graph.addEdge(e2, v1, v5, EdgeType.UNDIRECTED);
		graph.addEdge(e3, v1, v6, EdgeType.UNDIRECTED);

		graph.addEdge(e4, v2, v4, EdgeType.UNDIRECTED);
		graph.addEdge(e5, v2, v5, EdgeType.UNDIRECTED);
		graph.addEdge(e6, v2, v6, EdgeType.UNDIRECTED);

		graph.addEdge(e7, v3, v4, EdgeType.UNDIRECTED);
		graph.addEdge(e8, v3, v5, EdgeType.UNDIRECTED);
		graph.addEdge(e9, v3, v6, EdgeType.UNDIRECTED);

		return graph;
	}

	public static boolean isPlanar(final Graph g) {
		if (g.getVertexCount() < 5) {
			return true;
		}

		// wenn k5 subgraph enthalten => false

		// wenn k3,3 minor enthalten = false
		// minor gruppen von punkten

		// sonnst planar
		return true;
	}

	public static void printgraph(final Graph g, final String name) {
		System.out.println("Graphansicht: " + name);
		System.out.println("V anzahl:" + g.getVertexCount());
		System.out.println("E anzahl:" + g.getEdgeCount());

		final Collection vertices = g.getVertices();
		final Iterator itvertices = vertices.iterator();

		while (itvertices.hasNext()) {
			System.out.println("V: " + itvertices.next());
			// itvertices.remove();
			// System.out.println(itvertices.hasNext());
		}

		final Collection edges = g.getEdges();
		final Iterator itedges = edges.iterator();
		while (itedges.hasNext()) {
			System.out.println("E: " + itedges.next());
			// itedges.remove();
		}
	}

}
