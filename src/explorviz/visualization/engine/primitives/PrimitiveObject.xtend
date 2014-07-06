package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.math.Vector3f

import static extension explorviz.visualization.main.ArrayExtensions.*

abstract class PrimitiveObject {
	def float[] getVertices()

	def void draw()

	def boolean isHighlighted()

	def void highlight(Vector4f color)

	def void unhighlight()

	def void moveByVector(Vector3f vector)

	def float[] calculateNormal(float[] vertices, int times) {
		val v1 = new Vector3f(vertices.get(0), vertices.get(1), vertices.get(2))
		val v2 = new Vector3f(vertices.get(3), vertices.get(4), vertices.get(5))
		val v3 = new Vector3f(vertices.get(6), vertices.get(7), vertices.get(8))

		val normalVector3f = v2.sub(v1).cross(v3.sub(v1)).normalize
		val float[] normal = createFloatArray(3 * times)

		for (var int i = 0; i < times; i++) {
			normal.set(0 + i * 3, normalVector3f.x)
			normal.set(1 + i * 3, normalVector3f.y)
			normal.set(2 + i * 3, normalVector3f.z)
		}

		normal
	}
}
