package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import java.util.List
import java.util.ArrayList
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.renderer.ColorDefinitions
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.shared.model.helper.CommunicationAccumulator

class LineContainer {
	val static List<RememberedLine> rememberedLines = new ArrayList<RememberedLine>()

	var static int lineQuadsCount = 0
	var static int lineTrianglesCount = 0

	var static int lineOffsetInBuffer = 0

	def static init() {
		clear()
	}

	def static clear() {
		lineQuadsCount = 0
		lineTrianglesCount = 0

		lineOffsetInBuffer = 0
	}

	/**
	 * ATTENTION: all lines must be created in batch! call doLineCreation when finished
	 */
	def static void createLine(CommunicationAccumulator entity, Vector3f viewCenterPoint) {
		val rememberedLine = new RememberedLine()
		rememberedLine.entity = entity
		rememberedLine.viewCenterPoint = viewCenterPoint

		rememberedLines.add(rememberedLine)
	}

	def static void doLineCreation() {

		//		rememberedLines.sortInplaceBy[application == true]
		for (rememberedLine : rememberedLines) {
			val entity = rememberedLine.entity

			if (!entity.tiles.empty) {
				val line = new Line()
				line.color = ColorDefinitions::pipeColor
				line.begin
				val firstTile = entity.tiles.get(0)
				line.addPoint(firstTile.startPoint.x - rememberedLine.viewCenterPoint.x,
					firstTile.startPoint.y - rememberedLine.viewCenterPoint.y, firstTile.positionZ,
					firstTile.lineThickness, firstTile.alreadyDrawn)
					
				var oldQuadsSize = 0				

				for (var i = 0; i < entity.tiles.size; i++) {
					val tile = entity.tiles.get(i)
					line.addPoint(tile.endPoint.x - rememberedLine.viewCenterPoint.x,
						tile.endPoint.y - rememberedLine.viewCenterPoint.y, tile.positionZ, tile.lineThickness,
						tile.alreadyDrawn)
					tile.alreadyDrawn = true
					if (oldQuadsSize < line.quads.size) {
						tile.primitiveObjects.add(line.quads.get(oldQuadsSize))
						oldQuadsSize = line.quads.size
					}
				}
				line.end

				if (!line.quads.empty) {
					if (lineQuadsCount == 0 && lineTrianglesCount == 0) {
						lineOffsetInBuffer = line.quads.get(0).offsetStart
					}
					lineQuadsCount = lineQuadsCount + line.quads.size
				}

				if (!line.triangles.empty) {
					if (lineQuadsCount == 0 && lineTrianglesCount == 0) {
						lineOffsetInBuffer = line.triangles.get(0).offsetStart
					}
					lineTrianglesCount = lineTrianglesCount + line.triangles.size
				}
			}
		}
		rememberedLines.clear()
	}

	def static void drawLines() {
		if ((lineQuadsCount + lineTrianglesCount) > 0)
			BufferManager::drawLineAtOnce(lineOffsetInBuffer, lineQuadsCount, lineTrianglesCount)
	}

	private static class RememberedLine {
		@Accessors CommunicationAccumulator entity
		@Accessors Vector3f viewCenterPoint
	}
}
