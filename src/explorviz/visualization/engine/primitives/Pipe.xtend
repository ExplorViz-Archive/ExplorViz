package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import java.util.ArrayList
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.math.Matrix44f

class Pipe extends PrimitiveObject {
	protected static val int verticesDimension = 3
	private static val smoothnessEdgeCount = 2

	protected var started = false
	protected var alreadyClosed = false
	private var Vector3f lastPoint
	
	@Property var lineThickness = 0.2f
	@Property var Vector4f color
	@Property var transparent = false

	@Property val quads = new ArrayList<Quad>(1)

	def begin() {
		started = true
	}

	def end() {
		if (quads.size == 0) throw new IllegalArgumentException("At least 2 Points needed")

		alreadyClosed = true
	}

	def void addPoint(float x, float y, float z) {
		if (!started) throw new IllegalArgumentException("not yet started")
		if (alreadyClosed) throw new IllegalArgumentException("already closed")

		if (lastPoint == null) {
			lastPoint = new Vector3f(x, y, z)
		} else {
			val thisPoint = new Vector3f(x, y, z)
			val v = thisPoint.sub(lastPoint)

			val n = new Vector4f(createLineWidthVector(v), 0)
			val degForEachSegment = 360f / smoothnessEdgeCount * -1f

			var i = 1
			while (i <= smoothnessEdgeCount) {
				createSegmentPart(v, degForEachSegment, i, n, thisPoint)
				i = i + 1
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

	private def void createQuad(Vector3f firstSegmentVector, Vector3f secondSegmentVector, Vector3f startPoint, Vector3f targetPoint) {
		val BOTTOM_LEFT = startPoint.add(secondSegmentVector)
		val BOTTOM_RIGHT = targetPoint.add(secondSegmentVector)
		val TOP_RIGHT = targetPoint.add(firstSegmentVector)
		val TOP_LEFT = startPoint.add(firstSegmentVector)

		quads.add(new Quad(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, color, transparent))
	}

	def void addPoint(Vector3f point) {
		addPoint(point.x, point.y, point.z)
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
		quads.forEach[it.highlight(color)]
	}

	override unhighlight() {
		quads.forEach[it.unhighlight()]
	}

	override moveByVector(Vector3f vector) {
		quads.forEach[it.moveByVector(vector)]
	}

	override reAddToBuffer() {
		quads.forEach[it.reAddToBuffer()]
	}

}
