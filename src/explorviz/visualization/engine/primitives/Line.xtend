package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import java.util.ArrayList
import explorviz.visualization.engine.math.Vector4f

class Line extends PrimitiveObject {
	protected static val int verticesDimension = 3
	private static val DEFAULT_COLOR = new Vector4f(0f, 0f, 0f, 1f)

	protected var started = false
	protected var alreadyClosed = false

	private var Vector3f firstPoint
	private var Vector3f secondPoint
	private var Vector3f lastPoint
	private var Vector3f lastV
	
	@Property var lineThickness = 0.01f
	@Property var stippelWidth = 0.1f
	@Property var stippelGapWidth = 0.05f
	@Property var stippeled = false
	
	var highlighted = false
	
	var color = DEFAULT_COLOR

	@Property val quads = new ArrayList<Quad>(8)
	@Property val triangles = new ArrayList<Triangle>(8)

	def Vector4f getColor() {
		this.color
	}

	def void setColor(Vector4f color) {
		this.color = color
	}

	def void begin() {
		started = true
	}

	def void end() {
		if (quads.size == 0) throw new IllegalArgumentException("At least 2 Points needed")

		if (lastPoint.equals(firstPoint) && secondPoint != null) {
			val v = secondPoint.sub(firstPoint)
			if (quads.size >= 1 && !stippeled) {
				createJointPoint(firstPoint, lastV, v)
			}
		}

		alreadyClosed = true
	}

	def void addPoint(float x, float y, float z) {
		if (!started) throw new IllegalArgumentException("not yet started")
		if (alreadyClosed) throw new IllegalArgumentException("already closed")

		if (lastPoint == null) {
			lastPoint = new Vector3f(x, y, z)
			firstPoint = lastPoint
		} else {
			val thisPoint = new Vector3f(x, y, z)
			if (quads.size == 0) {
				secondPoint = thisPoint
			}
			val v = thisPoint.sub(lastPoint)
			if (quads.size >= 1 && !stippeled) {
				createJointPoint(lastPoint, lastV, v)
			}
			lastV = v
			val n_L = createLineWidthVector(v)

			if (!stippeled) {
				createQuad(n_L, v, lastPoint)
			} else {
				var stippelPart = v.scaleToLength(stippelWidth)
				var halfOfStippelPart = stippelPart.div(2.0f)
				var gapPart = v.scaleToLength(stippelGapWidth)
				var alreadyAdded = new Vector3f(0, 0, 0).add(halfOfStippelPart).add(gapPart)
				createQuad(n_L, halfOfStippelPart, lastPoint)

				while (abs(v.x) >= abs(alreadyAdded.x) && abs(v.y) >= abs(alreadyAdded.y)) {
					createQuad(n_L, stippelPart, lastPoint)
					lastPoint = lastPoint.add(stippelPart).add(gapPart)
					alreadyAdded = alreadyAdded.add(stippelPart).add(gapPart)
				}
				alreadyAdded.sub(stippelPart).sub(gapPart)
				createQuad(n_L, v.sub(alreadyAdded).add(stippelPart), lastPoint)
			}
			lastPoint = thisPoint
		}
	}

	private def createLineWidthVector(Vector3f v) {
		val n = new Vector3f(v.y, -1 * v.x, v.z)

		val L = lineThickness / 2
		new Vector3f(n.x * L / n.length(), n.y * L / n.length(), n.z * L / n.length())
	}

	private def void createJointPoint(Vector3f jointPoint, Vector3f lastV, Vector3f newV) {
		val leftLineWidthVector = createLineWidthVector(lastV)
		val rightLineWidthVector = createLineWidthVector(newV)

		val intersectionPoint = jointPoint

		addTriangle(jointPoint.add(leftLineWidthVector), jointPoint.add(rightLineWidthVector), intersectionPoint)
		addTriangle(jointPoint.sub(rightLineWidthVector), jointPoint.sub(leftLineWidthVector), intersectionPoint)
	}

	private def void addTriangle(Vector3f leftPoint, Vector3f rightPoint, Vector3f intersectionPoint) {
		val triangle = new Triangle()
		triangle.begin
		triangle.color = getColor()

		triangle.addPoint(leftPoint)
		triangle.addTexturePoint(0f, 1f)

		triangle.addPoint(rightPoint)
		triangle.addTexturePoint(1f, 1f)

		triangle.addPoint(intersectionPoint)
		triangle.addTexturePoint(1f, 0f)
		triangle.end

		triangles.add(triangle)
	}

	private def float abs(float f) {
		if (f < 0) -1 * f else f
	}

	def scaleToLength(Vector3f v, float L) {
		new Vector3f((v.x * L) / v.length(), (v.y * L) / v.length(), (v.z * L) / v.length());
	}

	private def void createQuad(Vector3f lineWidthVector, Vector3f v, Vector3f startPoint) {
		val BOTTOM_LEFT = startPoint.sub(lineWidthVector)
		val BOTTOM_RIGHT = startPoint.add(lineWidthVector)
		val TOP_LEFT = BOTTOM_LEFT.add(v)
		val TOP_RIGHT = BOTTOM_RIGHT.add(v)

		quads.add(new Quad(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, color))
	}

	def void addPoint(Vector3f point) {
		addPoint(point.x, point.y, point.z)
	}

	override final void draw() {
		quads.forEach([it.draw])
		triangles.forEach([it.draw])
	}

	override getVertices() {
		quads.get(0).vertices
	}

	override highlight(Vector4f color) {
		highlighted = true
		
		quads.forEach[it.highlight(color)]
		triangles.forEach[it.highlight(color)]
	}

	override unhighlight() {
		highlighted = false
		
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
	
	override isHighlighted() {
		highlighted
	}

}
