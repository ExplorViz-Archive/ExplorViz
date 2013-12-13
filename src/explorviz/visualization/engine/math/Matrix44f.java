package explorviz.visualization.engine.math;

public class Matrix44f {
	public final float[]	entries	= new float[4 * 4];
	
	public Matrix44f() {
		entries[0] = 1;
		entries[1] = 0;
		entries[2] = 0;
		entries[3] = 0;
		entries[4] = 0;
		entries[5] = 1;
		entries[6] = 0;
		entries[7] = 0;
		entries[8] = 0;
		entries[9] = 0;
		entries[10] = 1;
		entries[11] = 0;
		entries[12] = 0;
		entries[13] = 0;
		entries[14] = 0;
		entries[15] = 1;
	}
	
	public Matrix44f(Matrix44f m) {
		entries[0] = m.entries[0];
		entries[1] = m.entries[1];
		entries[2] = m.entries[2];
		entries[3] = m.entries[3];
		entries[4] = m.entries[4];
		entries[5] = m.entries[5];
		entries[6] = m.entries[6];
		entries[7] = m.entries[7];
		entries[8] = m.entries[8];
		entries[9] = m.entries[9];
		entries[10] = m.entries[10];
		entries[11] = m.entries[11];
		entries[12] = m.entries[12];
		entries[13] = m.entries[13];
		entries[14] = m.entries[14];
		entries[15] = m.entries[15];
	}
	
	public Matrix44f(final float _00, final float _01, final float _02, final float _03, final float _10,
			final float _11, final float _12, final float _13, final float _20, final float _21, final float _22,
			final float _23, final float _30, final float _31, final float _32, float _33) {
		entries[0] = _00;
		entries[1] = _01;
		entries[2] = _02;
		entries[3] = _03;
		entries[4] = _10;
		entries[5] = _11;
		entries[6] = _12;
		entries[7] = _13;
		entries[8] = _20;
		entries[9] = _21;
		entries[10] = _22;
		entries[11] = _23;
		entries[12] = _30;
		entries[13] = _31;
		entries[14] = _32;
		entries[15] = _33;
	}
	
	public Vector4f mult(Vector4f v) {
		return new Vector4f((entries[0] * v.x) + (entries[4] * v.y) + (entries[8] * v.z) + (entries[12] * v.w),
				(entries[1] * v.x) + (entries[5] * v.y) + (entries[9] * v.z) + (entries[13] * v.w), (entries[2] * v.x)
						+ (entries[6] * v.y) + (entries[10] * v.z) + (entries[14] * v.w), (entries[3] * v.x)
						+ (entries[7] * v.y) + (entries[11] * v.z) + (entries[15] * v.w));
	}
	
	public Matrix44f mult(Matrix44f m) {
		return new Matrix44f(
				((entries[0] * m.entries[0]) + (entries[1] * m.entries[4]) + (entries[2] * m.entries[8]) + (entries[3] * m.entries[12])),
				((entries[0] * m.entries[1]) + (entries[1] * m.entries[5]) + (entries[2] * m.entries[9]) + (entries[3] * m.entries[13])),
				((entries[0] * m.entries[2]) + (entries[1] * m.entries[6]) + (entries[2] * m.entries[10]) + (entries[3] * m.entries[14])),
				((entries[0] * m.entries[3]) + (entries[1] * m.entries[7]) + (entries[2] * m.entries[11]) + (entries[3] * m.entries[15])),
				
				((entries[4] * m.entries[0]) + (entries[5] * m.entries[4]) + (entries[6] * m.entries[8]) + (entries[7] * m.entries[12])),
				((entries[4] * m.entries[1]) + (entries[5] * m.entries[5]) + (entries[6] * m.entries[9]) + (entries[7] * m.entries[13])),
				((entries[4] * m.entries[2]) + (entries[5] * m.entries[6]) + (entries[6] * m.entries[10]) + (entries[7] * m.entries[14])),
				((entries[4] * m.entries[3]) + (entries[5] * m.entries[7]) + (entries[6] * m.entries[11]) + (entries[7] * m.entries[15])),
				
				((entries[8] * m.entries[0]) + (entries[9] * m.entries[4]) + (entries[10] * m.entries[8]) + (entries[11] * m.entries[12])),
				((entries[8] * m.entries[1]) + (entries[9] * m.entries[5]) + (entries[10] * m.entries[9]) + (entries[11] * m.entries[13])),
				((entries[8] * m.entries[2]) + (entries[9] * m.entries[6]) + (entries[10] * m.entries[10]) + (entries[11] * m.entries[14])),
				((entries[8] * m.entries[3]) + (entries[9] * m.entries[7]) + (entries[10] * m.entries[11]) + (entries[11] * m.entries[15])),
				
				((entries[12] * m.entries[0]) + (entries[13] * m.entries[4]) + (entries[14] * m.entries[8]) + (entries[15] * m.entries[12])),
				((entries[12] * m.entries[1]) + (entries[13] * m.entries[5]) + (entries[14] * m.entries[9]) + (entries[15] * m.entries[13])),
				((entries[12] * m.entries[2]) + (entries[13] * m.entries[6]) + (entries[14] * m.entries[10]) + (entries[15] * m.entries[14])),
				((entries[12] * m.entries[3]) + (entries[13] * m.entries[7]) + (entries[14] * m.entries[11]) + (entries[15] * m.entries[15])));
	}
	
	public Matrix44f transpose() {
		return new Matrix44f(entries[0], entries[4], entries[8], entries[12], entries[1], entries[5], entries[9],
				entries[13], entries[2], entries[6], entries[10], entries[14], entries[3], entries[7], entries[11],
				entries[15]);
	}
	
	public Matrix33f normalMatrix() {
		final float det = ((entries[0] * ((entries[10] * entries[5]) - (entries[9] * entries[6]))) - (entries[4] * ((entries[10] * entries[1]) - (entries[9] * entries[2]))))
				+ (entries[8] * ((entries[6] * entries[1]) - (entries[5] * entries[2])));
		return new Matrix33f(((entries[10] * entries[5]) - (entries[6] * entries[9])) / det,
				-((entries[10] * entries[4]) - (entries[6] * entries[8])) / det,
				((entries[9] * entries[4]) - (entries[5] * entries[8])) / det,
				-((entries[10] * entries[1]) - (entries[2] * entries[9])) / det,
				((entries[10] * entries[0]) - (entries[2] * entries[8])) / det,
				-((entries[9] * entries[0]) - (entries[1] * entries[8])) / det,
				((entries[6] * entries[1]) - (entries[2] * entries[5])) / det,
				-((entries[6] * entries[0]) - (entries[2] * entries[4])) / det,
				((entries[5] * entries[0]) - (entries[1] * entries[4])) / det);
	}
	
	public Matrix33f rotationMatrix() {
		return new Matrix33f(entries[0], entries[1], entries[2], entries[4], entries[5], entries[6], entries[8],
				entries[9], entries[10]);
	}
	
	/**
	 * Computes inverse matrix to <code>this</code> matrix
	 * 
	 * @return <code>this</code> inversed matrix
	 */
	public Matrix44f inverse() {
		final Matrix44f result = new Matrix44f();
		final float det = determinant();
		if (det == 0) {
			return result;
		} else {
			Matrix33f temp3;
			temp3 = new Matrix33f(entries[5], entries[6], entries[7], entries[9], entries[10], entries[11],
					entries[13], entries[14], entries[15]);
			result.entries[0] = temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[4], entries[6], entries[7], entries[8], entries[10], entries[11],
					entries[12], entries[14], entries[15]);
			result.entries[1] = -temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[4], entries[5], entries[7], entries[8], entries[9], entries[11], entries[12],
					entries[13], entries[15]);
			result.entries[2] = temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[4], entries[5], entries[6], entries[8], entries[9], entries[10], entries[12],
					entries[13], entries[14]);
			result.entries[3] = -temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[1], entries[2], entries[3], entries[9], entries[10], entries[11],
					entries[13], entries[14], entries[15]);
			result.entries[4] = -temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[0], entries[2], entries[3], entries[8], entries[10], entries[11],
					entries[12], entries[14], entries[15]);
			result.entries[5] = temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[0], entries[1], entries[3], entries[8], entries[9], entries[11], entries[12],
					entries[13], entries[15]);
			result.entries[6] = -temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[0], entries[1], entries[2], entries[8], entries[9], entries[10], entries[12],
					entries[13], entries[14]);
			result.entries[7] = temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[1], entries[2], entries[3], entries[5], entries[6], entries[7], entries[13],
					entries[14], entries[15]);
			result.entries[8] = temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[0], entries[2], entries[3], entries[4], entries[6], entries[7], entries[12],
					entries[14], entries[15]);
			result.entries[9] = -temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[0], entries[1], entries[3], entries[4], entries[5], entries[7], entries[12],
					entries[13], entries[15]);
			result.entries[10] = temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[0], entries[1], entries[2], entries[4], entries[5], entries[6], entries[12],
					entries[13], entries[14]);
			result.entries[11] = -temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[1], entries[2], entries[3], entries[5], entries[6], entries[7], entries[9],
					entries[10], entries[11]);
			result.entries[12] = -temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[0], entries[2], entries[3], entries[4], entries[6], entries[7], entries[8],
					entries[10], entries[11]);
			result.entries[13] = temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[0], entries[1], entries[3], entries[4], entries[5], entries[7], entries[8],
					entries[9], entries[11]);
			result.entries[14] = -temp3.determinant() / det;
			
			temp3 = new Matrix33f(entries[0], entries[1], entries[2], entries[4], entries[5], entries[6], entries[8],
					entries[9], entries[10]);
			result.entries[15] = temp3.determinant() / det;
			
			return result.transpose();
		}
	}
	
	private float determinant() {
		float result = 0;
		Matrix33f temp = new Matrix33f(entries[5], entries[6], entries[7], entries[9], entries[10], entries[11],
				entries[13], entries[14], entries[15]);
		result += entries[0] * temp.determinant();
		
		temp = new Matrix33f(entries[4], entries[6], entries[7], entries[8], entries[10], entries[11], entries[12],
				entries[14], entries[15]);
		result -= entries[1] * temp.determinant();
		
		temp = new Matrix33f(entries[4], entries[5], entries[7], entries[8], entries[9], entries[11], entries[12],
				entries[13], entries[15]);
		result += entries[2] * temp.determinant();
		
		temp = new Matrix33f(entries[4], entries[5], entries[6], entries[8], entries[9], entries[10], entries[12],
				entries[13], entries[14]);
		result -= entries[3] * temp.determinant();
		
		return result;
	}
	
	@Override
	public String toString() {
		return "(" + entries[0] + ", " + entries[1] + ", " + entries[2] + ", " + entries[3] + "\n" + entries[4] + ", "
				+ entries[5] + ", " + entries[6] + ", " + entries[7] + "\n" + entries[8] + ", " + entries[9] + ", "
				+ entries[10] + ", " + entries[11] + "\n" + entries[12] + ", " + entries[13] + ", " + entries[14]
				+ ", " + entries[15] + ")";
	}
	
	public static Matrix44f rotationX(float angleInDeg) {
		final double angleInRad = Math.toRadians(angleInDeg);
		final float sin = (float) Math.sin(angleInRad);
		final float cos = (float) Math.cos(angleInRad);
		return new Matrix44f(1, 0, 0, 0, 0, cos, sin, 0, 0, -sin, cos, 0, 0, 0, 0, 1);
	}
	
	public static Matrix44f rotationY(float angleInDeg) {
		final double angleInRad = Math.toRadians(angleInDeg);
		final float sin = (float) Math.sin(angleInRad);
		final float cos = (float) Math.cos(angleInRad);
		return new Matrix44f(cos, 0, -sin, 0, 0, 1, 0, 0, sin, 0, cos, 0, 0, 0, 0, 1);
	}
	
	public static Matrix44f rotationZ(double angleInDeg) {
		final double angleInRad = Math.toRadians(angleInDeg);
		final float sin = (float) Math.sin(angleInRad);
		final float cos = (float) Math.cos(angleInRad);
		return new Matrix44f(cos, sin, 0, 0, -sin, cos, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
	}
	
	public static Matrix44f rotationAxis(Vector3f axis, double angleInDeg) {
		// Rodrignes rotation formula
		axis = axis.normalize();
		final double angleInRad = Math.toRadians(angleInDeg);
		final float sin = (float) Math.sin(angleInRad);
		final float cos = (float) Math.cos(angleInRad);
		final float inv = (float) (1.0 - cos);
		return new Matrix44f((inv * axis.x * axis.x) + cos, (inv * axis.y * axis.x) - (sin * axis.z),
				(inv * axis.z * axis.x) + (sin * axis.y), 0, (inv * axis.x * axis.y) + (sin * axis.z),
				(inv * axis.y * axis.y) + cos, (inv * axis.z * axis.y) - (sin * axis.x), 0, (inv * axis.x * axis.z)
						- (sin * axis.y), (inv * axis.y * axis.z) + (sin * axis.x), (inv * axis.z * axis.z) + cos, 0,
				0, 0, 0, 1);
	}
	
	public static Matrix44f scale(float s) {
		return new Matrix44f(s, 0, 0, 0, 0, s, 0, 0, 0, 0, s, 0, 0, 0, 0, 1);
	}
	
	public static Matrix44f translation(Vector3f v) {
		return new Matrix44f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, v.x, v.y, v.z, 1);
	}
	
	public static Matrix44f translation(float x, float y, float z) {
		return new Matrix44f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, x, y, z, 1);
	}
	
	public static Matrix44f perspective(float fovy, float aspect, float zNear, float zFar) {
		fovy *= Math.PI / 180.0;
		final float f = (float) Math.tan((Math.PI / 2.0) - (fovy / 2.0));
		return new Matrix44f(f / aspect, 0, 0, 0, 0, f, 0, 0, 0, 0, (zFar + zNear) / (zNear - zFar), -1, 0, 0,
				(2 * zFar * zNear) / (zNear - zFar), 0);
	}
	
	public static Matrix44f lookAt(Vector3f eye, Vector3f center, Vector3f up) {
		final Vector3f f = center.sub(eye).normalize();
		final Vector3f s = f.cross(up.normalize()).normalize();
		final Vector3f u = s.cross(f);
		return new Matrix44f(s.x, u.x, -f.x, 0, s.y, u.y, -f.y, 0, s.z, u.z, -f.z, 0, -s.dot(eye), -u.dot(eye),
				f.dot(eye), 1);
	}
}
