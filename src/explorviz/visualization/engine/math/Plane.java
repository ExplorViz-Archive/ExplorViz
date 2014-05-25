package explorviz.visualization.engine.math;

public class Plane {
	public float a, b, c, d;

	public Plane() {
		a = b = c = d = 0;
	}

	public Plane(final float a, final float b, final float c, final float d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public Plane(final Plane p) {
		a = p.a;
		b = p.b;
		c = p.c;
		d = p.d;
	}

	public Plane(final Vector3f point, final Vector3f normal) {
		a = normal.x;
		b = normal.y;
		c = normal.z;
		d = -(normal.dot(point));
	}

	public double dot(final Vector3f p) {
		return (a * p.x) + (b * p.y) + (c * p.z) + d;
	}

	public Plane normalized() {
		final float invlen = (float) (1.0 / Math.sqrt((a * a) + (b * b) + (c * c)));
		return new Plane(a * invlen, b * invlen, c * invlen, d * invlen);
	}

	public void normalize() {
		final double invlen = 1.0 / Math.sqrt((a * a) + (b * b) + (c * c));
		a *= invlen;
		b *= invlen;
		c *= invlen;
		d *= invlen;
	}
}