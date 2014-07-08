package explorviz.visualization.engine.primitives

import java.util.ArrayList
import explorviz.visualization.engine.math.Vector3f
import elemental.html.WebGLTexture
import explorviz.visualization.engine.math.Vector4f

import static extension explorviz.visualization.main.ArrayExtensions.*
import explorviz.visualization.engine.buffer.BufferManager

class Quad extends PrimitiveObject {
	@Property val float[] vertices = createFloatArray(6 * 3)
	@Property val cornerPoints = new ArrayList<Vector3f>(4)

	private val float[] color = createFloatArray(6 * 3)
	private val boolean transparent
	private val boolean drawWithoutDepthTest
	
	public val int offsetStart

	@Property WebGLTexture texture
	private var boolean highlighted = false

	new(Vector3f center, Vector3f extensionInEachDirection, WebGLTexture texture, Vector4f color) {
		val BOTTOM_LEFT = new Vector3f(center.x - extensionInEachDirection.x, center.y - extensionInEachDirection.y,
			center.z - extensionInEachDirection.z)
		val BOTTOM_RIGHT = new Vector3f(center.x + extensionInEachDirection.x, center.y - extensionInEachDirection.y,
			center.z + extensionInEachDirection.z)
		val TOP_RIGHT = new Vector3f(center.x + extensionInEachDirection.x, center.y + extensionInEachDirection.y,
			center.z + extensionInEachDirection.z)
		val TOP_LEFT = new Vector3f(center.x - extensionInEachDirection.x, center.y + extensionInEachDirection.y,
			center.z - extensionInEachDirection.z)

		this.transparent = false
		this.drawWithoutDepthTest = false
		offsetStart = createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, color)
	}

	new(Vector3f center, Vector3f extensionInEachDirection, WebGLTexture texture, Vector4f color, boolean transparent, boolean drawWithoutDepthTest) {
		val BOTTOM_LEFT = new Vector3f(center.x - extensionInEachDirection.x, center.y - extensionInEachDirection.y,
			center.z - extensionInEachDirection.z)
		val BOTTOM_RIGHT = new Vector3f(center.x + extensionInEachDirection.x, center.y - extensionInEachDirection.y,
			center.z + extensionInEachDirection.z)
		val TOP_RIGHT = new Vector3f(center.x + extensionInEachDirection.x, center.y + extensionInEachDirection.y,
			center.z + extensionInEachDirection.z)
		val TOP_LEFT = new Vector3f(center.x - extensionInEachDirection.x, center.y + extensionInEachDirection.y,
			center.z - extensionInEachDirection.z)

		this.transparent = transparent
		this.drawWithoutDepthTest = drawWithoutDepthTest
		offsetStart = createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, color)
	}
	
	// for Labels
	new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, float textureCoordStartX, float textureCoordStartY, float textureDim) {
		this.transparent = true
		this.drawWithoutDepthTest = true
		offsetStart = createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, null, textureCoordStartX, textureCoordStartY, textureDim)
	}

	new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, Vector4f color) {
		this.transparent = false
		this.drawWithoutDepthTest = false
		offsetStart = createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, null, color)
	}

	new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, Vector4f color,
		boolean transparent, boolean drawWithoutDepthTest) {
		this.transparent = transparent
		this.drawWithoutDepthTest = drawWithoutDepthTest
		offsetStart = createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, null, color)
	}

	new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, WebGLTexture texture) {
		this.transparent = false
		this.drawWithoutDepthTest = false
		offsetStart = createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, null)
	}

	new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, WebGLTexture texture,
		boolean transparent, boolean drawWithoutDepthTest) {
		this.transparent = transparent
		this.drawWithoutDepthTest = drawWithoutDepthTest
		offsetStart = createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, null)
	}

	new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, WebGLTexture texture,
		Vector4f color) {
		this.transparent = false
		this.drawWithoutDepthTest = false
		offsetStart = createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, color)
	}

	def private int createFrom4Vector3f(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT,
		WebGLTexture texture, Vector4f colorVec) {
			createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, colorVec, 0,0,1f)
	}
				
	def private int createFrom4Vector3f(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT,
		WebGLTexture texture, Vector4f colorVec, float textureCoordStartX, float textureCoordStartY, float textureDim) {
		this.texture = texture
		cornerPoints.add(BOTTOM_LEFT)
		cornerPoints.add(BOTTOM_RIGHT)
		cornerPoints.add(TOP_RIGHT)
		cornerPoints.add(TOP_LEFT)

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
			color.set(12, colorVec.x)
			color.set(13, colorVec.y)
			color.set(14, colorVec.z)
			color.set(15, colorVec.w)
			color.set(16, colorVec.x)
			color.set(17, colorVec.y)
			color.set(18, colorVec.z)
			color.set(19, colorVec.w)
			color.set(20, colorVec.x)
			color.set(21, colorVec.y)
			color.set(22, colorVec.z)
			color.set(23, colorVec.w)
		}

		vertices.set(0, BOTTOM_LEFT.x)
		vertices.set(1, BOTTOM_LEFT.y)
		vertices.set(2, BOTTOM_LEFT.z)
		vertices.set(3, BOTTOM_RIGHT.x)
		vertices.set(4, BOTTOM_RIGHT.y)
		vertices.set(5, BOTTOM_RIGHT.z)
		vertices.set(6, TOP_RIGHT.x)
		vertices.set(7, TOP_RIGHT.y)
		vertices.set(8, TOP_RIGHT.z)
		vertices.set(9, TOP_RIGHT.x)
		vertices.set(10, TOP_RIGHT.y)
		vertices.set(11, TOP_RIGHT.z)
		vertices.set(12, TOP_LEFT.x)
		vertices.set(13, TOP_LEFT.y)
		vertices.set(14, TOP_LEFT.z)
		vertices.set(15, BOTTOM_LEFT.x)
		vertices.set(16, BOTTOM_LEFT.y)
		vertices.set(17, BOTTOM_LEFT.z)

		val float[] textureCoords = createFloatArray(6 * 2)
		textureCoords.set(0, textureCoordStartX)
		textureCoords.set(1, textureCoordStartY + textureDim)
		textureCoords.set(2, textureCoordStartX + textureDim)
		textureCoords.set(3, textureCoordStartY + textureDim)
		textureCoords.set(4, textureCoordStartX + textureDim)
		textureCoords.set(5, textureCoordStartY)
		textureCoords.set(6, textureCoordStartX + textureDim)
		textureCoords.set(7, textureCoordStartY)
		textureCoords.set(8, textureCoordStartX)
		textureCoords.set(9, textureCoordStartY)
		textureCoords.set(10, textureCoordStartX)
		textureCoords.set(11, textureCoordStartY + textureDim)

		val normal = calculateNormal(vertices, 6)
		addToBuffer(textureCoords, normal)
	}

	def private int addToBuffer(float[] textureCoords, float[] normal) {
		BufferManager::addQuad(vertices, textureCoords, color, normal)
	}

	override final void draw() {
		BufferManager::drawQuad(offsetStart, texture, transparent, drawWithoutDepthTest)
	}

	override highlight(Vector4f color) {
		highlighted = true

		val highlightColor = createFloatArray(6 * 4)

		for (var int i = 0; i < 6; i++) {
			highlightColor.set(0 + i * 4, color.x)
			highlightColor.set(1 + i * 4, color.y)
			highlightColor.set(2 + i * 4, color.z)
			highlightColor.set(3 + i * 4, color.w)
		}

		BufferManager::overrideColor(offsetStart, highlightColor)
	}

	override unhighlight() {
		highlighted = false

		BufferManager::overrideColor(offsetStart, color)
	}

	override moveByVector(Vector3f vector) {
	}

	override toString() {
		var result = ""
		for (cornerPoint : cornerPoints) {
			result = result + cornerPoint + " "
		}

		result
	}

	override isHighlighted() {
		highlighted
	}

}
