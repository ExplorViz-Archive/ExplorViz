package explorviz.shared.model

import elemental.html.WebGLTexture
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import java.util.ArrayList
import explorviz.visualization.renderer.ColorDefinitions

class Application extends DrawNodeEntity {
    @Property int id
    
	@Property boolean database
	
	@Property String name
	@Property String image
	
	@Property long lastUsage
	
	@Property Node parent
	
	@Property var components = new ArrayList<Component>
	
	@Property var communications = new ArrayList<CommunicationClazz>
	
	@Property val transient communicationsAccumulated = new ArrayList<CommunicationAppAccumulator>
	
	@Property var incomingCommunications = new ArrayList<Communication>
	@Property var outgoingCommunications = new ArrayList<Communication>
	
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
						ColorDefinitions::applicationForegroundColor, ColorDefinitions::applicationBackgroundColor, ColorDefinitions::applicationBackgroundRightColor)
				} else {
					texture = TextureManager::createTextureFromTextAndImagePath(text, "logos/java.png", 512, 256, 60,
						ColorDefinitions::applicationForegroundColor, ColorDefinitions::applicationBackgroundColor, ColorDefinitions::applicationBackgroundRightColor)
				}
			}
		} else {
			texture = (oldQuad as Quad).texture
		}

		createQuad(z, centerPoint, texture)
	}
	
	override void destroy() {
		components.forEach[it.destroy()]
		communications.forEach[it.destroy()]
		super.destroy()
	}
	
	def void clearAllPrimitiveObjects() {
		components.forEach[it.clearAllPrimitiveObjects()]
		communicationsAccumulated.forEach[it.clearAllPrimitiveObjects()]
	}
	
	def void unhighlight() {
		components.forEach[it.unhighlight]
	}
	
}