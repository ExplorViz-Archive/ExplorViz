package explorviz.visualization.engine.primitives

import elemental.html.WebGLTexture
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.engine.math.Vector3f
import java.util.List
import java.util.ArrayList
import explorviz.visualization.engine.buffer.BufferManager

class LabelContainer {
	var static WebGLTexture letterTextureWhite
	var static WebGLTexture letterTextureRed
	var static WebGLTexture letterTextureBlack

	val static List<RememberedLabel> rememberedLabels = new ArrayList<RememberedLabel>()

	var static int whiteLetterCount
	var static int whiteLetterOffsetInBuffer

	var static int whiteLetterHighLevelCount
	var static int whiteLetterHighLevelOffsetInBuffer

	var static int redLetterCount
	var static int redLetterOffsetInBuffer

	var static int blackLetterCount
	var static int blackLetterOffsetInBuffer

	def static init() {
		letterTextureWhite = TextureManager::createLetterTexture("White")
		letterTextureRed = TextureManager::createLetterTexture("Red")
		letterTextureBlack = TextureManager::createLetterTexture("Black")

		clear()
	}

	def static clear() {
		whiteLetterCount = 0
		whiteLetterOffsetInBuffer = 0

		whiteLetterHighLevelCount = 0
		whiteLetterHighLevelOffsetInBuffer = 0

		redLetterCount = 0
		redLetterOffsetInBuffer = 0

		blackLetterCount = 0
		blackLetterOffsetInBuffer = 0
	}

	/**
	 * ATTENTION: all labels must be created in batch! call doLabelCreation when finished
	 */
	def static createLabel(String text, Vector3f LEFT_BOTTOM, Vector3f RIGHT_BOTTOM, Vector3f RIGHT_TOP,
		Vector3f LEFT_TOP, boolean downwards, boolean white, boolean isClazz, boolean highlight) {
		val rememberedLabel = new RememberedLabel()
		rememberedLabel.text = text
		rememberedLabel.LEFT_BOTTOM = LEFT_BOTTOM
		rememberedLabel.RIGHT_BOTTOM = RIGHT_BOTTOM
		rememberedLabel.RIGHT_TOP = RIGHT_TOP
		rememberedLabel.LEFT_TOP = LEFT_TOP
		rememberedLabel.downwards = downwards
		rememberedLabel.isClazz = isClazz
		rememberedLabel.white = white
		rememberedLabel.highlight = highlight

		rememberedLabels.add(rememberedLabel)
	}

	def static doLabelCreation() {
		rememberedLabels.sortInplace[c1, c2|c1.highlight <=> c2.highlight]
		rememberedLabels.sortInplace[c1, c2|c1.white <=> c2.white]
		rememberedLabels.sortInplace[c1, c2|c1.downwards <=> c2.downwards] // TODO only works since no highlight

		for (rememberedLabel : rememberedLabels) {
			val label = new Label(rememberedLabel.text, rememberedLabel.LEFT_BOTTOM, rememberedLabel.RIGHT_BOTTOM,
				rememberedLabel.RIGHT_TOP, rememberedLabel.LEFT_TOP, rememberedLabel.downwards, rememberedLabel.isClazz)

			if (rememberedLabel.highlight) {
				if (redLetterCount == 0) {
					redLetterOffsetInBuffer = label.letters.get(0).offsetStart
				}
				redLetterCount += label.letters.size
			} else {
				if (rememberedLabel.white) {
					if (rememberedLabel.downwards) {
						if (whiteLetterCount == 0) {
							whiteLetterOffsetInBuffer = label.letters.get(0).offsetStart
						}
						whiteLetterCount += label.letters.size
					} else {
						if (whiteLetterHighLevelCount == 0) {
							whiteLetterHighLevelOffsetInBuffer = label.letters.get(0).offsetStart
						}
						whiteLetterHighLevelCount += label.letters.size
					}
				} else {
					if (blackLetterCount == 0) {
						blackLetterOffsetInBuffer = label.letters.get(0).offsetStart
					}
					blackLetterCount += label.letters.size
				}
			}
		}
		rememberedLabels.clear()
	}

	def static void draw() {
		if (whiteLetterCount > 0)
			BufferManager::drawLabelsAtOnce(whiteLetterOffsetInBuffer, letterTextureWhite, whiteLetterCount)
		if (blackLetterCount > 0)
			BufferManager::drawLabelsAtOnce(blackLetterOffsetInBuffer, letterTextureBlack, blackLetterCount)
		if (redLetterCount > 0)
			BufferManager::drawLabelsAtOnce(redLetterOffsetInBuffer, letterTextureRed, redLetterCount)
	}

	def static void drawHighLevel() {
		if (whiteLetterHighLevelCount > 0)
			BufferManager::drawLabelsAtOnce(whiteLetterHighLevelOffsetInBuffer, letterTextureWhite,
				whiteLetterHighLevelCount)
	}

	private static class RememberedLabel {
		@Property String text
		@Property Vector3f LEFT_BOTTOM
		@Property Vector3f RIGHT_BOTTOM
		@Property Vector3f RIGHT_TOP
		@Property Vector3f LEFT_TOP
		@Property boolean downwards
		@Property boolean isClazz
		@Property boolean white
		@Property boolean highlight
	}
}
