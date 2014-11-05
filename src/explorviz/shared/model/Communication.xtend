package explorviz.shared.model

import explorviz.shared.model.helper.DrawEdgeEntity
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Line
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.renderer.ColorDefinitions
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.visualization.main.ExplorViz
import explorviz.plugin.main.Perspective
import explorviz.plugin.attributes.IPluginKeys
import explorviz.plugin.capacitymanagement.CapManExecutionStates

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

			if (ExplorViz::currentPerspective == Perspective::EXECUTION) {
				if (commu.source.parent.parent.opened || commu.source.parent.parent.nodes.size == 1 ||
					commu.target.parent.parent.opened || commu.target.parent.parent.nodes.size == 1) {
					var stateSource = CapManExecutionStates::NONE
					var stateTarget = CapManExecutionStates::NONE
					if (commu.source.isGenericDataPresent(IPluginKeys::CAPMAN_EXECUTION_STATE)) {
						stateSource = commu.source.getGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE) as CapManExecutionStates
					}
					
					if (commu.target.isGenericDataPresent(IPluginKeys::CAPMAN_EXECUTION_STATE)) {
						stateTarget = commu.target.getGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE) as CapManExecutionStates
					}
					
					if (stateSource != CapManExecutionStates::NONE || stateTarget != CapManExecutionStates::NONE) {
						for (triangle : line.triangles) {
							triangle.blinking = true
						}
						for (quad : line.quads) {
							quad.blinking = true
						}
					}
				}
			}

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
