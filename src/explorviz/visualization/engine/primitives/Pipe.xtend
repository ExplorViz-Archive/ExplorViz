package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import java.util.ArrayList
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.math.Matrix44f

class Pipe extends PrimitiveObject {
	protected static val int verticesDimension = 3

	protected var started = false
	protected var alreadyClosed = false

	var Vector3f lastPoint
	@Property var lineThickness = 0.2f
	@Property var smoothnessEdgeCount = 6 // 16
	var Vector4f color
	@Property var transparent = false
	

	@Property val quads = new ArrayList<Quad>()
	@Property val triangles = new ArrayList<Triangle>()

	def Vector4f getColor() {
		this.color
	}

	def void setColor(Vector4f color) {
		this.color = color
	}

	new() {
	}

	def begin() {
		started = true
	}

	def end() {
		if (quads.size == 0) throw new IllegalArgumentException("At least 2 Points needed")

		alreadyClosed = true
	}

	def addPoint(float x, float y, float z) {
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

	def createSegmentPart(Vector3f v, float degForEachSegment, int index, Vector4f n, Vector3f targetPoint) {
		val firstRotatedSegmentVector = Matrix44f::rotationAxis(v,degForEachSegment * (index - 1)).mult(n).convertTo3f()
		val secondRotatedSegmentVector = Matrix44f::rotationAxis(v,degForEachSegment * index).mult(n).convertTo3f()
		
		createQuad(firstRotatedSegmentVector, secondRotatedSegmentVector, lastPoint, targetPoint)
	}

	def createLineWidthVector(Vector3f v) {
		val n = createNormalFrom2D(v)

		val L = lineThickness / 2f
		scaleToXYLength(n, L)
	}

	def createNormalFrom2D(Vector3f v) {
		new Vector3f(v.z, v.y, -1 * v.x)
	}

	def scaleToXYLength(Vector3f v, float L) {
		new Vector3f((v.x * L) / v.length(), (v.y * L) / v.length(), (v.z * L) / v.length())
	}

	def createQuad(Vector3f firstSegmentVector, Vector3f secondSegmentVector, Vector3f startPoint, Vector3f targetPoint) {
		val BOTTOM_LEFT = startPoint.add(secondSegmentVector)
		val BOTTOM_RIGHT = targetPoint.add(secondSegmentVector)
		val TOP_RIGHT = targetPoint.add(firstSegmentVector)
		val TOP_LEFT = startPoint.add(firstSegmentVector)

		quads.add(new Quad(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, color, transparent))
	}

	def addPoint(Vector3f point) {
		addPoint(point.x, point.y, point.z)
	}

	override draw() {
		quads.forEach([it.draw])
		triangles.forEach([it.draw])
	}

	override getVertices() {
		quads.get(0).vertices
	}

	override highlight(Vector4f color) {
		quads.forEach[it.highlight(color)]
		triangles.forEach[it.highlight(color)]
	}

	override unhighlight() {
		quads.forEach[it.unhighlight()]
		triangles.forEach[it.unhighlight()]
	}

	override moveByVector(Vector3f vector) {
		quads.forEach[it.moveByVector(vector)]
		triangles.forEach[it.moveByVector(vector)]
	}

	override reAddToBuffer() {
		quads.forEach[it.reAddToBuffer()]
		triangles.forEach[it.reAddToBuffer()]
	}

}
