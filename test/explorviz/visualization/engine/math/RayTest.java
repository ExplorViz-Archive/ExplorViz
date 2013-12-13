package explorviz.visualization.engine.math;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.visualization.engine.buffer.BufferManager;
import explorviz.visualization.engine.primitives.Quad;

public class RayTest {
	
	@Test
	public void testIntersectsQuad() throws Exception {
		BufferManager.init(null, null);
		
		final Vector3f origin = new Vector3f(0, 0, 0);
		final Vector3f oneUnitXY = new Vector3f(1, 1, 0);
		
		final Quad quad = new Quad(origin, oneUnitXY, null, null);
		Ray ray = new Ray(new Vector3f(0, 2, -1), new Vector3f(0, 0, 1));
		
		assertFalse(ray.intersects(quad));
		
		ray = new Ray(new Vector3f(0, 0, -1), new Vector3f(0, 0, 1));
		
		// assertTrue(ray.intersects(quad));
	}
}
