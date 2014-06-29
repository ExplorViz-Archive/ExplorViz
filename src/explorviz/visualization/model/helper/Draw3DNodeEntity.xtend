package explorviz.visualization.model.helper

import explorviz.visualization.engine.picking.EventObserver
import explorviz.visualization.engine.math.Vector4f
import elemental.html.WebGLTexture
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Box

class Draw3DNodeEntity extends EventObserver {
	@Property float width
	@Property float height
	@Property float depth
	
	@Property float positionX
	@Property float positionY
	@Property float positionZ
	
	@Property String name
	@Property String fullQualifiedName
	
	override destroy() {
	    super.destroy()
	}
	
	def getCenterPoint() {
		new Vector3f(this.positionX + this.width/2f,this.positionY + this.height/2f,this.positionZ + this.depth/2f)
	}
	
	def getExtension() {
		new Vector3f(this.width/2f,this.height/2f,this.depth/2f)
	}
	
	def createBox(Vector3f centerPoint, Vector4f color) {
		createBox(centerPoint,null,color)
	}
	
	def private createBox(Vector3f centerPoint, WebGLTexture texture, Vector4f color) {
        val extensionX = width / 2f
        val extensionY = height / 2f
        val extensionZ = depth / 2f
        
        val centerX = positionX + extensionX - centerPoint.x
        val centerY = positionY + extensionY - centerPoint.y
        val centerZ = positionZ + extensionZ - centerPoint.z

        new Box(new Vector3f(centerX, centerY, centerZ),
                 new Vector3f(extensionX, extensionY, extensionZ), 
                 texture, color)
    }
}