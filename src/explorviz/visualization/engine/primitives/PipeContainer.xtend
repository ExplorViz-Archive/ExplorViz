package explorviz.visualization.engine.primitives

import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList
import java.util.List
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.Logging

class PipeContainer {
	val static List<PipeContainer.RememberedPipe> rememberedPipes = new ArrayList<PipeContainer.RememberedPipe>()

	var static int pipeTransparentCount = 0
	var static int pipeTransparentOffsetInBuffer = 0

	var static int pipeCount = 0
	var static int pipeOffsetInBuffer = 0

	var static int extraTrianglesCount = 0

	def static init() {
		clear()
	}

	def static clear() {
		pipeTransparentCount = 0
		pipeTransparentOffsetInBuffer = 0

		pipeCount = 0
		pipeOffsetInBuffer = 0

		extraTrianglesCount = 0
	}

	/**
	 * ATTENTION: all boxes must be created in batch! call doBoxCreation when finished
	 */
	def static void createPipe(CommunicationAppAccumulator entity, Vector3f viewCenterPoint, float lineThickness,
		Vector3f start, Vector3f end) {
		val rememberedPipe = new PipeContainer.RememberedPipe()
		rememberedPipe.entity = entity
		rememberedPipe.viewCenterPoint = viewCenterPoint
		rememberedPipe.lineThickness = lineThickness
		rememberedPipe.start = start
		rememberedPipe.end = end

		rememberedPipes.add(rememberedPipe)
	}

	def static void doPipeCreation() {
		rememberedPipes.sortInplaceBy[entity.state == EdgeState.TRANSPARENT]

		for (rememberedPipe : rememberedPipes) {
			val entity = rememberedPipe.entity
			val transparent = entity.state == EdgeState.TRANSPARENT

			val color = if (transparent)
					ColorDefinitions::pipeColorTrans
				else
					ColorDefinitions::pipeColor

			val pipe = new Pipe(transparent, true, color)

			pipe.setLineThickness(rememberedPipe.lineThickness)

			val start = rememberedPipe.start.sub(rememberedPipe.viewCenterPoint)
			pipe.addPoint(start)

			val end = rememberedPipe.end.sub(rememberedPipe.viewCenterPoint)
			pipe.addPoint(end)

			entity.primitiveObjects.add(pipe)

			if (transparent) {
				if (pipeTransparentCount == 0) {
					pipeTransparentOffsetInBuffer = pipe.quads.get(0).offsetStart
				}
				pipeTransparentCount++
			} else {
				if (pipeCount == 0) {
					pipeOffsetInBuffer = pipe.quads.get(0).offsetStart
				}
				pipeCount++
			}

			if (entity.state == EdgeState.SHOW_DIRECTION_IN) {
				val triangle = createDirectionTriangle(start, end)
				entity.primitiveObjects.add(triangle)

				extraTrianglesCount++

			// TODO create labels
			}
		}
		rememberedPipes.clear()
	}

	def static private Triangle createDirectionTriangle(Vector3f start, Vector3f end) {
		val distance = end.sub(start)

		val xDistance = distance.x * 0.25f
		val zDistance = distance.z * 0.25f
		val p1 = new Vector3f(start.x + xDistance - 1f, start.y, start.z + zDistance)
		val p2 = new Vector3f(start.x + xDistance + 1f, start.y, start.z + zDistance + 2f)
		val p3 = new Vector3f(start.x + xDistance, start.y, start.z + zDistance - 5f)
		val triangle = new Triangle(null, ColorDefinitions::highlightColor, false, true, p1, p2, p3, 0f, 1f, 1f, 1f, 1f,
			0f)

		Logging::log(p1 + " p1")
		Logging::log(p2 + " p2")
		Logging::log(p3 + " p3")
		
		triangle
	}

	def static void drawTransparentPipes() {
		if (pipeTransparentCount > 0)
			BufferManager::drawPipesAtOnce(pipeTransparentOffsetInBuffer, pipeTransparentCount, true, 0)
	}

	def static void drawPipes() {
		if (pipeCount > 0)
			BufferManager::drawPipesAtOnce(pipeOffsetInBuffer, pipeCount, false, extraTrianglesCount)
	}

	private static class RememberedPipe {
		@Property CommunicationAppAccumulator entity
		@Property Vector3f viewCenterPoint
		@Property float lineThickness
		@Property Vector3f start
		@Property Vector3f end
	}
}
