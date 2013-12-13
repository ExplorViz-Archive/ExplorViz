package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.math.Vector3f

abstract class PrimitiveObject {
	def float[] getVertices()
	
    def void draw()
    
    def void reAddToBuffer()
    
    def void highlight(Vector4f color)
    def void unhighlight()
    
    def void moveByVector(Vector3f vector)
}
