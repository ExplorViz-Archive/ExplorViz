package explorviz.visualization.engine.main;

import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;
import explorviz.visualization.engine.FloatArray;
import explorviz.visualization.engine.math.Matrix44f;
import explorviz.visualization.engine.math.Vector3f;
import explorviz.visualization.engine.shaders.ShaderInitializer;

public class GLManipulation {
	private static WebGLUniformLocation		modelViewMatrixUniLocation;
	private static WebGLUniformLocation		normalMatrixUniLocation;
	private static WebGLRenderingContext	glContext;
	
	private static Matrix44f				modelViewMatrix;
	
	public static void init(WebGLRenderingContext glContext) {
		GLManipulation.glContext = glContext;
		modelViewMatrixUniLocation = glContext.getUniformLocation(ShaderInitializer.getShaderProgram(),
				"modelViewMatrix");
		normalMatrixUniLocation = glContext.getUniformLocation(ShaderInitializer.getShaderProgram(), "normalMatrix");
		modelViewMatrix = new Matrix44f();
	}
	
	public static Matrix44f getModelViewMatrix() {
		return modelViewMatrix;
	}
	
	public static void translate(Vector3f vec) {
		translate(vec.x, vec.y, vec.z);
	}
	
	public static void translate(float x, float y, float z) {
		modelViewMatrix.entries[12] += x;
		modelViewMatrix.entries[13] += y;
		modelViewMatrix.entries[14] += z;
	}
	
	public static void activateModelViewMatrix() {
		Matrix44f normalMatrix = modelViewMatrix.inverse();
		normalMatrix = normalMatrix.transpose();
		glContext.uniformMatrix4fv(normalMatrixUniLocation, false, FloatArray.create(normalMatrix.entries));
		
		glContext.uniformMatrix4fv(modelViewMatrixUniLocation, false, FloatArray.create(modelViewMatrix.entries));
	}
	
	public static void rotateX(float degree) {
		modelViewMatrix = Matrix44f.rotationX(degree).mult(modelViewMatrix);
	}
	
	public static void rotateY(float degree) {
		modelViewMatrix = Matrix44f.rotationY(degree).mult(modelViewMatrix);
	}
	
	public static void rotateZ(float degree) {
		modelViewMatrix = Matrix44f.rotationZ(degree).mult(modelViewMatrix);
	}
	
	public static void rotateAxis(Vector3f axis, float degree) {
		modelViewMatrix = Matrix44f.rotationAxis(axis, degree).mult(modelViewMatrix);
	}
	
	public static void loadIdentity() {
		modelViewMatrix = new Matrix44f();
	}
}
