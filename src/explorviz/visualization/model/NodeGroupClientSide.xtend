package explorviz.visualization.model

import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.model.helper.DrawNodeEntity

class NodeGroupClientSide extends DrawNodeEntity {
	@Property val List<NodeClientSide> nodes = new ArrayList<NodeClientSide>
	@Property LandscapeClientSide parent
	@Property Vector4f openedColor
	
	var boolean opened
	
	def boolean isOpened() {
	    opened
	}
	
	def void setOpened(boolean openedParam) {
	    if (openedParam) {
	       setAllChildrenVisibility(true)
	    } else {
	       setAllChildrenVisibility(false)
	       if (nodes.size() > 0) {
	          val firstNode = nodes.get(0)
    	      firstNode.visible = true
	       }
	    }
	    
	    this.opened = openedParam
	}

    private def setAllChildrenVisibility(boolean visiblity) {
        nodes.forEach [
    	   it.visible = visiblity
    	]
    }
    
    def Quad createNodeGroupQuad(float z, Vector3f centerPoint) {
        createQuad(z, centerPoint, openedColor)
    }
    
    def Quad createNodeGroupOpenSymbol(Quad nodeGroupQuad) {
        val extensionX = 0.1f
        val extensionY = 0.1f
        
        val TOP_RIGHT = nodeGroupQuad.cornerPoints.get(2)
        
        var float centerX = TOP_RIGHT.x - extensionX * 1.5f
        var float centerY = TOP_RIGHT.y - extensionY * 1.5f
        
        var symbol = "\u2013"
        if (!opened) symbol = "+"
        
        val texture = TextureManager::createTextureFromText(symbol, 128, 128, 228, 108, 10, 'bold 256px Arial', new Vector3f(openedColor.x,openedColor.y,openedColor.z))
        
        new Quad(new Vector3f(centerX, centerY, TOP_RIGHT.z + 0.01f),
                 new Vector3f(extensionX, extensionY, 0.0f), 
                 texture, null, true
        )
    }
	
	override void destroy() {
		nodes.forEach [it.destroy()]
	    super.destroy()
	}
	
}