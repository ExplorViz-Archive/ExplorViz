//package explorviz.visualization.layout.graph;
//
//public class GraphView extends javax.swing.JPanel {
//
//	private final Graph<? extends Displayable> graph;
//
//	public GraphView(final Graph<? extends Displayable> graph) {
//		this.graph = graph;
//	}
//
//	@Override
//	protected void paintComponent(final java.awt.Graphics g) {
//		super.paintComponent(g);
//
//		// Draw vertices
//		final java.util.List<? extends Displayable> vertices = graph.getVertices();
//		for (int i = 0; i < graph.getSize(); i++) {
//			final int x = vertices.get(i).getX();
//			final int y = vertices.get(i).getY();
//			final String name = vertices.get(i).getName();
//
//			g.fillOval(x - 8, y - 8, 16, 16);// Display a vertex
//			g.drawString(name, x - 12, y - 12); // Display the name
//		}
//
//		// Draw edges for pair of vertices
//		for (int i = 0; i < graph.getSize(); i++) {
//			final java.util.List<Integer> neighbors = graph.getNeighbors(i);
//			final int x1 = graph.getVertex(i).getX();
//			final int y1 = graph.getVertex(i).getY();
//			for (final int v : neighbors) {
//				final int x2 = graph.getVertex(v).getX();
//				final int y2 = graph.getVertex(v).getY();
//
//				g.drawLine(x1, y1, x2, y2);// Draw an edge for (i, v)
//			}
//		}
//	}
// }
