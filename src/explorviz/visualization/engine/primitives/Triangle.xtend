package explorviz.visualization.engine.primitives

import static extension explorviz.visualization.main.ArrayExtensions.*
import elemental.html.WebGLTexture
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.buffer.BufferManager

class Triangle extends PrimitiveObject {
	protected static val int verticesDimension = 3
	protected static val int verticesMaxLength = verticesDimension * 3
	protected static val int textureCoordsMaxLength = 2 * 3

	protected var started = false
	protected var alreadyClosed = false

	@Property val float[] vertices = createFloatArray(verticesMaxLength)
	@Property val float[] textureCoords = createFloatArray(textureCoordsMaxLength)
	@Property val float[] color = createFloatArray(4)
	@Property val float[] normal = createFloatArray(3)
	
	@Property WebGLTexture texture
	@Property boolean transparent = false

	private var indexVertices = 0
	private var indexTextureCoords = 0
	
	private int offsetStart

	def void begin() {
		started = true
	}

	def void end() {
		if (indexVertices != 0) throw new IllegalArgumentException("not enough points: " + indexVertices)

		alreadyClosed = true
		calculateNormal()
		addToBuffer()
	}

	def void calculateNormal() {
		val v1 = new Vector3f(vertices.get(0), vertices.get(1), vertices.get(2))
		val v2 = new Vector3f(vertices.get(3), vertices.get(4), vertices.get(5))
		val v3 = new Vector3f(vertices.get(6), vertices.get(7), vertices.get(8))

		val normalVector3f = v2.sub(v1).cross(v3.sub(v1)).normalize
		normal.set(0, normalVector3f.x)
		normal.set(1, normalVector3f.y)
		normal.set(2, normalVector3f.z)
	}

	def private void addToBuffer() {
		offsetStart = BufferManager::addTriangle(vertices, textureCoords, color, normal)
	}

	def void addPoint(float x, float y, float z) {
		if (!started) throw new IllegalArgumentException("not yet started")
		if (alreadyClosed) throw new IllegalArgumentException("already closed")

		vertices.setElement(indexVertices, x)
		indexVertices.inc
		vertices.setElement(indexVertices, y)
		indexVertices.inc
		vertices.setElement(indexVertices, z)
		indexVertices.inc

		indexVertices = indexVertices % verticesMaxLength
	}

	def void addPoint(Vector3f point) {
		addPoint(point.x, point.y, point.z)
	}

	def void addTexturePoint(float s, float t) {
		if (!started) throw new IllegalArgumentException("not yet started")
		if (alreadyClosed) throw new IllegalArgumentException("already closed")

		textureCoords.setElement(indexTextureCoords, s)
		indexTextureCoords.inc
		textureCoords.setElement(indexTextureCoords, t)
		indexTextureCoords.inc

		indexTextureCoords = indexTextureCoords % textureCoordsMaxLength
	}

	def setColor(Vector4f color) {
		if (color != null) {
			this.color.set(0, color.x)
			this.color.set(1, color.y)
			this.color.set(2, color.z)
			this.color.set(3, color.w)
		}
	}

	override final void draw() {
		BufferManager::drawTriangle(offsetStart, texture, transparent)
	}

	override highlight(Vector4f color) {
		val highlightColor = createFloatArray(4)
		highlightColor.set(0, color.x)
		highlightColor.set(1, color.y)
		highlightColor.set(2, color.z)
		highlightColor.set(3, color.w)
		BufferManager::overrideColor(offsetStart, highlightColor)
	}

	override unhighlight() {
		BufferManager::overrideColor(offsetStart, color)
	}

	override moveByVector(Vector3f vector) {
		val newPoints = createFloatArray(verticesMaxLength)
		newPoints.set(0, vertices.get(0) + vector.x)
		newPoints.set(1, vertices.get(1) + vector.y)
		newPoints.set(2, vertices.get(2) + vector.z)

		newPoints.set(3, vertices.get(3) + vector.x)
		newPoints.set(4, vertices.get(4) + vector.y)
		newPoints.set(5, vertices.get(5) + vector.z)

		newPoints.set(6, vertices.get(6) + vector.x)
		newPoints.set(7, vertices.get(7) + vector.y)
		newPoints.set(8, vertices.get(8) + vector.z)
		BufferManager::setNewVerticesPosition(offsetStart, newPoints)
	}

	override final reAddToBuffer() {
		addToBuffer()
	}

}
