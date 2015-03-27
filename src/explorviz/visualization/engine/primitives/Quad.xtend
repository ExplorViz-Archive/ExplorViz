package explorviz.visualization.engine.primitives

import java.util.ArrayList
import explorviz.visualization.engine.math.Vector3f
import elemental.html.WebGLTexture
import explorviz.visualization.engine.math.Vector4f

import static extension explorviz.visualization.main.ArrayExtensions.*
import explorviz.visualization.engine.buffer.BufferManager
import org.eclipse.xtend.lib.annotations.Accessors

class Quad extends PrimitiveObject {
	@Accessors val float[] vertices = createFloatArray(6 * 3)
	@Accessors val cornerPoints = new ArrayList<Vector3f>(4)

	private val float[] color = createFloatArray(6 * 3)
	private val boolean transparent
	private val boolean drawWithoutDepthTest

	public val int offsetStart

	@Accessors WebGLTexture texture
	private var boolean highlighted = false

	// for free field quad
	protected new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT) {
		this.transparent = false
		this.drawWithoutDepthTest = false
		this.offsetStart = 0
		
		createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, null, new Vector4f(1f,0f,0f,1f), 0, 0, 1f, 1f, false)
	}

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

	new(Vector3f center, Vector3f extensionInEachDirection, WebGLTexture texture, Vector4f color, boolean transparent,
		boolean drawWithoutDepthTest) {
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
	new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, float textureCoordStartX,
		float textureCoordStartY, float textureDimX, float textureDimY) {
		this.transparent = true
		this.drawWithoutDepthTest = true
		offsetStart = createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, null,
			textureCoordStartX, textureCoordStartY, textureDimX, textureDimY, true)
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

	def private int createFrom4Vector3f(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT,
		Vector3f TOP_LEFT, WebGLTexture texture, Vector4f colorVec) {
		createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, colorVec, 0, 0, 1f, 1f, true)
	}

	def private int createFrom4Vector3f(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT,
		Vector3f TOP_LEFT, WebGLTexture texture, Vector4f colorVec, float textureCoordStartX, float textureCoordStartY,
		float textureDimX, float textureDimY, boolean addToBuffer) {
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
		textureCoords.set(1, textureCoordStartY + textureDimY)
		textureCoords.set(2, textureCoordStartX + textureDimX)
		textureCoords.set(3, textureCoordStartY + textureDimY)
		textureCoords.set(4, textureCoordStartX + textureDimX)
		textureCoords.set(5, textureCoordStartY)
		textureCoords.set(6, textureCoordStartX + textureDimX)
		textureCoords.set(7, textureCoordStartY)
		textureCoords.set(8, textureCoordStartX)
		textureCoords.set(9, textureCoordStartY)
		textureCoords.set(10, textureCoordStartX)
		textureCoords.set(11, textureCoordStartY + textureDimY)

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
		vertices.set(0, vertices.get(0) + vector.x)
		vertices.set(1, vertices.get(1) - vector.y)
		vertices.set(2, vertices.get(2) + vector.z)

		vertices.set(3, vertices.get(3) + vector.x)
		vertices.set(4, vertices.get(4) - vector.y)
		vertices.set(5, vertices.get(5) + vector.z)

		vertices.set(6, vertices.get(6) + vector.x)
		vertices.set(7, vertices.get(7) - vector.y)
		vertices.set(8, vertices.get(8) + vector.z)
		
		vertices.set(9, vertices.get(9) + vector.x)
		vertices.set(10, vertices.get(10) - vector.y)
		vertices.set(11, vertices.get(11) + vector.z)
		
		vertices.set(12, vertices.get(12) + vector.x)
		vertices.set(13, vertices.get(13) - vector.y)
		vertices.set(14, vertices.get(14) + vector.z)
		
		vertices.set(15, vertices.get(15) + vector.x)
		vertices.set(16, vertices.get(16) - vector.y)
		vertices.set(17, vertices.get(17) + vector.z)
		
		BufferManager::setNewVerticesPosition(offsetStart, vertices, 6)
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
