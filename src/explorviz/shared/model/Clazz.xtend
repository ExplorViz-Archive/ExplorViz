package explorviz.shared.model

import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.renderer.ColorDefinitions
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.HashSet
import java.util.Set

class Clazz extends Draw3DNodeEntity {
	@Accessors var int instanceCount = 0
	@Accessors val transient Set<Integer> objectIds = new HashSet<Integer>()

	@Accessors Component parent
	@Accessors var boolean visible = false

	@Accessors var double rootCauseRating;
	@Accessors var boolean isRankingPositive = true;
	@Accessors var double temporaryRating = -1;

	override void destroy() {
		super.destroy()
	}

	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()
	}

	override void highlight() {
		for (primitiveObject : this.primitiveObjects)
			primitiveObject.highlight(ColorDefinitions::highlightColor)

		highlighted = true
	}

	override unhighlight() {
		if (highlighted) {
			for (primitiveObject : this.primitiveObjects)
				primitiveObject.unhighlight()
				
			highlighted = false
		}
	}
}
