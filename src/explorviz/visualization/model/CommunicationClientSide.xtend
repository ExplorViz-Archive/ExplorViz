package explorviz.visualization.model

import explorviz.visualization.engine.primitives.PrimitiveObject
import java.util.List
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Line
import explorviz.visualization.model.helper.DrawEdgeEntity
import explorviz.visualization.renderer.ColorDefinitions
import explorviz.visualization.experiment.Experiment

class CommunicationClientSide extends DrawEdgeEntity {
	@Property int requestsPerSecond
	
	@Property ApplicationClientSide source
	@Property ApplicationClientSide target
	
	val static pipeColor = ColorDefinitions::pipeColor
	
    def static createCommunicationLines(float z, LandscapeClientSide landscape, Vector3f centerPoint, List<PrimitiveObject> polygons) {
        val lineZvalue = z + 0.01f
        
        landscape.applicationCommunication.forEach[
            if (!it.points.empty) {
                val line = new Line()
                line.lineThickness = it.lineThickness
                line.color = pipeColor
                line.begin
                    it.points.forEach [
                      line.addPoint(it.x  - centerPoint.x, it.y - centerPoint.y, lineZvalue)
                    ]
                line.end
                
                it.primitiveObjects.add(line)
                polygons.add(line)
                val arrow = Experiment::drawTutorial(it.source.name, it.target.name, 
                	it.source.positionX +(it.source.positionX - it.target.positionX)/2, 
                	it.source.positionY +(it.source.positionY - it.target.positionY)/2, 
                	z+0.05f, polygons)
                it.primitiveObjects.addAll(arrow)
            }
        ]
    }
	
	override void destroy() {
		super.destroy()
	}
	
}