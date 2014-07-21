package explorviz.shared.model

import java.util.ArrayList
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions

class Component extends Draw3DNodeEntity {
	@Property var String name
	@Property var String fullQualifiedName
	@Property var boolean synthetic = false
	@Property var boolean foundation = false

	@Property var children = new ArrayList<Component>
	@Property var clazzes = new ArrayList<Clazz>

	@Property Component parentComponent

	@Property Application belongingApplication

	@Property var Vector4f color

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
