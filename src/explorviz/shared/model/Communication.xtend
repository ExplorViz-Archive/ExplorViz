package explorviz.shared.model

import explorviz.shared.model.helper.DrawEdgeEntity
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

	override void destroy() {
		super.destroy()
	}
}
