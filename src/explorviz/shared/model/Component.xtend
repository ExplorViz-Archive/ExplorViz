package explorviz.shared.model

import java.util.ArrayList
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.List

class Component extends Draw3DNodeEntity {
	@Accessors var String name
	@Accessors var String fullQualifiedName
	@Accessors var boolean synthetic = false
	@Accessors var boolean foundation = false

	@Accessors var List<Component> children = new ArrayList<Component>
	@Accessors var List<Clazz> clazzes = new ArrayList<Clazz>

	@Accessors Component parentComponent

	@Accessors Application belongingApplication

	@Accessors var Vector4f color

	var boolean opened = false

	def boolean isOpened() {
		opened
	}

	def void setOpened(boolean openedParam) {
		if (!openedParam) setAllChildrenUnopened()

		this.opened = openedParam
	}

	private def setAllChildrenUnopened() {
		children.forEach [
			it.setOpened(false)
		]
	}
	
	def void openAllComponents() {
		opened = true
		children.forEach[it.openAllComponents()]
	}

	override void destroy() {
		children.forEach[it.destroy()]
		clazzes.forEach[it.destroy()]
		super.destroy()
	}

	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()

		children.forEach[it.clearAllPrimitiveObjects()]
		clazzes.forEach[it.clearAllPrimitiveObjects()]
	}

	override void highlight() {
		this.primitiveObjects.forEach [
			it.highlight(ColorDefinitions::highlightColor)
		]
		highlighted = true
	}

	override void unhighlight() {
		if (highlighted) {
			this.primitiveObjects.forEach [
				it.unhighlight()
			]
			highlighted = false
		} else {
			children.forEach[it.unhighlight()]
			clazzes.forEach[it.unhighlight()]
		}
	}
}
