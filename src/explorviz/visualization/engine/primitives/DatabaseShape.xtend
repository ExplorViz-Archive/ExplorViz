package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f

class DatabaseShape extends PrimitiveObject {
    @Property var Line line
    
    new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, Vector4f color, float z) {
        line = new Line()
        line.stippeled = false
        val lineZvalue = z + 0.01f
        val lineThickness = 0.05f
        
        val offsetCurveHalf = 0.12f
        
        line.begin
            line.color = color
            line.lineThickness = lineThickness
            
            val bottom_left = new Vector3f(BOTTOM_LEFT.x, BOTTOM_LEFT.y + offsetCurveHalf,lineZvalue)
            val bottom_right = new Vector3f(BOTTOM_RIGHT.x,BOTTOM_RIGHT.y + offsetCurveHalf,lineZvalue)
            val top_right = new Vector3f(TOP_RIGHT.x,TOP_RIGHT.y - offsetCurveHalf,lineZvalue)
            val top_left = new Vector3f(TOP_LEFT.x,TOP_LEFT.y - offsetCurveHalf,lineZvalue)
            
            line.addPoint(top_right)
            line.addPoint(bottom_right)
            
            makeRoundCurve(line, bottom_right, bottom_left, lineZvalue)
            
            line.addPoint(bottom_left)
            line.addPoint(top_left)
            
            makeRoundCurve(line, top_left, top_right, lineZvalue)
            
            line.addPoint(top_right)
            
            makeRoundCurve(line, top_right, top_left, lineZvalue)
            
            line.addPoint(top_left)
        line.end
    }
    
    def private makeRoundCurve(Line line, Vector3f left_point, Vector3f right_point, float lineZvalue) {
        val xDiff = (right_point.x - left_point.x)
        val xDiffHalf = xDiff / 2f
        
        val steps = 0.1f
        
        var float i = steps
        val pointAtNull = ovalFormula((-1) * xDiffHalf, xDiff)
        while (i < 2f) {
            line.addPoint(left_point.x + xDiffHalf * i, left_point.y + (ovalFormula((i -1) * xDiffHalf, xDiff) - pointAtNull), lineZvalue)
            i = i + steps
        }
    }
    
    def private ovalFormula(float x, float maxDiff) {
        (Math::sqrt(1 - x*x/maxDiff) - 1.0f) as float
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
