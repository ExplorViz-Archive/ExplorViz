package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import java.util.List
import java.util.ArrayList
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.shared.model.Component
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.shared.model.Clazz
import explorviz.visualization.renderer.ColorDefinitions
import org.eclipse.xtend.lib.annotations.Accessors

class BoxContainer {
	val static List<RememberedBox> rememberedBoxes = new ArrayList<RememberedBox>()

	var static int boxLowLevelCount = 0
	var static int boxLowLevelOffsetInBuffer = 0

	var static int boxHighLevelCount = 0
	var static int boxHighLevelOffsetInBuffer = 0

	def static init() {
		clear()
	}

	def static clear() {
		boxLowLevelCount = 0
		boxLowLevelOffsetInBuffer = 0

		boxHighLevelCount = 0
		boxHighLevelOffsetInBuffer = 0
	}

	/**
	 * ATTENTION: all boxes must be created in batch! call doBoxCreation when finished
	 */
	def static void createBox(Draw3DNodeEntity entity, Vector3f viewCenterPoint, boolean lowLevel) {
		val rememberedBox = new RememberedBox()
		rememberedBox.entity = entity
		rememberedBox.viewCenterPoint = viewCenterPoint
		rememberedBox.lowLevel = lowLevel

		rememberedBoxes.add(rememberedBox)
	}

	def static void doBoxCreation() {
		rememberedBoxes.sortInplaceBy[lowLevel == true]
		
		for (rememberedBox : rememberedBoxes) {
			val entity = rememberedBox.entity
			val color = if (entity instanceof Clazz)
					ColorDefinitions::clazzColor
				else
					(entity as Component).color

			val box = new Box(entity.centerPoint.sub(rememberedBox.viewCenterPoint), entity.getExtension(), color)
			
			// test czi
//			Logging::log("Center: " + entity.centerPoint.sub(rememberedBox.viewCenterPoint).toString)
//			Logging::log("Extension: " + entity.getExtension().toString)
			
			entity.primitiveObjects.add(box)
			
			if (entity.highlighted) {
				entity.highlight()
			}

			if (rememberedBox.lowLevel) {
				if (boxLowLevelCount == 0) {
					boxLowLevelOffsetInBuffer = box.quads.get(0).offsetStart
				}
				boxLowLevelCount++
			} else {
				if (boxHighLevelCount == 0) {
					boxHighLevelOffsetInBuffer = box.quads.get(0).offsetStart
				}
				boxHighLevelCount++
			}
		}
		rememberedBoxes.clear()
	}

	def static void drawLowLevelBoxes() {
		if (boxLowLevelCount > 0)
			BufferManager::drawBoxesAtOnce(boxLowLevelOffsetInBuffer, boxLowLevelCount)
	}

	def static void drawHighLevelBoxes() {
		if (boxHighLevelCount > 0)
			BufferManager::drawBoxesAtOnce(boxHighLevelOffsetInBuffer, boxHighLevelCount)
	}

	private static class RememberedBox {
		@Accessors Draw3DNodeEntity entity
		@Accessors Vector3f viewCenterPoint
		@Accessors boolean lowLevel
	}
}
