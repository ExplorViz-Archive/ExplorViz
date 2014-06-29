package explorviz.visualization.model

import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.math.Vector4f
import elemental.html.WebGLTexture
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.model.helper.DrawNodeEntity
import java.util.ArrayList
import explorviz.visualization.renderer.ColorDefinitions
import explorviz.visualization.model.helper.CommunicationAppAccumulator

class ApplicationClientSide extends DrawNodeEntity {
	@Property int id
	@Property boolean database
	@Property String name
	@Property String image
	@Property long lastUsage

	@Property NodeClientSide parent

	@Property val components = new ArrayList<ComponentClientSide>
	@Property val communications = new ArrayList<CommunicationClazzClientSide>
	
	@Property val communicationsAccumulated = new ArrayList<CommunicationAppAccumulator>
	
	@Property val incomingCommunications = new ArrayList<CommunicationClientSide>
	@Property val outgoingCommunications = new ArrayList<CommunicationClientSide>

	static val Vector4f foregroundColor = ColorDefinitions::applicationForegroundColor
	static val Vector4f backgroundColor = ColorDefinitions::applicationBackgroundColor
	static val Vector4f backgroundRightColor = ColorDefinitions::applicationBackgroundRightColor
	
	def Quad createApplicationQuad(String text, float z, Vector3f centerPoint, PrimitiveObject oldQuad) {
		var WebGLTexture texture = null
		if (oldQuad == null) {
			if (image != null && !image.empty) {
				if (database) {
					texture = TextureManager::createTextureFromImagePath(image, 8, 150, 496, 200, 512, 512)
				} else {
					texture = TextureManager::createTextureFromImagePath(image, 50, 50, 412, 156, 512, 512) // 256
				}
			} else {
				if (database) {
					texture = TextureManager::createTextureFromTextAndImagePath(text, "logos/database.png", 512, 256, 60,
						foregroundColor, backgroundColor, backgroundRightColor)
				} else {
					texture = TextureManager::createTextureFromTextAndImagePath(text, "logos/java.png", 512, 256, 60,
						foregroundColor, backgroundColor, backgroundRightColor)
				}
			}
		} else {
			texture = (oldQuad as Quad).texture
		}

		createQuad(z, centerPoint, texture)
	}

	override void destroy() {
		components.forEach[it.destroy()]
		super.destroy()
	}

	def void clearAllPrimitiveObjects() {
		components.forEach[it.clearAllPrimitiveObjects()]
		communicationsAccumulated.forEach[it.clearAllPrimitiveObjects()]
	}

}
