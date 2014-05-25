package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.primitives.PrimitiveObject
import java.util.ArrayList
import explorviz.visualization.engine.math.Vector3f
import elemental.html.WebGLTexture
import explorviz.visualization.engine.math.Vector4f

class Box extends PrimitiveObject {
    @Property val quads = new ArrayList<Quad>(3)
    
    @Property var Vector3f center
    @Property var Vector3f extensionInEachDirection
    
    new (Vector3f center, Vector3f extensionInEachDirection, WebGLTexture texture, Vector4f color) {
    	this.center = center
    	this.extensionInEachDirection = extensionInEachDirection
    	
        // from the viewpoint of the front!
        val pointFrontBottomLeft = new Vector3f(center.x - extensionInEachDirection.x, center.y - extensionInEachDirection.y, center.z + extensionInEachDirection.z)
        val pointFrontBottomRight = new Vector3f(center.x + extensionInEachDirection.x, center.y - extensionInEachDirection.y, center.z + extensionInEachDirection.z)
        val pointFrontTopRight = new Vector3f(center.x + extensionInEachDirection.x, center.y + extensionInEachDirection.y, center.z + extensionInEachDirection.z)
        val pointFrontTopLeft = new Vector3f(center.x - extensionInEachDirection.x, center.y + extensionInEachDirection.y, center.z + extensionInEachDirection.z)
        
        // from the viewpoint of the back!
//        val pointBackBottomLeft = new Vector3f(center.x + extensionInEachDirection.x, center.y - extensionInEachDirection.y, center.z - extensionInEachDirection.z)
        val pointBackBottomRight = new Vector3f(center.x - extensionInEachDirection.x, center.y - extensionInEachDirection.y, center.z - extensionInEachDirection.z)
        val pointBackTopRight = new Vector3f(center.x - extensionInEachDirection.x, center.y + extensionInEachDirection.y, center.z - extensionInEachDirection.z)
        val pointBackTopLeft = new Vector3f(center.x + extensionInEachDirection.x, center.y + extensionInEachDirection.y, center.z - extensionInEachDirection.z)
        
        val quadFront = new Quad(pointFrontBottomLeft, pointFrontBottomRight, pointFrontTopRight, pointFrontTopLeft, texture, color)
        quads.add(quadFront)
        
//        val quadBack = new Quad(pointBackBottomLeft, pointBackBottomRight, pointBackTopRight, pointBackTopLeft, texture, color)
//        quads.add(quadBack)
        
    	val quadUpper = new Quad(pointFrontTopLeft, pointFrontTopRight, pointBackTopLeft, pointBackTopRight, texture, color)
    	quads.add(quadUpper)
    	
//    	val quadBottom = new Quad(pointFrontBottomRight, pointFrontBottomLeft, pointBackBottomRight, pointBackBottomLeft, texture, color)
//    	quads.add(quadBottom)
        
        val quadLeft = new Quad(pointBackBottomRight, pointFrontBottomLeft, pointFrontTopLeft, pointBackTopRight, texture, color)
        quads.add(quadLeft)
        
//        val quadRight = new Quad(pointFrontBottomRight, pointBackBottomLeft, pointBackTopLeft, pointFrontTopRight, texture, color)
//        quads.add(quadRight)
    }
    
    override final void draw() {
    	for (quad : quads) {
    		quad.draw()
    	}
    }

	override getVertices() {
		quads.get(0).vertices
	}
	
	override highlight(Vector4f color) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
    
    override unhighlight() {
        throw new UnsupportedOperationException("TODO: auto-generated method stub")
    }
    
    override moveByVector(Vector3f vector) {
        quads.forEach([it.moveByVector(vector)])
    }
    
    override reAddToBuffer() {
        quads.forEach([it.reAddToBuffer()])
    }
	
}
