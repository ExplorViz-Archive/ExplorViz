package explorviz.visualization.engine.primitives

import elemental.html.WebGLTexture
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.engine.math.Vector3f
import java.util.List
import java.util.ArrayList
import explorviz.visualization.engine.buffer.BufferManager
import org.eclipse.xtend.lib.annotations.Accessors

class LabelContainer {
	var static WebGLTexture letterTextureWhite
	var static WebGLTexture letterTextureWhiteApp
	var static WebGLTexture letterTextureHighlight
	var static WebGLTexture letterTextureBlack

	val static List<RememberedLabel> rememberedLabels = new ArrayList<RememberedLabel>()

	var static int whiteLetterCount
	var static int whiteLetterOffsetInBuffer

	var static int whiteAppLetterCount
	var static int whiteAppLetterOffsetInBuffer

	var static int highlighLetterCount
	var static int highlightLetterOffsetInBuffer

	var static int blackLetterCount
	var static int blackLetterOffsetInBuffer

	var static int downwardsLetterCount
	var static int downwardsLetterOffsetInBuffer

	def static init() {
		clear()

		TextureManager::deleteTextureIfExisting(letterTextureWhite)
		TextureManager::deleteTextureIfExisting(letterTextureWhiteApp)
		TextureManager::deleteTextureIfExisting(letterTextureHighlight)
		TextureManager::deleteTextureIfExisting(letterTextureBlack)

		letterTextureWhite = TextureManager::createLetterTexture("White12")
		letterTextureWhiteApp = TextureManager::createLetterTexture("WhiteApplication")
		letterTextureHighlight = TextureManager::createLetterTexture("HighlightApplication")
		letterTextureBlack = TextureManager::createLetterTexture("Black34")
	}

	def static clear() {
		whiteLetterCount = 0
		whiteLetterOffsetInBuffer = 0

		whiteAppLetterCount = 0
		whiteAppLetterOffsetInBuffer = 0

		highlighLetterCount = 0
		highlightLetterOffsetInBuffer = 0

		blackLetterCount = 0
		blackLetterOffsetInBuffer = 0

		downwardsLetterCount = 0
		downwardsLetterOffsetInBuffer = 0
	}

	/**
	 * ATTENTION: all labels must be created in batch! call doLabelCreation when finished
	 */
	def static createLabel(String text, Vector3f LEFT_BOTTOM, Vector3f RIGHT_BOTTOM, Vector3f RIGHT_TOP,
		Vector3f LEFT_TOP, boolean downwards, boolean white, boolean isClazz, boolean highlight,
		boolean applicationLevel) {
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
		rememberedLabel.applicationLevel = applicationLevel

		rememberedLabels.add(rememberedLabel)
	}

	def static doLabelCreation() {
		rememberedLabels.sortInplace[c1, c2|c1.highlight <=> c2.highlight]
		rememberedLabels.sortInplace[c1, c2|c1.white <=> c2.white]
		rememberedLabels.sortInplace[c1, c2|c1.applicationLevel <=> c2.applicationLevel]
		rememberedLabels.sortInplace[c1, c2|c1.downwards <=> c2.downwards]

		for (rememberedLabel : rememberedLabels) {
			val label = new Label(rememberedLabel.text, rememberedLabel.LEFT_BOTTOM, rememberedLabel.RIGHT_BOTTOM,
				rememberedLabel.RIGHT_TOP, rememberedLabel.LEFT_TOP, rememberedLabel.downwards, rememberedLabel.isClazz)
			if (rememberedLabel.highlight) {
				if (highlighLetterCount == 0) {
					highlightLetterOffsetInBuffer = label.letters.get(0).offsetStart
				}
				highlighLetterCount += label.letters.size
			} else {
				if (rememberedLabel.white) {
					if (rememberedLabel.applicationLevel) {
						if (rememberedLabel.downwards) {
							if (downwardsLetterCount == 0) {
								downwardsLetterOffsetInBuffer = label.letters.get(0).offsetStart
							}
							downwardsLetterCount += label.letters.size
						} else {
							if (whiteAppLetterCount == 0) {
								whiteAppLetterOffsetInBuffer = label.letters.get(0).offsetStart
							}
							whiteAppLetterCount += label.letters.size
						}
					} else {
						if (whiteLetterCount == 0) {
							whiteLetterOffsetInBuffer = label.letters.get(0).offsetStart
						}
						whiteLetterCount += label.letters.size
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
		if (whiteAppLetterCount > 0)
			BufferManager::drawLabelsAtOnce(whiteAppLetterOffsetInBuffer, letterTextureWhiteApp, whiteAppLetterCount)
		if (blackLetterCount > 0)
			BufferManager::drawLabelsAtOnce(blackLetterOffsetInBuffer, letterTextureBlack, blackLetterCount)
		if (highlighLetterCount > 0)
			BufferManager::drawLabelsAtOnce(highlightLetterOffsetInBuffer, letterTextureHighlight, highlighLetterCount)
	}

	def static void drawDownwardLabels() {
		if (downwardsLetterCount > 0)
			BufferManager::drawLabelsAtOnce(downwardsLetterOffsetInBuffer, letterTextureWhiteApp, downwardsLetterCount)
	}

	private static class RememberedLabel {
		@Accessors String text
		@Accessors Vector3f LEFT_BOTTOM
		@Accessors Vector3f RIGHT_BOTTOM
		@Accessors Vector3f RIGHT_TOP
		@Accessors Vector3f LEFT_TOP
		@Accessors boolean downwards
		@Accessors boolean isClazz
		@Accessors boolean white
		@Accessors boolean highlight
		@Accessors boolean applicationLevel
	}
}
