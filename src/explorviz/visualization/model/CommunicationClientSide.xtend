package explorviz.visualization.model

import explorviz.visualization.engine.primitives.PrimitiveObject
import java.util.List
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Line
import explorviz.visualization.model.helper.DrawEdgeEntity
import explorviz.visualization.renderer.ColorDefinitions

class CommunicationClientSide extends DrawEdgeEntity {
	@Property int requestsPerSecond
	
	@Property ApplicationClientSide source
	@Property ApplicationClientSide target
	
	val static pipeColor = ColorDefinitions::pipeColor
	
    def static createCommunicationLines(float z, LandscapeClientSide landscape, Vector3f centerPoint, List<PrimitiveObject> polygons) {
        val lineZvalue = z + 0.01f
        val lineThickness = 0.05f
        
        landscape.applicationCommunication.forEach[
            if (it.source.parent.visible && it.target.parent.visible) {
                val line = new Line()
                line.lineThickness = lineThickness * (it.requestsPerSecond / 50f) // TODO percentile
                line.color = pipeColor
                line.begin
                    it.points.forEach [
                      line.addPoint(it.x  - centerPoint.x, it.y - centerPoint.y, lineZvalue)
                    ]
                line.end
                
                it.primitiveObjects.add(line)
                polygons.add(line)
            }
        ]
    }
	
	override void destroy() {
		super.destroy()
	}
	
}