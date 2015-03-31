package explorviz.visualization.engine.primitives

import elemental.html.WebGLTexture
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f

import static extension explorviz.visualization.main.ArrayExtensions.*
import org.eclipse.xtend.lib.annotations.Accessors

class MouseCursor extends PrimitiveObject {
	@Accessors var float[] vertices = createFloatArray(3 * 3)
	
	private val float[] initialVertices = createFloatArray(3 * 3)
	private val float[] color = createFloatArray(4 * 3)
	public val int offsetStart
	private val boolean transparent
	private val boolean drawWithoutDepthTest
	private var Quad quad
	
	@Accessors WebGLTexture texture
	private var highlighted = false
	
	new(WebGLTexture texture, Vector4f colorVec, boolean transparent, boolean drawWithoutDepthTest, Vector3f p, float s1, float t1, float s2, float t2, float s3, float t3) {
		this.texture = texture
		
		if (colorVec != null) {
			color.set(0, colorVec.x)
			color.set(1, colorVec.y)
			color.set(2, colorVec.z)
			color.set(3, colorVec.w)
			color.set(4, colorVec.x)
			color.set(5, colorVec.y)
			color.set(6, colorVec.z)
			color.set(7, colorVec.w)
			color.set(8, colorVec.x)
			color.set(9, colorVec.y)
			color.set(10, colorVec.z)
			color.set(11, colorVec.w)
		}
		
		this.transparent = transparent
		this.drawWithoutDepthTest = drawWithoutDepthTest
		
		vertices.set(0, p.x - 0.2f)
		vertices.set(1, p.y - 0.9f)
		vertices.set(2, p.z)
		vertices.set(3, p.x + 0.65f)
		vertices.set(4, p.y - 0.55f)
		vertices.set(5, p.z)
		vertices.set(6, p.x)
		vertices.set(7, p.y)
		vertices.set(8, p.z)
		
		initialVertices.set(0, p.x - 0.2f)
		initialVertices.set(1, p.y - 0.9f)
		initialVertices.set(2, p.z)
		initialVertices.set(3, p.x + 0.65f)
		initialVertices.set(4, p.y - 0.55f)
		initialVertices.set(5, p.z)
		initialVertices.set(6, p.x)
		initialVertices.set(7, p.y)
		initialVertices.set(8, p.z)
		
		val float[] textureCoords = createFloatArray(2 * 3)
		
		textureCoords.set(0, s1)
		textureCoords.set(1, t1)
		textureCoords.set(2, s2)
		textureCoords.set(3, t2)
		textureCoords.set(4, s3)
		textureCoords.set(5, t3)
		
		val normal = calculateNormal(vertices, 3)
		offsetStart = addToBuffer(textureCoords, normal)	
		
		val bot_left = new Vector3f(vertices.get(0) + 0.4f, vertices.get(1) - 0.325f, p.z)		
		val bot_right = new Vector3f(vertices.get(3) - 0.2f, vertices.get(1) - 0.225f, p.z)
		val top_left = new Vector3f(vertices.get(0) + 0.3f, vertices.get(1) + 0.1f, p.z)
		val top_right = new Vector3f(vertices.get(3) - 0.3f, vertices.get(1) + 0.3f, p.z)
		
		quad = new Quad(bot_left, bot_right, top_right, top_left, colorVec, transparent, drawWithoutDepthTest)		

	}

	def private int addToBuffer(float[] textureCoords, float[] normal) {
		BufferManager::addTriangle(vertices, textureCoords, color, normal)
	}

	override final void draw() {
		BufferManager::drawTriangle(offsetStart, texture, transparent, drawWithoutDepthTest)
		quad.draw()
	}

	override highlight(Vector4f color) {}

	override unhighlight() {}

	override moveByVector(Vector3f vector) {		
		vertices.set(0, vertices.get(0) + vector.x)
		vertices.set(1, vertices.get(1) - vector.y)
		vertices.set(2, vertices.get(2) + vector.z)

		vertices.set(3, vertices.get(3) + vector.x)
		vertices.set(4, vertices.get(4) - vector.y)
		vertices.set(5, vertices.get(5) + vector.z)

		vertices.set(6, vertices.get(6) + vector.x)
		vertices.set(7, vertices.get(7) - vector.y)
		vertices.set(8, vertices.get(8) + vector.z)
		
		BufferManager::setNewVerticesPosition(offsetStart, vertices, 3)
		quad.moveByVector(vector)		
	}

	override isHighlighted() {
		highlighted
	}
	
	def void resetCoordinates() {
		vertices.set(0, initialVertices.get(0))
		vertices.set(1, initialVertices.get(1))
		vertices.set(2, initialVertices.get(2))
		
		vertices.set(3, initialVertices.get(3))
		vertices.set(4, initialVertices.get(4))
		vertices.set(5, initialVertices.get(5))
		
		vertices.set(6, initialVertices.get(6))
		vertices.set(7, initialVertices.get(7))
		vertices.set(8, initialVertices.get(8))
		
		BufferManager::setNewVerticesPosition(offsetStart, vertices, 3)
		quad.resetCoordinates()	
	}

}
