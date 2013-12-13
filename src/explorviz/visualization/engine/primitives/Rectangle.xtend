package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f

class Rectangle extends PrimitiveObject {
    @Property var Line line
    
    new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, Vector4f color, boolean stippeled, float z) {
        line = new Line()
        line.stippeled = stippeled
        val lineZvalue = z + 0.01f
        val lineThickness = 0.05f
        
        line.begin
            line.color = color
            line.lineThickness = lineThickness
            line.addPoint(BOTTOM_LEFT.x,BOTTOM_LEFT.y,lineZvalue)
            line.addPoint(BOTTOM_RIGHT.x,BOTTOM_RIGHT.y,lineZvalue)
            line.addPoint(TOP_RIGHT.x,TOP_RIGHT.y,lineZvalue)
            line.addPoint(TOP_LEFT.x,TOP_LEFT.y,lineZvalue)
            line.addPoint(BOTTOM_LEFT.x,BOTTOM_LEFT.y,lineZvalue)
        line.end
    }

    override draw() {
        line.draw
    }

    override getVertices() {
        line.vertices
    }

    override highlight(Vector4f color) {
        line.highlight(color)
    }
    
    override unhighlight() {
        line.unhighlight()
    }
    
    override moveByVector(Vector3f vector) {
        line.moveByVector(vector)
    }
    
    override reAddToBuffer() {
        line.reAddToBuffer()
    }
    
}
