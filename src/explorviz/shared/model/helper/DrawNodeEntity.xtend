package explorviz.shared.model.helper

import de.cau.cs.kieler.klay.layered.graph.LGraph
import de.cau.cs.kieler.klay.layered.graph.LNode
import de.cau.cs.kieler.klay.layered.graph.LPort
import elemental.html.WebGLTexture
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.picking.EventObserver
import explorviz.visualization.engine.primitives.Quad
import java.util.HashMap
import java.util.Map
import org.eclipse.xtend.lib.annotations.Accessors

abstract class DrawNodeEntity extends EventObserver {
	@Accessors String name
	
	@Accessors transient LGraph kielerGraphReference
	@Accessors transient LNode kielerNodeReference
	
	@Accessors transient Map<DrawNodeEntity, LPort> sourcePorts = new HashMap<DrawNodeEntity, LPort>()
	@Accessors transient Map<DrawNodeEntity, LPort> targetPorts = new HashMap<DrawNodeEntity, LPort>()
	
	@Accessors transient float width
	@Accessors transient float height
	
	@Accessors transient float positionX
	@Accessors transient float positionY
	@Accessors transient float positionZ
	
	override destroy() {
	    super.destroy()
	}
	
	def createQuad(float z, Vector3f centerPoint, Vector4f color) {
		createQuad(z,centerPoint,null,color)
	}
	
	def createQuad(float z, Vector3f centerPoint, WebGLTexture texture) {
		createQuad(z,centerPoint,texture,null)
	}
	
	def createQuad(float z, Vector3f centerPoint, WebGLTexture texture, Vector4f color) {
        val extensionX = width / 2f
        val extensionY = height / 2f
        
        positionZ = z
        
        val centerX = positionX + extensionX - centerPoint.x
        val centerY = positionY - extensionY - centerPoint.y

        new Quad(new Vector3f(centerX, centerY, z),
                 new Vector3f(extensionX, extensionY, 0.0f), 
                 texture, color)
    }
}