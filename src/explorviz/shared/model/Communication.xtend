package explorviz.shared.model

import explorviz.shared.model.helper.DrawEdgeEntity
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Line
import explorviz.visualization.engine.primitives.Triangle
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.renderer.ColorDefinitions
import java.util.List

class Communication extends DrawEdgeEntity {
	@Property int requests

	@Property Application source
	@Property Application target

	@Property Clazz sourceClazz
	@Property Clazz targetClazz

	def static void createCommunicationLines(float z, Landscape landscape, Vector3f centerPoint, List<Triangle> polygons) {
		val lineZvalue = z + 0.01f

		landscape.applicationCommunication.forEach [
			if (!it.points.empty) {
				val line = new Line()
				line.lineThickness = it.lineThickness
				line.color = ColorDefinitions::pipeColor
				line.begin
				it.points.forEach [
					line.addPoint(it.x - centerPoint.x, it.y - centerPoint.y, lineZvalue)
				]
				line.end

				it.primitiveObjects.add(line)
				line.quads.forEach [
					polygons.addAll(it.triangles)
				]
				polygons.addAll(line.triangles)
				val arrow = Experiment::drawTutorialCom(it.source.name, it.target.name,
					new Vector3f(it.source.positionX, it.source.positionY, z), it.source.width, it.source.height,
					centerPoint, polygons)
				it.primitiveObjects.addAll(arrow)
			}
		]
	}

	override void destroy() {
		super.destroy()
	}
}
