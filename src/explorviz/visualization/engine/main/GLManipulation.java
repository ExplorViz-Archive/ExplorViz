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
	private static Matrix44f rotateX33DegMatrix = Matrix44f.rotationX(33);
	private static Matrix44f rotateY45DegMatrix = Matrix44f.rotationY(45);

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
		modelViewMatrix.entries[14] += z;
	}

	public static void activateModelViewMatrix() {
		final Matrix44f normalMatrix = modelViewMatrix.inverseWithoutTranspose();
		glContext.uniformMatrix4fv(normalMatrixUniLocation, false,
				FloatArray.create(normalMatrix.entries));

		glContext.uniformMatrix4fv(modelViewMatrixUniLocation, false,
				FloatArray.create(modelViewMatrix.entries));
	}

	public static void rotateX(final float degree) {
		if ((degree < 0.01f) && (degree > -0.01f)) {
			return;
		}

		if ((degree < 33.01f) && (degree > -33.01f)) {
			modelViewMatrix = rotateX33DegMatrix.mult(modelViewMatrix);
		} else {
			modelViewMatrix = Matrix44f.rotationX(degree).mult(modelViewMatrix);
		}
	}

	public static void rotateY(final float degree) {
		if ((degree < 0.01f) && (degree > -0.01f)) {
			return;
		}

		if ((degree < 45.01f) && (degree > -45.01f)) {
			modelViewMatrix = rotateY45DegMatrix.mult(modelViewMatrix);
		} else {
			modelViewMatrix = Matrix44f.rotationY(degree).mult(modelViewMatrix);
		}
	}

	public static void rotateZ(final float degree) {
		if ((degree < 0.01f) && (degree > -0.01f)) {
			return;
		}

		modelViewMatrix = Matrix44f.rotationZ(degree).mult(modelViewMatrix);
	}

	public static void rotateAxis(final Vector3f axis, final float degree) {
		modelViewMatrix = Matrix44f.rotationAxis(axis, degree).mult(modelViewMatrix);
	}

	public static void loadIdentity() {
		modelViewMatrix.reset();
	}
}
