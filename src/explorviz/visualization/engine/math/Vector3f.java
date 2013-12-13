package explorviz.visualization.engine.math;

public class Vector3f {
	public float	x, y, z;
	
	public Vector3f() {
		x = y = z = 0;
	}
	
	public Vector3f(Vector3f v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	public Vector3f(Vector4f v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	public Vector3f(float val) {
		x = y = z = val;
	}
	
	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3f add(Vector3f v) {
		return new Vector3f(x + v.x, y + v.y, z + v.z);
	}
	
	public Vector3f sub(Vector3f v) {
		return new Vector3f(x - v.x, y - v.y, z - v.z);
	}
	
	public Vector3f negate() {
		return new Vector3f(-x, -y, -z);
	}
	
	public Vector3f scale(float c) {
		return new Vector3f(x * c, y * c, z * c);
	}
	
	public Vector3f div(float c) {
		if ((c < 0.000001) && (c > -0.000001)) {
			throw new IllegalArgumentException("c must not be 0");
		}
		return new Vector3f(x / c, y / c, z / c);
	}
	
	public float dot(Vector3f v) {
		return (x * v.x) + (y * v.y) + (z * v.z);
	}
	
	public Vector3f cross(Vector3f v) {
		return new Vector3f((y * v.z) - (z * v.y), (z * v.x) - (x * v.z), (x * v.y) - (y * v.x));
	}
	
	public float length() {
		return (float) Math.sqrt((x * x) + (y * y) + (z * z));
	}
	
	public Vector3f scaleToLength(float L) {
		return new Vector3f((x * L) / length(), (y * L) / length(), (z * L) / length());
	}
	
	public Vector3f normalize() {
		final float invlen = (float) (1.0 / Math.sqrt((x * x) + (y * y) + (z * z)));
		return new Vector3f(x * invlen, y * invlen, z * invlen);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Vector3f) {
			final Vector3f otherVector = (Vector3f) other;
			return (checkFloatEquals(x, otherVector.x) && checkFloatEquals(y, otherVector.y) && checkFloatEquals(z,
					otherVector.z));
		}
		return false;
	}
	
	private boolean checkFloatEquals(float first, float second) {
		return Math.abs(first - second) < 0.001f;
		
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
}
