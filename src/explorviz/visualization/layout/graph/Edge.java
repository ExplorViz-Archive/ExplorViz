package explorviz.visualization.layout.graph;

public class Edge<E> {
	java.util.ArrayList<E> list = new java.util.ArrayList<E>();
	E u;
	E v;

	public Edge(final E u, final E v) {
		this.u = u;
		this.v = v;
	}

	public Edge() {
		// TODO Auto-generated constructor stub
	}
}