package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import java.util.List
import java.util.ArrayList
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.shared.model.helper.DrawNodeEntity
import elemental.html.WebGLTexture
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.renderer.ColorDefinitions
import org.eclipse.xtend.lib.annotations.Accessors

class QuadContainer {
	val static List<RememberedQuad> rememberedQuads = new ArrayList<RememberedQuad>()

	var static int quadCount = 0
	var static int quadOffsetInBuffer = 0

	var static int quadWithAppTextureCount = 0
	var static int quadWithAppTextureOffsetInBuffer = 0

	var static WebGLTexture appTexture

	def static init() {
		clear()
		
		TextureManager::deleteTextureIfExisting(appTexture)

		appTexture = TextureManager::createGradientTexture(ColorDefinitions::applicationBackgroundColor,
			ColorDefinitions::applicationBackgroundRightColor, 512, 256)
	}

	def static clear() {
		quadCount = 0
		quadOffsetInBuffer = 0

		quadWithAppTextureCount = 0
		quadWithAppTextureOffsetInBuffer = 0
	}

	/**
	 * ATTENTION: all quads must be created in batch! call doQuadCreation when finished
	 */
	def static void createQuad(DrawNodeEntity entity, Vector3f viewCenterPoint, WebGLTexture texture, Vector4f color,
		boolean application) {
		val rememberedQuad = new RememberedQuad()
		rememberedQuad.entity = entity
		rememberedQuad.viewCenterPoint = viewCenterPoint
		rememberedQuad.texture = texture
		rememberedQuad.color = color
		rememberedQuad.application = application

		rememberedQuads.add(rememberedQuad)
	}

	def static void doQuadCreation() {
		rememberedQuads.sortInplaceBy[application == true]

		for (rememberedQuad : rememberedQuads) {
			val entity = rememberedQuad.entity

			val quad = createQuadInternal(entity, rememberedQuad.viewCenterPoint, rememberedQuad.texture,
				rememberedQuad.color)
			entity.primitiveObjects.add(quad)
			if (rememberedQuad.application == false) {
				if (quadCount == 0) {
					quadOffsetInBuffer = quad.offsetStart
				}
				quadCount++
			} else {
				if (quadWithAppTextureCount == 0) {
					quadWithAppTextureOffsetInBuffer = quad.offsetStart
				}
				quadWithAppTextureCount++
			}
		}
		rememberedQuads.clear()
	}

	def private static Quad createQuadInternal(DrawNodeEntity entity, Vector3f centerPoint, WebGLTexture texture,
		Vector4f color) {
		val extensionX = entity.width / 2f
		val extensionY = entity.height / 2f

		val centerX = entity.positionX + extensionX - centerPoint.x
		val centerY = entity.positionY - extensionY - centerPoint.y

		new Quad(new Vector3f(centerX, centerY, entity.positionZ),
			new Vector3f(extensionX, extensionY, 0.0f), texture, color)
	}

	def static void drawQuads() {
		if (quadCount > 0)
			BufferManager::drawQuadsAtOnce(quadOffsetInBuffer, quadCount)
	}

	def static void drawQuadsWithAppTexture() {
		if (quadWithAppTextureCount > 0)
			BufferManager::drawQuadsWithAppTextureAtOnce(quadWithAppTextureOffsetInBuffer, quadWithAppTextureCount,
				appTexture)
	}

	private static class RememberedQuad {
		@Accessors DrawNodeEntity entity
		@Accessors Vector3f viewCenterPoint
		@Accessors WebGLTexture texture
		@Accessors Vector4f color
		@Accessors boolean application
	}
}
