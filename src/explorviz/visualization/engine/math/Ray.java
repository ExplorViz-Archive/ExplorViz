package explorviz.visualization.engine.math;

import java.util.List;

import explorviz.visualization.engine.primitives.*;

public class Ray {
	public final Vector3f origin;
	public final Vector3f dir;

	public Ray(final Vector3f origin, final Vector3f dir) {
		this.origin = new Vector3f(origin);
		this.dir = new Vector3f(dir);
	}

	@Override
	public String toString() {
		return "[origin = " + origin + ",\n dir = " + dir + "]";
	}

	public boolean intersects(final PrimitiveObject object) {
		if (object instanceof Box) {
			return checkBox((Box) object);
		} else if (object instanceof Quad) {
			return checkQuad((Quad) object);
		} else if (object instanceof Triangle) {
			return checkTriangle((Triangle) object);
		} else if (object instanceof Line) {
			return checkLine((Line) object);
		} else if (object instanceof Pipe) {
			return checkPipe((Pipe) object);
		}

		return false;
	}

	private boolean checkBox(final Box box) {
		for (final Quad quad : box.getQuads()) {
			if (checkQuad(quad)) {
				return true;
			}
		}

		return false;
	}

	private boolean checkQuad(final Quad quad) {
		final List<Triangle> triangles = quad.getTriangles();
		return checkTriangle(triangles.get(0)) || checkTriangle(triangles.get(1));
	}

	private boolean checkTriangle(final Triangle triangle) {
		return getIntersectCoefficient(triangle) < (Float.MAX_VALUE - 100f);
	}

	private boolean checkLine(final Line line) {
		for (final Quad quad : line.getQuads()) {
			if (checkQuad(quad)) {
				return true;
			}
		}

		return false;
	}

	private boolean checkPipe(final Pipe pipe) {
		for (final Quad quad : pipe.getQuads()) {
			if (checkQuad(quad)) {
				return true;
			}
		}

		return false;
	}

	public float getIntersectCoefficient(final PrimitiveObject object) {
		if (object instanceof Box) {
			return getIntersectCoefficient((Box) object);
		} else if (object instanceof Quad) {
			return getIntersectCoefficient((Quad) object);
		} else if (object instanceof Triangle) {
			return getIntersectCoefficient((Triangle) object);
		} else if (object instanceof Line) {
			return getIntersectCoefficient((Line) object);
		} else if (object instanceof Pipe) {
			return getIntersectCoefficient((Pipe) object);
		}

		return Float.MAX_VALUE;
	}

	private float getIntersectCoefficient(final Box box) {
		float minimum = Float.MAX_VALUE;

		for (final Quad quad : box.getQuads()) {
			minimum = Math.min(minimum, getIntersectCoefficient(quad));
		}

		return minimum;
	}

	private float getIntersectCoefficient(final Quad quad) {
		final List<Triangle> triangles = quad.getTriangles();
		return Math.min(getIntersectCoefficient(triangles.get(0)),
				getIntersectCoefficient(triangles.get(1)));
	}

	/**
	 * Algorithm inspired by
	 * http://geomalgorithms.com/a06-_intersect-2.html#intersect3D_RayTriangle
	 * %28%29
	 * 
	 * @param triangle
	 * @return
	 */
	private float getIntersectCoefficient(final Triangle triangle) {
		final float[] vertices = triangle.getVertices();

		final Vector3f triangleV0 = new Vector3f(vertices[0], vertices[1], vertices[2]);
		final Vector3f triangleV1 = new Vector3f(vertices[3], vertices[4], vertices[5]);
		final Vector3f triangleV2 = new Vector3f(vertices[6], vertices[7], vertices[8]);

		final Vector3f u = triangleV1.sub(triangleV0);
		final Vector3f v = triangleV2.sub(triangleV0);

		final Vector3f n = u.cross(v);

		if (n == new Vector3f(0, 0, 0)) { // triangle is degenerate
			return Float.MAX_VALUE;
		}

		final Vector3f w0 = origin.sub(triangleV0);
		final float a = -1 * n.dot(w0);
		final float b = n.dot(dir);

		if (Math.abs(b) < 0.00001f) { // ray is parallel
			return Float.MAX_VALUE;
		}

		final float r = a / b;
		if (r < 0f) { // ray points in other direction
			return Float.MAX_VALUE;
		}

		final Vector3f intersectPointOfPlane = origin.add(dir.scale(r));

		// is I inside T?
		float uu, uv, vv, wu, wv, D;
		uu = u.dot(u);
		uv = u.dot(v);
		vv = v.dot(v);
		final Vector3f w = intersectPointOfPlane.sub(triangleV0);
		wu = w.dot(u);
		wv = w.dot(v);
		D = (uv * uv) - (uu * vv);

		// get and test parametric coords
		float s, t;
		s = ((uv * wv) - (vv * wu)) / D;
		if ((s < 0.0) || (s > 1.0)) {
			return Float.MAX_VALUE;
		}
		t = ((uv * wu) - (uu * wv)) / D;
		if ((t < 0.0) || ((s + t) > 1.0)) {
			return Float.MAX_VALUE;
		}

		return r;
	}

	private float getIntersectCoefficient(final Line line) {
		float minimum = Float.MAX_VALUE;

		for (final Quad quad : line.getQuads()) {
			minimum = Math.min(minimum, getIntersectCoefficient(quad));
		}

		return minimum;
	}

	private float getIntersectCoefficient(final Pipe pipe) {
		float minimum = Float.MAX_VALUE;

		for (final Quad quad : pipe.getQuads()) {
			minimum = Math.min(minimum, getIntersectCoefficient(quad));
		}

		return minimum;
	}
}