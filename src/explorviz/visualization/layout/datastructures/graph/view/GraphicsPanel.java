/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package explorviz.visualization.layout.datastructures.graph.view;

import java.awt.*;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import explorviz.visualization.layout.datastructures.graph.model.AdjMatrixGraph;
import explorviz.visualization.layout.datastructures.graph.model.Vertex;

/**
 * 
 * @author Erich
 */
public class GraphicsPanel extends JPanel {

	AdjMatrixGraph graph;
	MatrixOutput ta;
	LinksOutput links;
	public static String st;
	JTextArea tea;
	int from, to;
	Vector vec;
	int verticeCount;

	public GraphicsPanel(final AdjMatrixGraph panel, final MatrixOutput ta, final LinksOutput output) {

		graph = panel;
		this.ta = ta;
		links = output;
		vec = new Vector(10);
		verticeCount = graph.SIZE;
	}

	@Override
	public void update(final Graphics g) {
		paintComponent(g);

	}

	@Override
	public void paint(final Graphics g) {
		// graph.showMatrixTxtArea(AdjMatrixGraph.adjMatrix, ta);
		super.paint(g);
		final Random rnd = new Random();

		// Function for adding/drawing Node
		int h = 0;
		int v = 10;

		int count = 1;

		for (int y = 1; y <= Math.ceil(Math.sqrt(verticeCount)); y++) {
			for (int x = 1; x <= Math.ceil(Math.sqrt(verticeCount)); x++) {
				if (count <= verticeCount) { // stop drawing when the number of
					final Point p = new Point(h, v); // Vertices is reached
					final Vertex<Integer> vertex = new Vertex(count);
					vertex.setXPos(p);
					vertex.setYPos(p);
					System.out.println(vertex.setXPos(p) + " " + vertex.setYPos(p));
					graph.addVertex(vertex);
					vec.add(p);
					if (verticeCount <= 100) {
						g.setColor(Color.blue);
						final String numberInCircle = Integer.toString(count);
						g.drawString(numberInCircle, h + 15, v);
						g.setColor(new Color(139, 139, 131));
						g.fillOval(h, v, 30, 30);
					}
					h = h + 100;
					count++;
				}
			}

			h = 0;
			v = v + 100;
		}

		// Function for adding/drawing random Edges
		for (int i = 0; i < graph.getNumberOfVertices(); i++) {
			final int radiusOffset = 15;
			final int j = rnd.nextInt((graph.getNumberOfVertices()));
			final int k = rnd.nextInt((graph.getNumberOfVertices()));

			if ((j > 0) && (k > 0) && (k < count) && (j < count) && (j != k)) {
				final String a = Integer.toString(k);
				final String b = Integer.toString(j);

				// System.out.println(k + "--->" + j);
				final Vertex<Integer> source = new Vertex(k);
				final Vertex<Integer> destination = new Vertex(j);
				// Here we use -1 because in the model we start counting by zero
				// and here by 1
				from = source.getLabel() - 1;
				to = destination.getLabel() - 1;
				final Point org = (Point) vec.get(from);
				final Point dest = (Point) vec.get(to);
				if (verticeCount <= 64) {
					g.setColor(Color.red);
					g.drawLine(org.x + radiusOffset, org.y + radiusOffset, dest.x + radiusOffset,
							dest.y + radiusOffset);

				}
				links.append(a + "  ----->  " + b);
				links.append("\t\t ");
				links.append(b + "  ----->  " + a);
				links.append("\n ");
				// graph.showLinks(k, j, links);
				graph.addEdge(source, destination);
				final double abstand = Math.sqrt((dest.x * dest.x) + (dest.y * dest.y));
				final Point on = new Point((dest.x - 30) + radiusOffset, dest.y + 30 + radiusOffset);

			}

		}

		graph.showMatrixTxtArea(AdjMatrixGraph.adjMatrix, ta);

		// System.out.println(graph.getNumberOfEdges());
		// System.out.println(graph.getNumberOfVertices());
	}
}
