package explorviz.visualization.engine.primitives

import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.textures.TextureManager

class Label extends PrimitiveObject {
	protected val List<Quad> letters = new ArrayList<Quad>()

	static val MINIMUM_LETTER_SIZE = 1.75f
	static val SPACE_BETWEEN_LETTERS_IN_PERCENT = 0.05f

	protected new(String text, Vector3f LEFT_BOTTOM, Vector3f RIGHT_BOTTOM, Vector3f RIGHT_TOP, Vector3f LEFT_TOP,
		boolean downwards, boolean isClazz) {
		if (downwards) {
			val maxAvailableLength = Math.abs(RIGHT_TOP.z - LEFT_BOTTOM.z)

			var quadSize = Math.abs(RIGHT_TOP.x - RIGHT_BOTTOM.x)
			var requiredLength = calculateRequiredLength(text, quadSize)

			if (requiredLength > maxAvailableLength) {
				quadSize = maxAvailableLength / (((text.length * 0.5f) + ((text.length - 1) * SPACE_BETWEEN_LETTERS_IN_PERCENT))) as float

				if (quadSize < MINIMUM_LETTER_SIZE) {
					quadSize = MINIMUM_LETTER_SIZE
				}

				requiredLength = calculateRequiredLength(text, quadSize)
			}

			val X = LEFT_BOTTOM.x + Math.abs(RIGHT_TOP.x - RIGHT_BOTTOM.x) / 2f - quadSize / 2f
			val Y = LEFT_BOTTOM.y
			val Z_START = (LEFT_BOTTOM.z + maxAvailableLength / 2f - (requiredLength / 2f) - (quadSize * 0.25f)) as float

			for (var int i = 0; i < text.length; i++) {
				var offset =  ((0.5f - SPACE_BETWEEN_LETTERS_IN_PERCENT) * quadSize) as float
				val zPosition = (quadSize - offset) * i
				letters.add(
					createLetter(
						text.charAt(i),
						new Vector3f(X, Y, Z_START + zPosition),
						new Vector3f(X, Y, Z_START + zPosition + quadSize),
						new Vector3f(X + quadSize, Y, Z_START + zPosition + quadSize),
						new Vector3f(X + quadSize, Y, Z_START + zPosition)
					)
				)
			}
		} else {
			var QUAD_X_DIST = Math.abs(RIGHT_TOP.sub(RIGHT_BOTTOM).x)
			QUAD_X_DIST = QUAD_X_DIST - QUAD_X_DIST * 0.4f // narrower text

			var QUAD_Z_DIST = Math.abs(RIGHT_TOP.sub(RIGHT_BOTTOM).z)
			QUAD_Z_DIST = QUAD_Z_DIST - QUAD_Z_DIST * 0.4f // narrower text

			val requiredXLength = QUAD_X_DIST * text.length
			val TOP_X_START = LEFT_TOP.x + Math.abs(RIGHT_TOP.x - LEFT_TOP.x) / 2f - (requiredXLength / 2f)
			val BOTTOM_X_START = LEFT_BOTTOM.x + Math.abs(RIGHT_BOTTOM.x - LEFT_BOTTOM.x) / 2f - (requiredXLength / 2f)

			val requiredZLength = QUAD_Z_DIST * text.length
			val TOP_Z_START = LEFT_TOP.z + Math.abs(RIGHT_TOP.z - LEFT_TOP.z) / 2f - (requiredZLength / 2f)
			val BOTTOM_Z_START = LEFT_BOTTOM.z + Math.abs(RIGHT_BOTTOM.z - LEFT_BOTTOM.z) / 2f - (requiredZLength / 2f)

			for (var int i = 0; i < text.length; i++) {
				letters.add(
					createLetter(
						text.charAt(i),
						new Vector3f(BOTTOM_X_START + QUAD_X_DIST * i, LEFT_BOTTOM.y, BOTTOM_Z_START + QUAD_Z_DIST * i),
						new Vector3f(BOTTOM_X_START + QUAD_X_DIST * (i + 1), RIGHT_BOTTOM.y,
							BOTTOM_Z_START + QUAD_Z_DIST * (i + 1)),
						new Vector3f(TOP_X_START + QUAD_X_DIST * (i + 1), RIGHT_TOP.y,
							TOP_Z_START + QUAD_Z_DIST * (i + 1)),
						new Vector3f(TOP_X_START + QUAD_X_DIST * i, LEFT_TOP.y, TOP_Z_START + QUAD_Z_DIST * i)
					)
				)
			}
		}
	}
	
	private def float calculateRequiredLength(String text, float quadSize) {
		((text.length * quadSize * 0.5f) + ((text.length - 1) * quadSize * SPACE_BETWEEN_LETTERS_IN_PERCENT)) as float
	}

	private def createLetter(char letter, Vector3f LEFT_BOTTOM, Vector3f RIGHT_BOTTOM, Vector3f RIGHT_TOP,
		Vector3f LEFT_TOP) {
		val fontSize = TextureManager::fontSize
		val lettersPerSide = TextureManager::lettersPerSide

		val textureSize = (fontSize * lettersPerSide) as float

		val i = letter as int - TextureManager::letterStartCode
		val textureStartX = ((i % lettersPerSide) * fontSize) / textureSize
		val textureStartY = ((i / lettersPerSide) * fontSize) / textureSize + 0.003f
		val textureDimX = 1f / lettersPerSide
		val textureDimY = textureDimX - 0.006f

		new Quad(LEFT_BOTTOM, RIGHT_BOTTOM, RIGHT_TOP, LEFT_TOP, textureStartX, textureStartY, textureDimX, textureDimY)
	}

	override getVertices() {

		// not used
		letters.get(0).vertices
	}

	override draw() {
		letters.forEach [
			it.draw()
		]
	}

	override isHighlighted() {
		false
	}

	override highlight(Vector4f color) {
		// dont
	}

	override unhighlight() {
		// dont
	}

	override moveByVector(Vector3f vector) {
		// dont
	}

}
