package explorviz.visualization.engine.primitives

import elemental.html.WebGLTexture
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.engine.math.Vector3f
import java.util.List
import java.util.ArrayList
import explorviz.visualization.engine.buffer.BufferManager

class LabelContainer {
	var static WebGLTexture letterTextureWhite
	var static WebGLTexture letterTextureBlack

	val static List<RememberedLabel> rememberedLabels = new ArrayList<RememberedLabel>()
	var static int whiteLetterCount = 0
	var static int whiteLetterOffsetInBuffer = 0

	var static int blackLetterCount = 0
	var static int blackLetterOffsetInBuffer = 0

	def static init() {
		letterTextureWhite = TextureManager::createLetterTexture(true)
		letterTextureBlack = TextureManager::createLetterTexture(false)

		clear()
	}

	def static clear() {
		whiteLetterCount = 0
		whiteLetterOffsetInBuffer = 0

		blackLetterCount = 0
		blackLetterOffsetInBuffer = 0
	}

	/**
	 * ATTENTION: all labels must be created in batch! call doLabelCreation when finished
	 */
	def static createLabel(String text, Vector3f LEFT_BOTTOM, Vector3f RIGHT_BOTTOM, Vector3f RIGHT_TOP,
		Vector3f LEFT_TOP, boolean downwards, boolean white) {
		val rememberedLabel = new RememberedLabel()
		rememberedLabel.text = text
		rememberedLabel.LEFT_BOTTOM = LEFT_BOTTOM
		rememberedLabel.RIGHT_BOTTOM = RIGHT_BOTTOM
		rememberedLabel.RIGHT_TOP = RIGHT_TOP
		rememberedLabel.LEFT_TOP = LEFT_TOP
		rememberedLabel.downwards = downwards
		rememberedLabel.white = white

		rememberedLabels.add(rememberedLabel)
	}

	def static doLabelCreation() {
		for (rememberedLabel : rememberedLabels) {
			val label = new Label(rememberedLabel.text, rememberedLabel.LEFT_BOTTOM, rememberedLabel.RIGHT_BOTTOM,
				rememberedLabel.RIGHT_TOP, rememberedLabel.LEFT_TOP, rememberedLabel.downwards)
			if (rememberedLabel.white) {
				if (whiteLetterCount == 0) {
					whiteLetterOffsetInBuffer = label.letters.get(0).offsetStart
				}
				whiteLetterCount += label.letters.size
			} else {
				if (blackLetterCount == 0) {
					blackLetterOffsetInBuffer = label.letters.get(0).offsetStart
				}
				blackLetterCount += label.letters.size
			}
		}
		rememberedLabels.clear()
	}

	def static void draw() {
		if (whiteLetterCount > 0)
			BufferManager::drawLabelsAtOnce(whiteLetterOffsetInBuffer, letterTextureWhite, whiteLetterCount)
		if (blackLetterCount > 0)
			BufferManager::drawLabelsAtOnce(blackLetterOffsetInBuffer, letterTextureBlack, blackLetterCount)
	}

	private static class RememberedLabel {
		@Property String text
		@Property Vector3f LEFT_BOTTOM
		@Property Vector3f RIGHT_BOTTOM
		@Property Vector3f RIGHT_TOP
		@Property Vector3f LEFT_TOP
		@Property boolean downwards
		@Property boolean white
	}
}
