package explorviz.visualization.engine.math;

public class Matrix33f {
    public final float[] entries = new float[3 * 3];
    
    public Matrix33f() {
	entries[0] = 1;
	entries[1] = 0;
	entries[2] = 0;
	entries[3] = 0;
	entries[4] = 1;
	entries[5] = 0;
	entries[6] = 0;
	entries[7] = 0;
	entries[8] = 1;
    }
    
    public Matrix33f(Matrix33f m) {
	entries[0] = m.entries[0];
	entries[1] = m.entries[1];
	entries[2] = m.entries[2];
	entries[3] = m.entries[3];
	entries[4] = m.entries[4];
	entries[5] = m.entries[5];
	entries[6] = m.entries[6];
	entries[7] = m.entries[7];
	entries[8] = m.entries[8];
    }
    
    public Matrix33f(final float _00, final float _01, final float _02, final float _10,
	    final float _11, final float _12, final float _20, final float _21, float _22) {
	entries[0] = _00;
	entries[1] = _01;
	entries[2] = _02;
	entries[3] = _10;
	entries[4] = _11;
	entries[5] = _12;
	entries[6] = _20;
	entries[7] = _21;
	entries[8] = _22;
    }
    
    public Vector3f mult(Vector3f v) {
	return new Vector3f((entries[0] * v.x) + (entries[3] * v.y) + (entries[6] * v.z),
		(entries[1] * v.x) + (entries[4] * v.y) + (entries[7] * v.z), (entries[2] * v.x)
			+ (entries[5] * v.y) + (entries[8] * v.z));
    }
    
    public Matrix33f mult(Matrix33f m) {
	return new Matrix33f(
		((entries[0] * m.entries[0]) + (entries[1] * m.entries[3]) + (entries[2] * m.entries[6])),
		((entries[0] * m.entries[1]) + (entries[1] * m.entries[4]) + (entries[2] * m.entries[7])),
		((entries[0] * m.entries[2]) + (entries[1] * m.entries[5]) + (entries[2] * m.entries[8])),
		
		((entries[3] * m.entries[0]) + (entries[4] * m.entries[3]) + (entries[5] * m.entries[6])),
		((entries[3] * m.entries[1]) + (entries[4] * m.entries[4]) + (entries[5] * m.entries[7])),
		((entries[3] * m.entries[2]) + (entries[4] * m.entries[5]) + (entries[5] * m.entries[8])),
		
		((entries[6] * m.entries[0]) + (entries[7] * m.entries[3]) + (entries[8] * m.entries[6])),
		((entries[6] * m.entries[1]) + (entries[7] * m.entries[4]) + (entries[8] * m.entries[7])),
		((entries[6] * m.entries[2]) + (entries[7] * m.entries[5]) + (entries[8] * m.entries[8])));
    }
    
    @Override
    public String toString() {
	return "(" + entries[0] + ", " + entries[1] + ", " + entries[2] + "\n" + entries[3] + ", "
		+ entries[4] + ", " + entries[5] + "\n" + entries[6] + ", " + entries[7] + ", "
		+ entries[8] + ")";
    }
    
    public static Matrix33f rotationX(double angleInDeg) {
	final double angleInRad = Math.toRadians(angleInDeg);
	final float sin = (float) Math.sin(angleInRad);
	final float cos = (float) Math.cos(angleInRad);
	return new Matrix33f(1, 0, 0, 0, cos, sin, 0, -sin, cos);
    }
    
    public static Matrix33f rotationY(double angleInDeg) {
	final double angleInRad = Math.toRadians(angleInDeg);
	final float sin = (float) Math.sin(angleInRad);
	final float cos = (float) Math.cos(angleInRad);
	return new Matrix33f(cos, 0, -sin, 0, 1, 0, sin, 0, cos);
    }
    
    public static Matrix33f rotationZ(double angleInDeg) {
	final double angleInRad = Math.toRadians(angleInDeg);
	final float sin = (float) Math.sin(angleInRad);
	final float cos = (float) Math.cos(angleInRad);
	return new Matrix33f(cos, sin, 0, -sin, cos, 0, 0, 0, 1);
    }
    
    public static Matrix33f rotationAxis(Vector3f axis, double angleInDeg) {
	axis = axis.normalize();
	final double angleInRad = Math.toRadians(angleInDeg);
	final float sin = (float) Math.sin(angleInRad);
	final float cos = (float) Math.cos(angleInRad);
	final float inv = (float) (1.0 - cos);
	return new Matrix33f((inv * axis.x * axis.x) + cos, (inv * axis.y * axis.x)
		- (sin * axis.z), (inv * axis.z * axis.x) + (sin * axis.y), (inv * axis.x * axis.y)
		+ (sin * axis.z), (inv * axis.y * axis.y) + cos, (inv * axis.z * axis.y)
		- (sin * axis.x), (inv * axis.x * axis.z) - (sin * axis.y), (inv * axis.y * axis.z)
		+ (sin * axis.x), (inv * axis.z * axis.z) + cos);
    }
    
    public static Matrix33f scale(float s) {
	return new Matrix33f(s, 0, 0, 0, s, 0, 0, 0, s);
    }
    
    public float determinant() {
	float determinant = (entries[0] * entries[4] * entries[8])
		+ (entries[3] * entries[7] * entries[2]) + (entries[6] * entries[1] * entries[5]);
	determinant -= (entries[2] * entries[4] * entries[6])
		+ (entries[5] * entries[7] * entries[0]) + (entries[8] * entries[1] * entries[3]);
	
	return determinant;
    }
}
