package explorviz.visualization.model

import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.primitives.DatabaseShape
import explorviz.visualization.engine.math.Vector4f
import elemental.html.WebGLTexture
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.model.helper.DrawNodeEntity
import explorviz.visualization.model.helper.IViewable
import java.util.ArrayList
import explorviz.visualization.renderer.ColorDefinitions

class ApplicationClientSide extends DrawNodeEntity implements IViewable {
	@Property int id
	@Property boolean database
	@Property String name
	@Property String image
	@Property long lastUsage

	@Property NodeClientSide parent

	@Property var components = new ArrayList<ComponentClientSide>
	@Property var communications = new ArrayList<CommunicationClazzClientSide>

	static val Vector4f foregroundColor = ColorDefinitions::applicationForegroundColor
	static val Vector4f backgroundColor = ColorDefinitions::applicationBackgroundColor
	static val Vector4f backgroundRightColor = ColorDefinitions::applicationBackgroundRightColor
	
	def PrimitiveObject createApplicationShape(Quad quad, float z) {
		if (database) {
			createApplicationDatabase(quad, z)
		} else {
			createApplicationBox(quad, z)
		}
	}

	def private DatabaseShape createApplicationDatabase(Quad quad, float z) {
		//new DatabaseShape(quad.cornerPoints.get(0),quad.cornerPoints.get(1),quad.cornerPoints.get(2),quad.cornerPoints.get(3), new Vector4f(0f,0f,1f,1f), z)
		null
	}

	def private PrimitiveObject createApplicationBox(Quad quad, float z) {

		//createLineAroundQuad(quad,z,false,new Vector4f(0f,0f,1f,1f))
		null
	}

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
					texture = TextureManager::createTextureFromTextAndImagePath(text, "logos/database.png", 512, 256, 46,
						foregroundColor, backgroundColor, backgroundRightColor)
				} else {
					texture = TextureManager::createTextureFromTextAndImagePath(text, "logos/java.png", 512, 256, 46,
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
		communications.forEach[it.clearAllPrimitiveObjects()]
	}

}
