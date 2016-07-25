package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import java.util.ArrayList
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.math.Matrix44f
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

class Pipe extends PrimitiveObject {
	public static val smoothnessQuadsCount = 1
	private static var smoothnessEdgeCount = 2
	@Accessors val List<Quad> quads = new ArrayList<Quad>(smoothnessQuadsCount)

	private var Vector3f lastPoint

	private var boolean highlighted = false

	@Accessors var float lineThickness = 0.2f
	val Vector4f color
	val boolean drawWithoutDepthTest
	val boolean transparent

	new(boolean transparent, boolean drawWithoutDepthTest, Vector4f color) {
		this.transparent = transparent
		this.drawWithoutDepthTest = drawWithoutDepthTest
		this.color = color
	}

	def void addPoint(Vector3f point) {
		if (lastPoint == null) {
			lastPoint = point
		} else {
			val thisPoint = point
			val v = thisPoint.sub(lastPoint)

			val n = new Vector4f(createLineWidthVector(v), 0)
			if (smoothnessEdgeCount == 2) {
				val normal = new Vector3f(n.x, n.y, n.z)
				createQuad(normal, new Vector3f(n.x * -1, n.y, n.z * -1), lastPoint, thisPoint)
			} else {
				val degForEachSegment = 360f / smoothnessEdgeCount * -1f
				for (var int i = 0; i <= smoothnessEdgeCount; i++) {
					createSegmentPart(v, degForEachSegment, i, n, thisPoint)
				}
			}

			lastPoint = thisPoint
		}
	}

	private def createSegmentPart(Vector3f v, float degForEachSegment, int index, Vector4f n, Vector3f targetPoint) {
		val firstRotatedSegmentVector = Matrix44f::rotationAxis(v, degForEachSegment * (index - 1)).mult(n).
			convertTo3f()
		val secondRotatedSegmentVector = Matrix44f::rotationAxis(v, degForEachSegment * index).mult(n).convertTo3f()

		createQuad(firstRotatedSegmentVector, secondRotatedSegmentVector, lastPoint, targetPoint)
	}

	private def createLineWidthVector(Vector3f v) {
		val n = createNormalFrom2D(v)

		val L = lineThickness / 2f
		scaleToXYLength(n, L)
	}

	private def createNormalFrom2D(Vector3f v) {
		new Vector3f(v.z, v.y, -1 * v.x)
	}

	private def scaleToXYLength(Vector3f v, float L) {
		new Vector3f((v.x * L) / v.length(), (v.y * L) / v.length(), (v.z * L) / v.length())
	}

	private def void createQuad(Vector3f firstSegmentVector, Vector3f secondSegmentVector, Vector3f startPoint,
		Vector3f targetPoint) {
		val BOTTOM_LEFT = startPoint.add(secondSegmentVector)
		val BOTTOM_RIGHT = targetPoint.add(secondSegmentVector)
		val TOP_RIGHT = targetPoint.add(firstSegmentVector)
		val TOP_LEFT = startPoint.add(firstSegmentVector)

		quads.add(new Quad(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, color, false, drawWithoutDepthTest))
	}

	override final void draw() {
		for (quad : quads) {
			quad.draw()
		}
	}

	override getVertices() {

		// not used
		null
	}

	override highlight(Vector4f color) {
		highlighted = true
		for (quad : quads)
			quad.highlight(color)
	}

	override unhighlight() {
		highlighted = false
		for (quad : quads)
			quad.unhighlight
	}

	override moveByVector(Vector3f vector) {
		for (quad : quads)
			quad.moveByVector(vector)
	}

	override isHighlighted() {
		highlighted
	}



	def getColor() {
		return PipeNative::getColor(this)
	}

}
