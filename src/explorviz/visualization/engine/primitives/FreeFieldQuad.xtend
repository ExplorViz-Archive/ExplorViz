package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.math.Vector3f

class FreeFieldQuad extends Quad {
	new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT) {
		super(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT)
	}
	
	override getVertices() {
		super.vertices
	}
	
	override isHighlighted() {
		false
	}
	
	override highlight(Vector4f color) {
	}
	
	override unhighlight() {
	}
	
	override moveByVector(Vector3f vector) {
	}
	
}