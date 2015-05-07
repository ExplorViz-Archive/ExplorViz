package explorviz.shared.model

import explorviz.shared.model.helper.DrawEdgeEntity
import org.eclipse.xtend.lib.annotations.Accessors

class Communication extends DrawEdgeEntity {
	@Accessors int requests
	@Accessors String technology
	
	@Accessors float averageResponseTimeInNanoSec

	@Accessors Application source
	@Accessors Application target
	
	@Accessors Clazz sourceClazz
	@Accessors Clazz targetClazz

	override void destroy() {
		super.destroy()
	}
}
