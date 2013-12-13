package explorviz.visualization.engine.octree;

import explorviz.visualization.engine.math.Matrix44f;
import explorviz.visualization.engine.math.Vector3f;

public class Frustum {
	private static final int	NUMSIDES		= 6;
	
	private static final int	A				= 0;
	private static final int	B				= 1;
	private static final int	C				= 2;
	private static final int	D				= 3;
	
	private static int			RIGHT			= 0;
	private static int			LEFT			= 1;
	private static int			BOTTOM			= 2;
	private static int			TOP				= 3;
	private static int			BACK			= 4;
	private static int			FRONT			= 5;
	
	private static float[][]	FrustumPlanes	= new float[NUMSIDES][4];
	
	private static Matrix44f	perspectiveMatrix;
	
	public static void initPerspectiveMatrix(Matrix44f perspectiveMatrix) {
		Frustum.perspectiveMatrix = perspectiveMatrix;
	}
	
	public static void compute(Matrix44f modelViewMatrix) {
		final Matrix44f clip = modelViewMatrix.mult(perspectiveMatrix);
		
		FrustumPlanes[RIGHT][A] = clip.entries[3] - clip.entries[0];
		FrustumPlanes[RIGHT][B] = clip.entries[7] - clip.entries[4];
		FrustumPlanes[RIGHT][C] = clip.entries[11] - clip.entries[8];
		FrustumPlanes[RIGHT][D] = clip.entries[15] - clip.entries[12];
		
		FrustumPlanes[LEFT][A] = clip.entries[3] + clip.entries[0];
		FrustumPlanes[LEFT][B] = clip.entries[7] + clip.entries[4];
		FrustumPlanes[LEFT][C] = clip.entries[11] + clip.entries[8];
		FrustumPlanes[LEFT][D] = clip.entries[15] + clip.entries[12];
		
		FrustumPlanes[BOTTOM][A] = clip.entries[3] + clip.entries[1];
		FrustumPlanes[BOTTOM][B] = clip.entries[7] + clip.entries[5];
		FrustumPlanes[BOTTOM][C] = clip.entries[11] + clip.entries[9];
		FrustumPlanes[BOTTOM][D] = clip.entries[15] + clip.entries[13];
		
		FrustumPlanes[TOP][A] = clip.entries[3] - clip.entries[1];
		FrustumPlanes[TOP][B] = clip.entries[7] - clip.entries[5];
		FrustumPlanes[TOP][C] = clip.entries[11] - clip.entries[9];
		FrustumPlanes[TOP][D] = clip.entries[15] - clip.entries[13];
		
		FrustumPlanes[BACK][A] = clip.entries[3] - clip.entries[2];
		FrustumPlanes[BACK][B] = clip.entries[7] - clip.entries[6];
		FrustumPlanes[BACK][C] = clip.entries[11] - clip.entries[10];
		FrustumPlanes[BACK][D] = clip.entries[15] - clip.entries[14];
		
		FrustumPlanes[FRONT][A] = clip.entries[3] + clip.entries[2];
		FrustumPlanes[FRONT][B] = clip.entries[7] + clip.entries[6];
		FrustumPlanes[FRONT][C] = clip.entries[11] + clip.entries[10];
		FrustumPlanes[FRONT][D] = clip.entries[15] + clip.entries[14];
		
		normalizeFrustumPlanes();
	}
	
	private static void normalizeFrustumPlanes() {
		for (final float[] fplane : FrustumPlanes) {
			final float inner = (fplane[A] * fplane[A]) + (fplane[B] * fplane[B]) + (fplane[C] * fplane[C]);
			final float magnitude = (float) Math.sqrt(inner);
			
			fplane[A] /= magnitude;
			fplane[B] /= magnitude;
			fplane[C] /= magnitude;
			fplane[D] /= magnitude;
		}
	}
	
	public static boolean isPointWithin(final Vector3f p) {
		for (final float[] fplane : FrustumPlanes) {
			if (((fplane[A] * p.x) + (fplane[B] * p.y) + (fplane[C] * p.z) + fplane[D]) <= 0) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isSphereWithin(final Vector3f center, double radius) {
		for (final float[] fplane : FrustumPlanes) {
			if (((fplane[A] * center.x) + (fplane[B] * center.y) + (fplane[C] * center.z) + fplane[D]) <= -radius) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isBoxWithin(final Vector3f center, final float size) {
		for (final float[] fplane : FrustumPlanes) {
			if (((fplane[A] * (center.x - size)) + (fplane[B] * (center.y - size)) + (fplane[C] * (center.z - size)) + fplane[D]) > 0) {
				continue;
			}
			if (((fplane[A] * (center.x + size)) + (fplane[B] * (center.y - size)) + (fplane[C] * (center.z - size)) + fplane[D]) > 0) {
				continue;
			}
			if (((fplane[A] * (center.x - size)) + (fplane[B] * (center.y + size)) + (fplane[C] * (center.z - size)) + fplane[D]) > 0) {
				continue;
			}
			if (((fplane[A] * (center.x + size)) + (fplane[B] * (center.y + size)) + (fplane[C] * (center.z - size)) + fplane[D]) > 0) {
				continue;
			}
			if (((fplane[A] * (center.x - size)) + (fplane[B] * (center.y - size)) + (fplane[C] * (center.z + size)) + fplane[D]) > 0) {
				continue;
			}
			if (((fplane[A] * (center.x + size)) + (fplane[B] * (center.y - size)) + (fplane[C] * (center.z + size)) + fplane[D]) > 0) {
				continue;
			}
			if (((fplane[A] * (center.x - size)) + (fplane[B] * (center.y + size)) + (fplane[C] * (center.z + size)) + fplane[D]) > 0) {
				continue;
			}
			if (((fplane[A] * (center.x + size)) + (fplane[B] * (center.y + size)) + (fplane[C] * (center.z + size)) + fplane[D]) > 0) {
				continue;
			}
			
			return false;
		}
		
		return true;
	}
}
