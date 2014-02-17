package explorviz.visualization.model

import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.model.helper.DrawNodeEntity
import explorviz.visualization.renderer.ColorDefinitions

class NodeClientSide extends DrawNodeEntity {
	@Property String ipAddress
	@Property String name
	@Property float cpuUtilization
	@Property float memoryConsumption
	
	@Property boolean visible = true 
	
	@Property NodeGroupClientSide parent
	
	@Property val List<ApplicationClientSide> applications = new ArrayList<ApplicationClientSide>
	
	static val foregroundColor = ColorDefinitions::nodeForegroundColor
	static val backgroundColor = ColorDefinitions::nodeBackgroundColor
	
    def Quad createNodeQuad(float z, Vector3f centerPoint) {
        createQuad(z, centerPoint, TextureManager::createTextureFromTextWithBgColor("", 512, 512, backgroundColor))
    }
	
    def createNodeLabel(Quad node, String ipAddress) {
    	val ORIG_BOTTOM_LEFT = node.cornerPoints.get(0)
    	val ORIG_BOTTOM_RIGHT = node.cornerPoints.get(1)
    	
    	val labelWidth = 2.0f
    	val labelHeight = 0.75f
    	
    	val labelOffsetBottom = 0.1f
    	
    	val absolutLabelLeftStart = ORIG_BOTTOM_LEFT.x + ((ORIG_BOTTOM_RIGHT.x - ORIG_BOTTOM_LEFT.x) / 2f) - (labelWidth / 2f)
    	
    	val BOTTOM_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_BOTTOM_LEFT.y + labelOffsetBottom, 0.05f)
    	val BOTTOM_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth, ORIG_BOTTOM_RIGHT.y + labelOffsetBottom, 0.05f)
    	val TOP_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth, ORIG_BOTTOM_RIGHT.y + labelOffsetBottom + labelHeight, 0.05f)
    	val TOP_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_BOTTOM_LEFT.y + labelOffsetBottom + labelHeight, 0.05f)
    	
    	new Quad(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, TextureManager::createTextureFromTextWithTextSizeWithFgColorWithBgColor(ipAddress,1024,512,105, foregroundColor, backgroundColor))
    }
	
	override void destroy() {
		applications.forEach [ it.destroy()]
	    super.destroy()
	}
	
}