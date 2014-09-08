package explorviz.shared.model

import explorviz.shared.model.helper.DrawEdgeEntity
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Line
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.renderer.ColorDefinitions
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

class Communication extends DrawEdgeEntity {
	@Accessors int requests

	@Accessors Application source
	@Accessors Application target

	@Accessors Clazz sourceClazz
	@Accessors Clazz targetClazz

	def static void createCommunicationLine(float z, Communication commu, Vector3f centerPoint,
		List<PrimitiveObject> polygons) {
		val lineZvalue = z + 0.02f

		if (!commu.points.empty) {
			val line = new Line()
			line.lineThickness = commu.lineThickness
			line.color = ColorDefinitions::pipeColor
			line.begin
			commu.points.forEach [
				line.addPoint(it.x - centerPoint.x, it.y - centerPoint.y, lineZvalue)
			]
			line.end

			commu.primitiveObjects.add(line)
			polygons.addAll(line.triangles)
			polygons.addAll(line.quads)
			val arrow = Experiment::drawTutorialCom(commu.source.name, commu.target.name,
				new Vector3f(commu.source.positionX, commu.source.positionY, z), commu.source.width, commu.source.height,
				centerPoint)
			commu.primitiveObjects.addAll(arrow)
		}
	}

	override void destroy() {
		super.destroy()
	}
}
