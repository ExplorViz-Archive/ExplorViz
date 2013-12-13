package explorviz.visualization.engine.math;

public interface Bounding {
	public boolean isVisible(Plane[] frustum);
	public double rayCast(Ray ray);
}