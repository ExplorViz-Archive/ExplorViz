package explorviz.visualization.engine.main;

import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;
import explorviz.visualization.engine.FloatArray;
import explorviz.visualization.engine.math.Matrix44f;
import explorviz.visualization.engine.math.Vector3f;
import explorviz.visualization.engine.shaders.ShaderInitializer;

public class GLManipulation {
	private static WebGLUniformLocation modelViewMatrixUniLocation;
	private static WebGLUniformLocation normalMatrixUniLocation;
	private static WebGLRenderingContext glContext;

	private static Matrix44f modelViewMatrix;

	public static void init(final WebGLRenderingContext glContext) {
		GLManipulation.glContext = glContext;
		modelViewMatrixUniLocation = glContext.getUniformLocation(
				ShaderInitializer.getShaderProgram(), "modelViewMatrix");
		normalMatrixUniLocation = glContext.getUniformLocation(
				ShaderInitializer.getShaderProgram(), "normalMatrix");
		modelViewMatrix = new Matrix44f();
	}

	public static Matrix44f getModelViewMatrix() {
		return modelViewMatrix;
	}

	public static void translate(final Vector3f vec) {
		translate(vec.x, vec.y, vec.z);
	}

	public static void translate(final float x, final float y, final float z) {
		modelViewMatrix.entries[12] += x;
		modelViewMatrix.entries[13] += y;
		if (SceneDrawer.lastViewedApplication != null) {
			modelViewMatrix.entries[14] += z;
		}
	}

	public static void scale(final float x, final float y, final float z) {
		modelViewMatrix.entries[0] *= x;
		modelViewMatrix.entries[5] *= y;
		modelViewMatrix.entries[10] *= z;
	}

	public static void activateModelViewMatrix() {
		final Matrix44f normalMatrix = modelViewMatrix.inverseWithoutTranspose();
		glContext.uniformMatrix4fv(normalMatrixUniLocation, false,
				FloatArray.create(normalMatrix.entries));

		glContext.uniformMatrix4fv(modelViewMatrixUniLocation, false,
				FloatArray.create(modelViewMatrix.entries));
	}

	public static void rotateX(final float degree) {
		if ((degree < 0.0001f) && (degree > -0.0001f)) {
			return;
		}

		modelViewMatrix = modelViewMatrix.mult(Matrix44f.rotationX(degree));
	}

	public static void rotateY(final float degree) {
		if ((degree < 0.0001f) && (degree > -0.0001f)) {
			return;
		}

		modelViewMatrix = modelViewMatrix.mult(Matrix44f.rotationY(degree));
	}

	public static void rotateZ(final float degree) {
		if ((degree < 0.0001f) && (degree > -0.0001f)) {
			return;
		}

		modelViewMatrix = modelViewMatrix.mult(Matrix44f.rotationZ(degree));
	}

	public static void rotateAxis(final Vector3f axis, final float degree) {
		modelViewMatrix = modelViewMatrix.mult(Matrix44f.rotationAxis(axis, degree));
	}

	public static void loadIdentity() {
		modelViewMatrix.reset();
	}
}
