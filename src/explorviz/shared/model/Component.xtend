package explorviz.shared.model

import java.util.ArrayList
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.List

class Component extends Draw3DNodeEntity {
	@Accessors var boolean synthetic = false
	@Accessors var boolean foundation = false

	@Accessors var List<Component> children = new ArrayList<Component>
	@Accessors var List<Clazz> clazzes = new ArrayList<Clazz>

	@Accessors Component parentComponent

	@Accessors Application belongingApplication

	@Accessors var Vector4f color
	
	@Accessors var boolean isRankingPositive = true;
	@Accessors var double rootCauseRating;
	@Accessors var double temporaryRating = -1;

	var boolean opened = false

	def boolean isOpened() {
		opened
	}

	def void setOpened(boolean openedParam) {
		if (!openedParam) setAllChildrenUnopened()

		this.opened = openedParam
	}

	private def setAllChildrenUnopened() {
		for (child : children)
			child.setOpened(false)
	}

	def void openAllComponents() {
		opened = true
		for (child : children)
			child.openAllComponents()
	}

	override void destroy() {
		for (child : children)
			child.destroy()

		for (clazz : clazzes)
			clazz.destroy()
		super.destroy()
	}

	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()

		for (child : children)
			child.clearAllPrimitiveObjects()

		for (clazz : clazzes)
			clazz.clearAllPrimitiveObjects()
	}

	override void highlight() {
		for (primitiveObject : this.primitiveObjects)
			primitiveObject.highlight(ColorDefinitions::highlightColor)
			
		highlighted = true
	}

	override void unhighlight() {
		if (highlighted) {
			for (primitiveObject : this.primitiveObjects)
				primitiveObject.unhighlight()
				
			highlighted = false
		} else {
		for (child : children)
			child.unhighlight()

		for (clazz : clazzes)
			clazz.unhighlight()
		}
	}
}
