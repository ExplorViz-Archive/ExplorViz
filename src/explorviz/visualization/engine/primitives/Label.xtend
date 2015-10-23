package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.main.ClassnameSplitter
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.textures.TextureManager
import java.util.ArrayList
import java.util.List

class Label extends PrimitiveObject {
	protected val List<Quad> letters = new ArrayList<Quad>()

	static val MINIMUM_LETTER_SIZE = 1.75f
	static val SPACE_BETWEEN_LETTERS_IN_PERCENT = 0.09f

	public new(String text, Vector3f LEFT_BOTTOM, Vector3f RIGHT_BOTTOM, Vector3f RIGHT_TOP, Vector3f LEFT_TOP,
		boolean downwards, boolean isClazz) {
		if (downwards) {
			createLabelDownwards(text, LEFT_BOTTOM, RIGHT_BOTTOM, RIGHT_TOP, LEFT_TOP)
		} else {
			if (isClazz) {
				val splittedText = ClassnameSplitter::splitClassname(text, 14, 2)
				if (splittedText.size < 2) {
					createLabelSideWays(text, LEFT_BOTTOM, RIGHT_BOTTOM, RIGHT_TOP, LEFT_TOP)
				} else {
					val Y = LEFT_BOTTOM.y

					var quadSizeHalf = Math.abs(RIGHT_TOP.x - RIGHT_BOTTOM.x) / 2f + 0.075f

					createLabelSideWays(splittedText.get(0),
						new Vector3f(LEFT_BOTTOM.x + quadSizeHalf, Y, LEFT_BOTTOM.z - quadSizeHalf),
						new Vector3f(RIGHT_BOTTOM.x + quadSizeHalf, Y, RIGHT_BOTTOM.z - quadSizeHalf),
						new Vector3f(RIGHT_TOP.x + quadSizeHalf, Y, RIGHT_TOP.z - quadSizeHalf),
						new Vector3f(LEFT_TOP.x + quadSizeHalf, Y, LEFT_TOP.z - quadSizeHalf))
					createLabelSideWays(splittedText.get(1),
						new Vector3f(LEFT_BOTTOM.x - quadSizeHalf, Y, LEFT_BOTTOM.z + quadSizeHalf),
						new Vector3f(RIGHT_BOTTOM.x - quadSizeHalf, Y, RIGHT_BOTTOM.z + quadSizeHalf),
						new Vector3f(RIGHT_TOP.x - quadSizeHalf, Y, RIGHT_TOP.z + quadSizeHalf),
						new Vector3f(LEFT_TOP.x - quadSizeHalf, Y, LEFT_TOP.z + quadSizeHalf))
				}
			} else {
				createLabelSideWays(text, LEFT_BOTTOM, RIGHT_BOTTOM, RIGHT_TOP, LEFT_TOP)
			}
		}
	}

	private def void createLabelDownwards(String text, Vector3f LEFT_BOTTOM, Vector3f RIGHT_BOTTOM, Vector3f RIGHT_TOP,
		Vector3f LEFT_TOP) {
		val maxAvailableLength = Math.abs(RIGHT_TOP.z - LEFT_BOTTOM.z)

		var quadSize = Math.abs(RIGHT_TOP.x - RIGHT_BOTTOM.x)
		var requiredLength = calculateRequiredLength(text, quadSize)

		if (requiredLength > maxAvailableLength) {
			quadSize = maxAvailableLength /
				(((text.length * 0.5f) + ((text.length - 1) * SPACE_BETWEEN_LETTERS_IN_PERCENT)))

			if (quadSize < MINIMUM_LETTER_SIZE) {
				quadSize = MINIMUM_LETTER_SIZE
			}

			requiredLength = calculateRequiredLength(text, quadSize)
		}

		val X = LEFT_BOTTOM.x + Math.abs(RIGHT_TOP.x - RIGHT_BOTTOM.x) / 2f - quadSize / 2f
		val Y = LEFT_BOTTOM.y
		val Z_START = (LEFT_BOTTOM.z + maxAvailableLength / 2f - (requiredLength / 2f) - (quadSize * 0.25f))

		for (var int i = 0; i < text.length; i++) {
			var offset = ((0.5f - SPACE_BETWEEN_LETTERS_IN_PERCENT) * quadSize)
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
	}

	public static def float calculateRequiredLength(String text, float quadSize) {
		if (text == null || text.empty) {
			return 0
		}
		
		((text.length * quadSize * 0.5f) + ((text.length - 1) * quadSize * SPACE_BETWEEN_LETTERS_IN_PERCENT))
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

	private def void createLabelSideWays(String text, Vector3f LEFT_BOTTOM, Vector3f RIGHT_BOTTOM, Vector3f RIGHT_TOP,
		Vector3f LEFT_TOP) {
		val yDirection = checkIfYorZDirection(LEFT_BOTTOM, RIGHT_TOP)

		if (yDirection) {
			var quadSize = Math.abs(LEFT_TOP.y - LEFT_BOTTOM.y)
			var requiredLength = calculateRequiredLength(text, quadSize)

			val X_START = LEFT_TOP.x + Math.abs(RIGHT_TOP.x - LEFT_TOP.x) / 2f - (requiredLength / 2f) -
				(quadSize * 0.25f)
			val Y_START = LEFT_BOTTOM.y + Math.abs(LEFT_TOP.y - LEFT_BOTTOM.y) / 2f - (quadSize * 0.5f)

			val Z = LEFT_BOTTOM.z

			for (var int i = 0; i < text.length; i++) {
				var offset = ((0.5f - SPACE_BETWEEN_LETTERS_IN_PERCENT) * quadSize)
				val position = (quadSize - offset) * i

				letters.add(
					createLetter(
						text.charAt(i),
						new Vector3f(X_START + position, Y_START, Z),
						new Vector3f(X_START + position + quadSize, Y_START, Z),
						new Vector3f(X_START + position + quadSize, Y_START + quadSize, Z),
						new Vector3f(X_START + position, Y_START + quadSize, Z)
					)
				)
			}
		} else {
			var quadSize = Math.abs(RIGHT_TOP.x - RIGHT_BOTTOM.x)
			var requiredLength = calculateRequiredLength(text, quadSize)

			//		if (requiredLength > maxAvailableLength) {
			//			quadSize = maxAvailableLength /
			//				(((text.length * 0.5f) + ((text.length - 1) * SPACE_BETWEEN_LETTERS_IN_PERCENT)))
			//
			//			if (quadSize < MINIMUM_LETTER_SIZE) {
			//				quadSize = MINIMUM_LETTER_SIZE
			//			}
			//
			//			requiredLength = calculateRequiredLength(text, quadSize)
			//		}
			val TOP_X_START = LEFT_TOP.x + Math.abs(RIGHT_TOP.x - LEFT_TOP.x) / 2f - (requiredLength / 2f) -
				(quadSize * 0.25f)
			val BOTTOM_X_START = LEFT_BOTTOM.x + Math.abs(RIGHT_BOTTOM.x - LEFT_BOTTOM.x) / 2f - (requiredLength / 2f) -
				(quadSize * 0.25f)

			val Y = LEFT_BOTTOM.y

			val TOP_Z_START = LEFT_TOP.z + Math.abs(RIGHT_TOP.z - LEFT_TOP.z) / 2f - (requiredLength / 2f) -
				(quadSize * 0.25f)
			val BOTTOM_Z_START = LEFT_BOTTOM.z + Math.abs(RIGHT_BOTTOM.z - LEFT_BOTTOM.z) / 2f - (requiredLength / 2f) -
				(quadSize * 0.25f)

			for (var int i = 0; i < text.length; i++) {
				var offset = ((0.5f - SPACE_BETWEEN_LETTERS_IN_PERCENT) * quadSize)
				val position = (quadSize - offset) * i

				letters.add(
					createLetter(
						text.charAt(i),
						new Vector3f(BOTTOM_X_START + position, Y + 0.2f, BOTTOM_Z_START + position),
						new Vector3f(BOTTOM_X_START + position + quadSize, Y + 0.2f, BOTTOM_Z_START + position + quadSize),
						new Vector3f(TOP_X_START + position + quadSize, Y + 0.2f, TOP_Z_START + position + quadSize),
						new Vector3f(TOP_X_START + position, Y + 0.2f, TOP_Z_START + position)
					)
				)
			}
		}
	}

	private def boolean checkIfYorZDirection(Vector3f LEFT_BOTTOM, Vector3f RIGHT_TOP) {
		Math.abs(LEFT_BOTTOM.y - RIGHT_TOP.y) > 0.01f
	}

	override getVertices() {

		// not used
		letters.get(0).vertices
	}

	override draw() {
		for (letter : letters)
			letter.draw()
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
