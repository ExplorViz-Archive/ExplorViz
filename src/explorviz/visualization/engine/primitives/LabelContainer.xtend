package explorviz.visualization.engine.primitives

import elemental.html.WebGLTexture
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.engine.math.Vector3f
import java.util.List
import java.util.ArrayList
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.engine.math.Vector4f

class LabelContainer {
	var static WebGLTexture letterTextureWhite
	var static WebGLTexture letterTextureBlack

	val static List<RememberedLabel> rememberedLabels = new ArrayList<RememberedLabel>()
	val static List<Label> whiteLabels = new ArrayList<Label>()
	var static int whiteLetterCount = 0
	var static int whiteLetterOffsetInBuffer = 0

	val static List<Label> blackLabels = new ArrayList<Label>()
	var static int blackLetterCount = 0
	var static int blackLetterOffsetInBuffer = 0

	def static init() {
		letterTextureWhite = TextureManager::createLetterTexture(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f))
		letterTextureBlack = TextureManager::createLetterTexture(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f))

		clear()
	}

	def static clear() {
		whiteLetterCount = 0
		whiteLetterOffsetInBuffer = 0
		whiteLabels.clear()

		blackLetterCount = 0
		blackLetterOffsetInBuffer = 0
		blackLabels.clear()
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
				whiteLabels.add(label)
			} else {
				if (blackLetterCount == 0) {
					blackLetterOffsetInBuffer = label.letters.get(0).offsetStart
				}
				blackLetterCount += label.letters.size
				blackLabels.add(label)
			}
		}
		rememberedLabels.clear()
	}

	def static draw() {
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
