package explorviz.visualization.engine.main

import explorviz.visualization.engine.math.Matrix44f
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f

class ProjectionHelper {
	var static Matrix44f projectMatrix

	def static void setMatrix(Matrix44f projectMatrixParam) {
		projectMatrix = projectMatrixParam
	}

	public def static unproject(int winX, int winY, int winZ, int viewportWidth, int viewportHeight, Matrix44f modelView) {
		val normalized = new Vector4f()
		normalized.x = winX / (viewportWidth as float) * 2.0f - 1f
		normalized.y = winY / (viewportHeight as float) * -2.0f + 1f
		normalized.z = 2.0f * winZ - 1f
		normalized.w = 1f

		val viewProjectMatrixInverse = modelView.mult(projectMatrix).inverse()

		val out = viewProjectMatrixInverse.mult(normalized)

		if (out.w == 0f) return new Vector3f()

		out.w = 1.0f / out.w

		return new Vector3f(out.x * out.w, out.y * out.w, out.z * out.w)
	}
}
