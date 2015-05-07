package explorviz.visualization.engine.primitives

import elemental.html.WebGLTexture
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f

import static extension explorviz.visualization.main.ArrayExtensions.*
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.ArrayList

class Crosshair extends PrimitiveObject {
	@Accessors val float[] vertices = createFloatArray(6 * 3)	
	@Accessors val cornerPoints = new ArrayList<Vector3f>(4)

	private val float[] color = createFloatArray(6 * 3)
	private val boolean transparent
	private val boolean drawWithoutDepthTest

	public val int offsetStart

	@Accessors WebGLTexture texture


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
	
	override isHighlighted() {false}
	
	override highlight(Vector4f color) {}
	
	override unhighlight() {}
	
	override moveByVector(Vector3f vector) {}

}
