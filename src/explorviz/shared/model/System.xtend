package explorviz.shared.model

import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

class System extends DrawNodeEntity {
	@Accessors List<NodeGroup> nodeGroups = new ArrayList<NodeGroup>
	
	@Accessors Landscape parent
	
	var boolean opened = true
	
	public static val Vector4f plusColor = ColorDefinitions::systemPlusColor
	public static val Vector4f foregroundColor = ColorDefinitions::systemForegroundColor
	public static val Vector4f backgroundColor = ColorDefinitions::systemBackgroundColor

	def boolean isOpened() {
		opened
	}

	def void setOpened(boolean openedParam) {
		if (openedParam) {
			nodeGroups.forEach [
				it.visible = true
				it.setOpened(false)
			]
		} else {
			nodeGroups.forEach [
				it.visible = false
				it.setAllChildrenVisibility(false)
			]
		}

		this.opened = openedParam
	}

	override void destroy() {
		nodeGroups.forEach[it.destroy()]
		super.destroy()
	}
}