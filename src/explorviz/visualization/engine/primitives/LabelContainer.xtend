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
	val static List<Label> labels = new ArrayList<Label>()
	var static int letterCount = 0
	var static int letterOffsetInBuffer = 0

	def static init() {
		letterTextureWhite = TextureManager::createLetterTexture(new Vector4f(1.0f,1.0f,1.0f,1.0f))
		letterTextureBlack = TextureManager::createLetterTexture(new Vector4f(0.0f,0.0f,0.0f,1.0f))

		clear()
	}

	def static clear() {
		letterCount = 0
		letterOffsetInBuffer = 0
		labels.clear()
	}

	/**
	 * ATTENTION: all labels must be created in batch! call doLabelCreation when finished
	 */
	def static createLabel(String text, Vector3f LEFT_BOTTOM, Vector3f RIGHT_BOTTOM, Vector3f RIGHT_TOP, Vector3f LEFT_TOP, boolean downwards) {
		val rememberedLabel = new RememberedLabel()
		rememberedLabel.text = text
		rememberedLabel.LEFT_BOTTOM = LEFT_BOTTOM
		rememberedLabel.RIGHT_BOTTOM = RIGHT_BOTTOM
		rememberedLabel.RIGHT_TOP = RIGHT_TOP
		rememberedLabel.LEFT_TOP = LEFT_TOP
		rememberedLabel.downwards = downwards

		rememberedLabels.add(rememberedLabel)
	}

	def static doLabelCreation() {
		for (rememberedLabel : rememberedLabels) {
			val label = new Label(rememberedLabel.text, rememberedLabel.LEFT_BOTTOM, rememberedLabel.RIGHT_BOTTOM, rememberedLabel.RIGHT_TOP, rememberedLabel.LEFT_TOP, rememberedLabel.downwards)
			if (letterCount == 0) {
				letterOffsetInBuffer = label.letters.get(0).offsetStart
			}
			letterCount += label.letters.size
			labels.add(label)
		}
		rememberedLabels.clear()
	}

	def static draw() {
		if (letterCount > 0)
			BufferManager::drawLabelsAtOnce(letterOffsetInBuffer, letterTextureBlack, letterCount)
	}

	private static class RememberedLabel {
		@Property String text
		@Property Vector3f LEFT_BOTTOM
		@Property Vector3f RIGHT_BOTTOM
		@Property Vector3f RIGHT_TOP
		@Property Vector3f LEFT_TOP
		@Property boolean downwards
	}
}
