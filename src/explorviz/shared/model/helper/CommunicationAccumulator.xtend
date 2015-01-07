package explorviz.shared.model.helper

import explorviz.shared.model.helper.DrawEdgeEntity
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.List

class CommunicationAccumulator extends DrawEdgeEntity {
	@Accessors List<CommunicationTileAccumulator> tiles

	override void destroy() {
		super.destroy()
	}
}
