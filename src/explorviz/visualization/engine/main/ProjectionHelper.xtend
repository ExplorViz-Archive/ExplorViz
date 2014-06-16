package explorviz.visualization.engine.main

import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.math.Matrix44f

class ProjectionHelper {
	var static Matrix44f projectMatrix

	def static void setMatrix(Matrix44f projectMatrixParam) {
		projectMatrix = projectMatrixParam
	}

	public def static unproject(int winX, int winY, int winZ, int viewportWidth, int viewportHeight) {
		val normalized = new Vector4f()
		normalized.x = 2.0f * winX / (viewportWidth as float) - 1f
		normalized.y = -2.0f * winY / (viewportHeight as float) + 1f
		normalized.z = 2.0f * winZ - 1f
		normalized.w = 1f

		val viewProjectMatrix = GLManipulation::getModelViewMatrix().mult(projectMatrix)

		val out = viewProjectMatrix.inverse().mult(normalized)

		if (out.w == 0f) return new Vector3f()

		out.w = 1.0f / out.w

		return new Vector3f(out.x * out.w, out.y * out.w, out.z * out.w)
	}

	public def static unprojectDirection(float x, float y, float z) {
		val normalized = new Vector4f(x, y, z, 1f)

		var rotatedMatrix = new Matrix44f()
		val cameraRotation = Navigation::getCameraRotate()

		rotatedMatrix = Matrix44f.rotationX(cameraRotation.x).mult(rotatedMatrix)
		rotatedMatrix = Matrix44f.rotationY(cameraRotation.y).mult(rotatedMatrix)
		rotatedMatrix = Matrix44f.rotationZ(cameraRotation.z).mult(rotatedMatrix)

		val out = rotatedMatrix.inverse().mult(normalized)

		if (out.w == 0f) return new Vector3f()

		out.w = 1.0f / out.w

		return new Vector3f(out.x * out.w, out.y * out.w, out.z * out.w)
	}
}
