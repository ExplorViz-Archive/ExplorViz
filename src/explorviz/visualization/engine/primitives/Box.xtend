package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.primitives.PrimitiveObject
import java.util.ArrayList
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f

class Box extends PrimitiveObject {
	@Property val quads = new ArrayList<Quad>(3)

	public val Vector3f center
	public val Vector3f extensionInEachDirection
	public val Vector4f color

	var boolean highlighted = false

	new(Vector3f center, Vector3f extensionInEachDirection, Vector4f color) {
		this.center = center
		this.extensionInEachDirection = extensionInEachDirection
		this.color = color

		// from the viewpoint of the front!
		val pointFrontBottomLeft = new Vector3f(center.x - extensionInEachDirection.x,
			center.y - extensionInEachDirection.y, center.z + extensionInEachDirection.z)
		val pointFrontBottomRight = new Vector3f(center.x + extensionInEachDirection.x,
			center.y - extensionInEachDirection.y, center.z + extensionInEachDirection.z)
		val pointFrontTopRight = new Vector3f(center.x + extensionInEachDirection.x,
			center.y + extensionInEachDirection.y, center.z + extensionInEachDirection.z)
		val pointFrontTopLeft = new Vector3f(center.x - extensionInEachDirection.x,
			center.y + extensionInEachDirection.y, center.z + extensionInEachDirection.z)

		// from the viewpoint of the back!
		val pointBackBottomRight = new Vector3f(center.x - extensionInEachDirection.x,
			center.y - extensionInEachDirection.y, center.z - extensionInEachDirection.z)
		val pointBackTopRight = new Vector3f(center.x - extensionInEachDirection.x,
			center.y + extensionInEachDirection.y, center.z - extensionInEachDirection.z)
		val pointBackTopLeft = new Vector3f(center.x + extensionInEachDirection.x, center.y + extensionInEachDirection.y,
			center.z - extensionInEachDirection.z)

		val quadFront = new Quad(pointFrontBottomLeft, pointFrontBottomRight, pointFrontTopRight, pointFrontTopLeft,
			color)
		quads.add(quadFront)

		val quadUpper = new Quad(pointFrontTopLeft, pointFrontTopRight, pointBackTopLeft, pointBackTopRight, color)
		quads.add(quadUpper)

		val quadLeft = new Quad(pointBackBottomRight, pointFrontBottomLeft, pointFrontTopLeft, pointBackTopRight,
			color)
		quads.add(quadLeft)

	}

	override final void draw() {
		for (quad : quads) {
			quad.draw()
		}
	}

	override getVertices() {
		quads.get(0).vertices
	}

	override highlight(Vector4f color) {
		highlighted = true

		for (quad : quads) {
			quad.highlight(color)
		}
	}

	override unhighlight() {
		highlighted = false

		for (quad : quads) {
			quad.unhighlight()
		}
	}

	override moveByVector(Vector3f vector) {
		quads.forEach([it.moveByVector(vector)])
	}

	override isHighlighted() {
		highlighted
	}

}
