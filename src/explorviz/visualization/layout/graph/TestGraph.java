package explorviz.visualization.layout.graph;

public class TestGraph {
	public static void main(final String[] args) {
		final String[] vertices = { "Kiel", "Hannover", "Berlin", "Kassel", "Hamburg", "Bremen",
				"Luebeck", "New York", "Muenchen", "Bonn", "Frankfurt", "Rostock" };

		// Edge array
		final int[][] edges = { { 0, 1 }, { 0, 3 }, { 0, 5 }, { 1, 0 }, { 1, 2 }, { 1, 3 },
				{ 2, 1 }, { 2, 3 }, { 2, 4 }, { 2, 10 }, { 3, 0 }, { 3, 1 }, { 3, 2 }, { 3, 4 },
				{ 3, 5 }, { 4, 2 }, { 4, 3 }, { 4, 5 }, { 4, 7 }, { 4, 8 }, { 4, 10 }, { 5, 0 },
				{ 5, 3 }, { 5, 4 }, { 5, 6 }, { 5, 7 }, { 6, 5 }, { 6, 7 }, { 7, 4 }, { 7, 5 },
				{ 7, 6 }, { 7, 8 }, { 8, 4 }, { 8, 7 }, { 8, 9 }, { 8, 10 }, { 8, 11 }, { 9, 8 },
				{ 9, 11 }, { 10, 2 }, { 10, 4 }, { 10, 8 }, { 10, 11 }, { 11, 8 }, { 11, 9 },
				{ 11, 10 } };

		final Graph<String> graph1 = new HyperGraph<String>(edges, vertices);
		System.out.println("The number of vertices in graph1: " + graph1.getSize());
		System.out.println("The vertex with index 1 is " + graph1.getVertex(1));
		System.out.println("The index for Berlin is " + graph1.getIndex("Berlin"));
		System.out.println("The edges for graph1:");
		graph1.printEdges();
		System.out.println("The neighbours of 1 are: " + graph1.getNeighbors(1));
		System.out.println("The Adjacency Matrix is: ");
		graph1.printAdjacencyMatrix();
		final int[][] adjMatrix = graph1.getAdjacencyMatrix();
		graph1.calculateColSumm(adjMatrix);
		graph1.getRank(adjMatrix);

	}

}
