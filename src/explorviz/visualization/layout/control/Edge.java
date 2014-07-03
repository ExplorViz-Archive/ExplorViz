package explorviz.visualization.layout.control;
/**
 * Kante
 * @author Wassim
 *
 * @param <V>
 */
public class Edge{
	
	Vertex v1;
	Vertex v2;
	String name; 
	
	public Edge(Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
		this.name = v1 + " verbunden mit " + v2; 
	}
	
	public Vertex getV1() {
		return v1;
	}
	
	public Vertex getV2() {
		return v2;
	}
	
	
	@Override
	public String toString() {

		return this.name;
	}

}
