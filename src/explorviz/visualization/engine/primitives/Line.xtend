package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import java.util.ArrayList
import explorviz.visualization.engine.math.Vector4f
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.List

class Line extends PrimitiveObject {
	private static val Vector4f DEFAULT_COLOR = new Vector4f(0f, 0f, 0f, 1f)

	@Accessors val List<Quad> quads = new ArrayList<Quad>(8)
	@Accessors val List<Triangle> triangles = new ArrayList<Triangle>(8)

	private var Vector3f firstPoint
	private var Vector3f secondPoint
	private var Vector3f lastPoint
	private var Vector3f lastV
	private var boolean alreadyOneSegment = false

	@Accessors var float lastLineThickness = 0f
	@Accessors var Vector4f color = DEFAULT_COLOR

	var highlighted = false
	

	def void begin() {
	}

	def void end() {
		if (lastPoint.equals(firstPoint) && secondPoint != null) {
			val v = secondPoint.sub(firstPoint)
			if (alreadyOneSegment) {
				createJointPoint(firstPoint, lastV, v, lastLineThickness)
			}
		}
	}

	def void addPoint(float x, float y, float z, float lineThickness, boolean alreadyDrawn) {
		if (lastPoint == null) {
			lastPoint = new Vector3f(x, y, z)
			firstPoint = lastPoint
		} else {
			val thisPoint = new Vector3f(x, y, z)
			if (!alreadyOneSegment) {
				secondPoint = thisPoint
			}
			val v = thisPoint.sub(lastPoint)
			if (alreadyOneSegment) {
				createJointPoint(lastPoint, lastV, v, Math.max(lastLineThickness,lineThickness))
			}
			lastV = v
			val n_L = createLineWidthVector(v, lineThickness)

			if (!alreadyDrawn) {
				createQuad(n_L, v, lastPoint)
			}

			alreadyOneSegment = true
			lastPoint = thisPoint
		}

		lastLineThickness = lineThickness
	}

	private def createLineWidthVector(Vector3f v, float lineThickness) {
		val n = new Vector3f(v.y, -1 * v.x, v.z)

		val L = lineThickness / 2
		new Vector3f(n.x * L / n.length(), n.y * L / n.length(), n.z * L / n.length())
	}

	private def void createJointPoint(Vector3f jointPoint, Vector3f lastV, Vector3f newV, float lineThickness) {
		val leftLineWidthVector = createLineWidthVector(lastV, lineThickness)
		val rightLineWidthVector = createLineWidthVector(newV, lineThickness)

		val intersectionPoint = jointPoint

		addTriangle(jointPoint.add(leftLineWidthVector), jointPoint.add(rightLineWidthVector), intersectionPoint)
		addTriangle(jointPoint.sub(rightLineWidthVector), jointPoint.sub(leftLineWidthVector), intersectionPoint)
	}

	private def void addTriangle(Vector3f leftPoint, Vector3f rightPoint, Vector3f intersectionPoint) {
		val triangle = new Triangle(null, color, false, false, leftPoint, rightPoint, intersectionPoint, 0f, 1f, 1f, 1f,
			1f, 0f)
		triangles.add(triangle)
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

	override isHighlighted() {
		highlighted
	}

}
