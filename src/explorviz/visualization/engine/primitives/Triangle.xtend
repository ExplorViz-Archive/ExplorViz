package explorviz.visualization.engine.primitives

import elemental.html.WebGLTexture
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f

import static extension explorviz.visualization.main.ArrayExtensions.*
import org.eclipse.xtend.lib.annotations.Accessors

class Triangle extends PrimitiveObject {
	@Accessors val float[] vertices = createFloatArray(3 * 3)
	
	private val float[] color = createFloatArray(4 * 3)
	public val int offsetStart
	private val boolean transparent
	private val boolean drawWithoutDepthTest
	
	@Accessors var boolean blinking = false
	private var long lastBlinkTimestamp = 0
	
	@Accessors WebGLTexture texture
	private var highlighted = false
	
	new(WebGLTexture texture, Vector4f colorVec, boolean transparent, boolean drawWithoutDepthTest, Vector3f p1, Vector3f p2, Vector3f p3, float s1, float t1, float s2, float t2, float s3, float t3) {
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
		
		vertices.set(0, p1.x)
		vertices.set(1, p1.y)
		vertices.set(2, p1.z)
		vertices.set(3, p2.x)
		vertices.set(4, p2.y)
		vertices.set(5, p2.z)
		vertices.set(6, p3.x)
		vertices.set(7, p3.y)
		vertices.set(8, p3.z)
		
		val float[] textureCoords = createFloatArray(2 * 3)
		
		textureCoords.set(0, s1)
		textureCoords.set(1, t1)
		textureCoords.set(2, s2)
		textureCoords.set(3, t2)
		textureCoords.set(4, s3)
		textureCoords.set(5, t3)
		
		val normal = calculateNormal(vertices, 3)
		offsetStart = addToBuffer(textureCoords, normal)
	}

	def private int addToBuffer(float[] textureCoords, float[] normal) {
		BufferManager::addTriangle(vertices, textureCoords, color, normal)
	}

	override final void draw() {
		if (blinking) {
			val currentTime = java.lang.System.currentTimeMillis
			if (lastBlinkTimestamp == 0) {
				lastBlinkTimestamp = currentTime
			}

			if (currentTime < lastBlinkTimestamp + Quad::BLINK_INTERVAL_IN_MILLIS) {
				BufferManager::drawTriangle(offsetStart, texture, transparent, drawWithoutDepthTest)
			} else if (currentTime < lastBlinkTimestamp + Quad::BLINK_INTERVAL_IN_MILLIS * 2) {
				// dont draw
			} else {
				lastBlinkTimestamp = currentTime
			}
		} else {
			BufferManager::drawTriangle(offsetStart, texture, transparent, drawWithoutDepthTest)
		}
	}

	override highlight(Vector4f color) {
		highlighted = true
		
		val highlightColor = createFloatArray(4 * 3)
		highlightColor.set(0, color.x)
		highlightColor.set(1, color.y)
		highlightColor.set(2, color.z)
		highlightColor.set(3, color.w)
		highlightColor.set(4, color.x)
		highlightColor.set(5, color.y)
		highlightColor.set(6, color.z)
		highlightColor.set(7, color.w)
		highlightColor.set(8, color.x)
		highlightColor.set(9, color.y)
		highlightColor.set(10, color.z)
		highlightColor.set(11, color.w)
		BufferManager::overrideColor(offsetStart, highlightColor)
	}

	override unhighlight() {
		highlighted = false
		
		BufferManager::overrideColor(offsetStart, color)
	}

	override moveByVector(Vector3f vector) {
		val newPoints = createFloatArray(3 * 3)
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

	override isHighlighted() {
		highlighted
	}

}
